package org.grpcmock;

import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.ServiceDescriptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.grpc.util.MutableHandlerRegistry;
import java.io.IOException;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.response.ExceptionResponseActionBuilderImpl;
import org.grpcmock.definitions.response.ObjectResponseActionBuilderImpl;
import org.grpcmock.definitions.response.ResponseAction;
import org.grpcmock.definitions.response.StreamResponseBuilderImpl;
import org.grpcmock.definitions.response.steps.ExceptionResponseActionBuilder;
import org.grpcmock.definitions.response.steps.ExceptionStreamResponseBuildersStep;
import org.grpcmock.definitions.response.steps.ObjectResponseActionBuilder;
import org.grpcmock.definitions.response.steps.ObjectStreamResponseBuilderStep;
import org.grpcmock.definitions.stub.ServiceBuilderStepImpl;
import org.grpcmock.definitions.stub.steps.MappingStubBuilder;
import org.grpcmock.definitions.stub.steps.ServiceBuilderStep;
import org.grpcmock.exception.GrpcMockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main gRPC Mock class for managing the gRPC server.
 *
 * @author Fadelis
 */
public final class GrpcMock {

  private static final Logger log = LoggerFactory.getLogger(GrpcMock.class);
  private static final ThreadLocal<GrpcMock> INSTANCE = ThreadLocal
      .withInitial(() -> grpcMock().build());
  public static final int DEFAULT_PORT = 8888;

  private final Server server;
  private final MutableHandlerRegistry handlerRegistry;

  GrpcMock(@Nonnull Server server, @Nonnull MutableHandlerRegistry handlerRegistry) {
    Objects.requireNonNull(server);
    Objects.requireNonNull(handlerRegistry);
    this.server = server;
    this.handlerRegistry = handlerRegistry;
  }

  /**
   * Retrieve current port of the server.
   */
  public int getPort() {
    return server.getPort();
  }

  /**
   * Starts the gRPC mock server. Does not throw any exception if the server is already running.
   *
   * @throws GrpcMockException if the server is unable to start.
   */
  public GrpcMock start() {
    try {
      server.start();
    } catch (IllegalStateException e) {
      log.warn("gRPC Mock server is already started");
    } catch (IOException e) {
      throw new GrpcMockException("failed to start gRPC Mock server", e);
    }
    return this;
  }

  /**
   * Stops the gRPC mock server via {@link Server#shutdownNow}.
   */
  public GrpcMock stop() {
    server.shutdownNow();
    return this;
  }

  /**
   * Register a mock gRPC service sto to the server.
   */
  public void register(@Nonnull MappingStubBuilder mappingStubBuilder) {
    Objects.requireNonNull(mappingStubBuilder);
    handlerRegistry.addService(mappingStubBuilder.build().serverServiceDefinition());
  }

  /**
   * Removes all stubs defined from the mock server.
   */
  public void resetAll() {
    handlerRegistry.getServices().forEach(handlerRegistry::removeService);
  }

  public static GrpcMockBuilder grpcMock() {
    return grpcMock(DEFAULT_PORT);
  }

  public static GrpcMockBuilder grpcMock(int port) {
    return new GrpcMockBuilder(port);
  }

  public static GrpcMockBuilder grpcMock(@Nonnull ServerBuilder serverBuilder) {
    return new GrpcMockBuilder(serverBuilder);
  }

  public static void configureFor(int port) {
    INSTANCE.set(grpcMock(port).build());
  }

  public static void configureFor(@Nonnull GrpcMock client) {
    Objects.requireNonNull(client);
    INSTANCE.set(client);
  }

  public static int getGlobalPort() {
    return INSTANCE.get().getPort();
  }

  /**
   * Removes all stubs defined from the global mock server.
   */
  public static void resetMappings() {
    INSTANCE.get().resetAll();
  }

  /**
   * Register a gRPC service stub to the global gRPC mock server.
   */
  public static void stubFor(MappingStubBuilder mappingStubBuilder) {
    INSTANCE.get().register(mappingStubBuilder);
  }

  /**
   * <p>Returns a service stub builder step using a gRPC {@link ServiceDescriptor}.
   * <p>A valid {@link MethodDescriptor} for the given service has to be defined next
   * in order to register a valid stub.
   */
  public static ServiceBuilderStep service(@Nonnull ServiceDescriptor serviceDescriptor) {
    return new ServiceBuilderStepImpl(serviceDescriptor);
  }

  /**
   * <p>Returns a service stub builder step using a gRPC service name.
   * <p>A valid {@link MethodDescriptor} for the given service has to be defined next
   * in order to register a valid stub.
   */
  public static ServiceBuilderStep service(@Nonnull String serviceName) {
    return new ServiceBuilderStepImpl(serviceName);
  }

  /**
   * Returns a response action, which will send out the given response object via {@link
   * StreamObserver#onNext}.
   */
  public static <RespT> ObjectResponseActionBuilder<RespT> response(
      @Nonnull RespT responseObject) {
    return new ObjectResponseActionBuilderImpl<>(responseObject);
  }

  /**
   * <p>Returns a response action, which will send out
   * the given exception via {@link StreamObserver#onError}.
   * <p>It not recommended to use this methods, because without
   * a proper {@link ServerInterceptor} translating non-gRPC exceptions to gRPC ones It will be
   * translated to {@link Status#UNKNOWN} type of exception without any message. The {@link
   * #statusException(Status)} should be used to define concrete gRPC errors.
   */
  public static ExceptionResponseActionBuilder exception(@Nonnull Throwable exception) {
    return new ExceptionResponseActionBuilderImpl(exception);
  }

  /**
   * <p>Returns a response action, which will send out a {@link StatusRuntimeException}
   * with given {@link Status} via {@link StreamObserver#onError}.
   */
  public static ExceptionResponseActionBuilder statusException(@Nonnull Status status) {
    return new ExceptionResponseActionBuilderImpl(status);
  }

  /**
   * <p>Returns a stream response, which can respond with multiple {@link ResponseAction}.
   */
  public static <RespT> ObjectStreamResponseBuilderStep<RespT> stream(
      @Nonnull ObjectResponseActionBuilder<RespT> responseAction) {
    Objects.requireNonNull(responseAction);
    return new StreamResponseBuilderImpl<>(responseAction.build());
  }

  /**
   * <p>Returns a terminating stream response, which will respond with {@link ResponseAction} and
   * terminate the call, since it will be {@link StreamObserver#onError} response.
   */
  public static <RespT> ExceptionStreamResponseBuildersStep<RespT> stream(
      @Nonnull ExceptionResponseActionBuilder responseAction) {
    Objects.requireNonNull(responseAction);
    return new StreamResponseBuilderImpl<>(responseAction.build());
  }
}
