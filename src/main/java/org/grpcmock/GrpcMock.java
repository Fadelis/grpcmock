package org.grpcmock;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServiceDescriptor;
import io.grpc.Status;
import io.grpc.util.MutableHandlerRegistry;
import java.io.IOException;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.response.ExceptionResponseActionBuilder;
import org.grpcmock.definitions.response.ObjectResponseActionBuilder;
import org.grpcmock.definitions.response.steps.ExceptionResponseActionBuilderStep;
import org.grpcmock.definitions.response.steps.ObjectResponseActionBuilderStep;
import org.grpcmock.definitions.stub.ServiceBuilderStepImpl;
import org.grpcmock.definitions.stub.steps.MappingStubBuilder;
import org.grpcmock.definitions.stub.steps.ServiceBuilderStep;
import org.grpcmock.exception.GrpcMockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main gRPCMock class that start the mock server.
 *
 * @author Fadelis
 */
public final class GrpcMock {

  private static final Logger log = LoggerFactory.getLogger(GrpcMock.class);
  public static final int DEFAULT_PORT = 8888;
  private static final ThreadLocal<GrpcMock> INSTANCE = ThreadLocal
      .withInitial(() -> grpcMock().build());

  private final Server server;
  private final MutableHandlerRegistry handlerRegistry;

  GrpcMock(@Nonnull Server server, @Nonnull MutableHandlerRegistry handlerRegistry) {
    this.server = server;
    this.handlerRegistry = handlerRegistry;
  }

  public int getPort() {
    return server.getPort();
  }

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

  public GrpcMock stop() {
    server.shutdownNow();
    return this;
  }

  public void register(@Nonnull MappingStubBuilder mappingStubBuilder) {
    Objects.requireNonNull(mappingStubBuilder);
    handlerRegistry.addService(mappingStubBuilder.build().serverServiceDefinition());
  }

  public void resetAll() {
    handlerRegistry.getServices().forEach(handlerRegistry::removeService);
  }

  public MutableHandlerRegistry handlerRegistry() {
    return handlerRegistry;
  }

  public static GrpcMockBuilder grpcMock() {
    return grpcMock(DEFAULT_PORT);
  }

  public static GrpcMockBuilder grpcMock(int port) {
    return new GrpcMockBuilder(ServerBuilder.forPort(port));
  }

  public static GrpcMockBuilder grpcMock(@Nonnull ServerBuilder serverBuilder) {
    Objects.requireNonNull(serverBuilder);
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

  public static void resetMappings() {
    INSTANCE.get().resetAll();
  }

  public static void stubFor(MappingStubBuilder mappingStubBuilder) {
    INSTANCE.get().register(mappingStubBuilder);
  }

  public static ServiceBuilderStep service(@Nonnull ServiceDescriptor serviceDescriptor) {
    Objects.requireNonNull(serviceDescriptor);
    return new ServiceBuilderStepImpl(serviceDescriptor);
  }

  public static ServiceBuilderStep service(@Nonnull String serviceName) {
    Objects.requireNonNull(serviceName);
    return new ServiceBuilderStepImpl(serviceName);
  }

  public static <RespT> ObjectResponseActionBuilderStep<RespT> response(
      @Nonnull RespT responseObject) {
    return new ObjectResponseActionBuilder<>(responseObject);
  }

  public static ExceptionResponseActionBuilderStep exception(@Nonnull Throwable exception) {
    return new ExceptionResponseActionBuilder(exception);
  }

  public static ExceptionResponseActionBuilderStep statusException(@Nonnull Status status) {
    Objects.requireNonNull(status);
    return new ExceptionResponseActionBuilder(status.asRuntimeException());
  }
}
