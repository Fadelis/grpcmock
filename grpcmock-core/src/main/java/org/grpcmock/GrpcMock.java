package org.grpcmock;

import static java.util.Optional.ofNullable;

import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.grpc.util.MutableHandlerRegistry;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.response.Delay;
import org.grpcmock.definitions.response.ExceptionResponseActionBuilderImpl;
import org.grpcmock.definitions.response.ObjectResponseActionBuilderImpl;
import org.grpcmock.definitions.response.ResponseAction;
import org.grpcmock.definitions.response.StreamResponseBuilderImpl;
import org.grpcmock.definitions.response.steps.ExceptionResponseActionBuilder;
import org.grpcmock.definitions.response.steps.ExceptionStreamResponseBuildersStep;
import org.grpcmock.definitions.response.steps.ObjectResponseActionBuilder;
import org.grpcmock.definitions.response.steps.ObjectStreamResponseBuilderStep;
import org.grpcmock.definitions.stub.MethodStub;
import org.grpcmock.definitions.stub.ServerStreamingMethodStubBuilderImpl;
import org.grpcmock.definitions.stub.ServiceStub;
import org.grpcmock.definitions.stub.UnaryMethodStubBuilderImpl;
import org.grpcmock.definitions.stub.steps.BidiStreamingMethodStubBuilderStep;
import org.grpcmock.definitions.stub.steps.ClientStreamingMethodStubBuilderStep;
import org.grpcmock.definitions.stub.steps.MethodStubBuilder;
import org.grpcmock.definitions.stub.steps.ServerStreamingMethodStubBuilderStep;
import org.grpcmock.definitions.stub.steps.UnaryMethodStubBuilderStep;
import org.grpcmock.definitions.verification.CountMatcher;
import org.grpcmock.definitions.verification.RequestPattern;
import org.grpcmock.definitions.verification.RequestPatternBuilderImpl;
import org.grpcmock.definitions.verification.steps.RequestPatternBuilderStep;
import org.grpcmock.exception.GrpcMockException;
import org.grpcmock.exception.GrpcMockVerificationError;
import org.grpcmock.interceptors.RequestCaptureInterceptor;
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

  private final Server server;
  private final MutableHandlerRegistry handlerRegistry;
  private final RequestCaptureInterceptor requestCaptureInterceptor;
  private final Map<String, ServiceStub> serviceStubs = new ConcurrentHashMap<>();

  GrpcMock(
      @Nonnull Server server,
      @Nonnull MutableHandlerRegistry handlerRegistry,
      @Nonnull RequestCaptureInterceptor requestCaptureInterceptor
  ) {
    Objects.requireNonNull(server);
    Objects.requireNonNull(handlerRegistry);
    Objects.requireNonNull(requestCaptureInterceptor);
    this.server = server;
    this.handlerRegistry = handlerRegistry;
    this.requestCaptureInterceptor = requestCaptureInterceptor;
  }

  /**
   * Retrieve current port of the server.
   *
   * @throws IllegalStateException if the server has not yet been started.
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
   * <p>Register a gRPC method stub to the server.
   * <p>If given method is already registered, then configured scenarios will be appended to
   * that method's stub.
   */
  public <ReqT, RespT> void register(@Nonnull MethodStubBuilder<ReqT, RespT> methodStubBuilder) {
    Objects.requireNonNull(methodStubBuilder);

    MethodStub<ReqT, RespT> methodStub = methodStubBuilder.build();
    // register method stub to existing service stub or create a new one with the single method stub
    ServiceStub serviceStub = serviceStubs.compute(
        methodStub.serviceName(),
        (key, registeredStub) -> ofNullable(registeredStub)
            .map(previous -> previous.registerMethod(methodStub))
            .orElseGet(() -> new ServiceStub(methodStub)));
    // create or overwrite the service definition for this service stub in the grpc server
    handlerRegistry.addService(serviceStub.serverServiceDefinition());
  }

  /**
   * Verify that given {@link RequestPattern} is called a number of times satisfying the provided {@link CountMatcher}.
   *
   * @throws GrpcMockVerificationError if the verify step fails.
   */
  public <ReqT> void verifyThat(
      @Nonnull RequestPattern<ReqT> requestPattern,
      @Nonnull CountMatcher countMatcher
  ) {
    Objects.requireNonNull(requestPattern);
    Objects.requireNonNull(countMatcher);

    int callCount = requestCaptureInterceptor.callCountFor(requestPattern);
    if (!countMatcher.test(callCount)) {
      throw new GrpcMockVerificationError(String.format(
          "Expected %s method to be called %s, but actual call count was %d",
          requestPattern.fullMethodName(), countMatcher, callCount));
    }
  }

  /**
   * Removes all stubs defined from the mock server.
   */
  public void resetAll() {
    serviceStubs.clear();
    requestCaptureInterceptor.clear();
    handlerRegistry.getServices().forEach(handlerRegistry::removeService);
  }

  /**
   * Returns gRPC Mock builder initiated with a random port.
   */
  public static GrpcMockBuilder grpcMock() {
    return grpcMock(0);
  }

  /**
   * Returns gRPC Mock builder with the given port. If given port is <code>0</code>, then a random free port will be selected.
   */
  public static GrpcMockBuilder grpcMock(int port) {
    return new GrpcMockBuilder(port);
  }

  /**
   * Returns gRPC Mock builder using the provided gRPC {@link ServerBuilder} configuration. The user is responsible that the port
   * used in the builder is available and free.
   */
  public static GrpcMockBuilder grpcMock(@Nonnull ServerBuilder serverBuilder) {
    return new GrpcMockBuilder(serverBuilder);
  }

  /**
   * Configure the global static gRPC Mock instance to use a new one with the provided port.
   */
  public static void configureFor(int port) {
    INSTANCE.set(grpcMock(port).build());
  }

  /**
   * Configure the global static gRPC Mock instance to use the provided one.
   */
  public static void configureFor(@Nonnull GrpcMock client) {
    Objects.requireNonNull(client);
    INSTANCE.set(client);
  }

  /**
   * Returns the port for the global static gRPC Mock instance.
   *
   * @throws IllegalStateException if the server has not yet been started.
   */
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
   * <p>Register a gRPC method stub to the global gRPC mock server.
   * <p>If given method is already registered, then configured scenarios will be appended to
   * that method's stub.
   * <p>When multiple stubs, satisfying the same request condition matching, are registered, the
   * last one registered will be triggered.
   *
   * @param methodStubBuilder a method stub builder created through one of {@link #unaryMethod}, {@link #serverStreamingMethod},
   * {@link #clientStreamingMethod} or {@link #bidiStreamingMethod}.
   */
  public static <ReqT, RespT> void stubFor(MethodStubBuilder<ReqT, RespT> methodStubBuilder) {
    INSTANCE.get().register(methodStubBuilder);
  }

  /**
   * Returns a stub builder for {@link MethodType#UNARY} method or {@link MethodType#SERVER_STREAMING} method with a single
   * response.
   */
  public static <ReqT, RespT> UnaryMethodStubBuilderStep<ReqT, RespT> unaryMethod(
      @Nonnull MethodDescriptor<ReqT, RespT> method) {
    return new UnaryMethodStubBuilderImpl<>(method);
  }

  /**
   * Returns a stub builder for {@link MethodType#SERVER_STREAMING} method.
   */
  public static <ReqT, RespT> ServerStreamingMethodStubBuilderStep<ReqT, RespT> serverStreamingMethod(
      @Nonnull MethodDescriptor<ReqT, RespT> method) {
    return new ServerStreamingMethodStubBuilderImpl<>(method);
  }

  /**
   * <p>Returns a stub builder for {@link MethodType#CLIENT_STREAMING} method or {@link
   * MethodType#BIDI_STREAMING} method with a single response at request stream completion.
   *
   * @deprecated Not yet implemented
   */
  @Deprecated
  public static <ReqT, RespT> ClientStreamingMethodStubBuilderStep<ReqT, RespT> clientStreamingMethod(
      @Nonnull MethodDescriptor<ReqT, RespT> method) {
    throw new GrpcMockException("Not yet implemented");
  }

  /**
   * <p>Returns a stub builder for {@link MethodType#BIDI_STREAMING} method.
   *
   * @deprecated Not yet implemented
   */
  @Deprecated
  public static <ReqT, RespT> BidiStreamingMethodStubBuilderStep<ReqT, RespT> bidiStreamingMethod(
      @Nonnull MethodDescriptor<ReqT, RespT> method) {
    throw new GrpcMockException("Not yet implemented");
  }

  /**
   * Returns a response action, which will send out the given response object via {@link StreamObserver#onNext}.
   */
  public static <RespT> ObjectResponseActionBuilder<RespT> response(
      @Nonnull RespT responseObject) {
    return new ObjectResponseActionBuilderImpl<>(responseObject);
  }

  /**
   * <p>Returns a response action, which will send out
   * the given exception via {@link StreamObserver#onError}.
   * <p>It not recommended to use this methods, because without
   * a proper {@link ServerInterceptor} translating non-gRPC exceptions to gRPC ones It will be translated to {@link
   * Status#UNKNOWN} type of exception without any message. The {@link #statusException(Status)} should be used to define concrete
   * gRPC errors.
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
      @Nonnull ObjectResponseActionBuilder<RespT> responseAction
  ) {
    Objects.requireNonNull(responseAction);
    return new StreamResponseBuilderImpl<>(responseAction.build());
  }

  /**
   * <p>Returns a stream response, which can respond with multiple {@link ResponseAction}.
   * <p>In order to configure a {@link Delay} for the actions see {@link GrpcMock#response} method.
   *
   * @param responses single response objects for the stream response. Will be returned in provided list order.
   */
  public static <RespT> ObjectStreamResponseBuilderStep<RespT> stream(
      @Nonnull List<RespT> responses
  ) {
    Objects.requireNonNull(responses);
    responses.forEach(Objects::requireNonNull);
    return new StreamResponseBuilderImpl<>(responses.stream()
        .map(GrpcMock::response)
        .map(ObjectResponseActionBuilder::build)
        .collect(Collectors.toList()));
  }

  /**
   * <p>Returns a stream response, which can respond with multiple {@link ResponseAction}.
   * <p>In order to configure a {@link Delay} for the actions see {@link GrpcMock#response} method.
   *
   * @param responses single response objects for the stream response. Will be returned in provided array order.
   */
  public static <RespT> ObjectStreamResponseBuilderStep<RespT> stream(@Nonnull RespT... responses) {
    return stream(Arrays.asList(responses));
  }

  /**
   * <p>Returns a terminating stream response, which will respond with {@link ResponseAction} and
   * terminate the call, since it will be {@link StreamObserver#onError} response.
   */
  public static <RespT> ExceptionStreamResponseBuildersStep<RespT> stream(
      @Nonnull ExceptionResponseActionBuilder responseAction
  ) {
    Objects.requireNonNull(responseAction);
    return new StreamResponseBuilderImpl<>(responseAction.build());
  }

  /**
   * <p>Verify that given method was called exactly once.
   * <p>This is the same as invoking <code>verifyThat(method, times(1))</code>.
   *
   * @throws GrpcMockVerificationError if the verify step fails.
   */
  public static <ReqT> void verifyThat(@Nonnull MethodDescriptor<ReqT, ?> method) {
    verifyThat(calledMethod(method), times(1));
  }

  /**
   * <p>Verify that given {@link RequestPattern} was called exactly once.
   * <p>This is the same as invoking <code>verifyThat(requestPattern, times(1))</code>.
   *
   * @throws GrpcMockVerificationError if the verify step fails.
   */
  public static <ReqT> void verifyThat(@Nonnull RequestPatternBuilderStep<ReqT> requestPattern) {
    verifyThat(requestPattern, times(1));
  }

  /**
   * <p>Verify that given method was called number of times satisfying provided {@link
   * CountMatcher}.
   *
   * @throws GrpcMockVerificationError if the verify step fails.
   */
  public static <ReqT> void verifyThat(
      @Nonnull MethodDescriptor<ReqT, ?> method,
      @Nonnull CountMatcher countMatcher
  ) {
    verifyThat(calledMethod(method), countMatcher);
  }

  /**
   * <p>Verify that given {@link RequestPattern} was called number of times satisfying provided
   * {@link CountMatcher}.
   *
   * @throws GrpcMockVerificationError if the verify step fails.
   */
  public static <ReqT> void verifyThat(
      @Nonnull RequestPatternBuilderStep<ReqT> requestPattern,
      @Nonnull CountMatcher countMatcher
  ) {
    Objects.requireNonNull(requestPattern);
    INSTANCE.get().verifyThat(requestPattern.build(), countMatcher);
  }

  /**
   * Returns request pattern builder instance, used for verifying call count using {@link #verifyThat(RequestPatternBuilderStep,
   * CountMatcher)}.
   */
  public static <ReqT> RequestPatternBuilderStep<ReqT> calledMethod(
      @Nonnull MethodDescriptor<ReqT, ?> method
  ) {
    return new RequestPatternBuilderImpl<>(method);
  }

  /**
   * Called exactly the specified number of times.
   */
  public static CountMatcher times(int count) {
    return CountMatcher.times(count);
  }

  /**
   * Never called.
   */
  public static CountMatcher never() {
    return CountMatcher.never();
  }

  /**
   * Called the specified number of times or more.
   */
  public static CountMatcher atLeast(int count) {
    return CountMatcher.atLeast(count);
  }

  /**
   * Called the specified number of times or less.
   */
  public static CountMatcher atMost(int count) {
    return CountMatcher.atMost(count);
  }
}
