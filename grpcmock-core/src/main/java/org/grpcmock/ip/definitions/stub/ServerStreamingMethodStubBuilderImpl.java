package org.grpcmock.ip.definitions.stub;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.ip.definitions.response.Response;
import org.grpcmock.ip.definitions.response.ResponseImpl;
import org.grpcmock.ip.definitions.response.ResponseProxyImpl;
import org.grpcmock.ip.definitions.response.steps.ExceptionResponseActionBuilder;
import org.grpcmock.ip.definitions.response.steps.ObjectResponseActionBuilder;
import org.grpcmock.ip.definitions.response.steps.StreamResponseBuilder;
import org.grpcmock.ip.definitions.stub.steps.NextServerStreamingMethodResponseBuilderStep;
import org.grpcmock.ip.definitions.stub.steps.ServerStreamingMethodStubBuilderStep;
import org.grpcmock.ip.definitions.verification.RequestPatternBuilderImpl;
import org.grpcmock.ip.exception.GrpcMockException;

/**
 * @author Fadelis
 */
public final class ServerStreamingMethodStubBuilderImpl<ReqT, RespT> implements
    ServerStreamingMethodStubBuilderStep<ReqT, RespT>,
    NextServerStreamingMethodResponseBuilderStep<ReqT, RespT> {

  private final MethodDescriptor<ReqT, RespT> method;
  private final List<Response<ReqT, RespT>> responses;
  private final RequestPatternBuilderImpl<ReqT> requestPatternBuilder;

  public ServerStreamingMethodStubBuilderImpl(@Nonnull MethodDescriptor<ReqT, RespT> method) {
    Objects.requireNonNull(method);
    if (method.getType() != MethodType.SERVER_STREAMING) {
      throw new GrpcMockException("This builder accepts only server streaming methods");
    }
    this.method = method;
    this.responses = new ArrayList<>();
    this.requestPatternBuilder = new RequestPatternBuilderImpl<>(method);
  }

  @Override
  public <T> ServerStreamingMethodStubBuilderStep<ReqT, RespT> withHeader(
      @Nonnull Metadata.Key<T> headerKey,
      @Nonnull Predicate<T> predicate
  ) {
    this.requestPatternBuilder.withHeader(headerKey, predicate);
    return this;
  }

  @Override
  public ServerStreamingMethodStubBuilderStep<ReqT, RespT> withRequest(
      @Nonnull Predicate<ReqT> requestPredicate
  ) {
    this.requestPatternBuilder.withRequest(requestPredicate);
    return this;
  }

  @Override
  public NextServerStreamingMethodResponseBuilderStep<ReqT, RespT> willReturn(
      @Nonnull ObjectResponseActionBuilder<RespT> response
  ) {
    Objects.requireNonNull(response);
    this.responses.add(new ResponseImpl<>(response.build()));
    return this;
  }

  @Override
  public NextServerStreamingMethodResponseBuilderStep<ReqT, RespT> willReturn(
      @Nonnull ExceptionResponseActionBuilder response
  ) {
    Objects.requireNonNull(response);
    this.responses.add(new ResponseImpl<>(response.build()));
    return this;
  }

  @Override
  public NextServerStreamingMethodResponseBuilderStep<ReqT, RespT> willReturn(
      @Nonnull StreamResponseBuilder<RespT> response
  ) {
    Objects.requireNonNull(response);
    this.responses.add(response.build());
    return this;
  }

  @Override
  public NextServerStreamingMethodResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull StreamResponseBuilder<RespT> response
  ) {
    return willReturn(response);
  }

  @Override
  public NextServerStreamingMethodResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ObjectResponseActionBuilder<RespT> response) {
    return willReturn(response);
  }

  @Override
  public NextServerStreamingMethodResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ExceptionResponseActionBuilder response) {
    return willReturn(response);
  }

  @Override
  public NextServerStreamingMethodResponseBuilderStep<ReqT, RespT> willProxyTo(
      @Nonnull BiConsumer<ReqT, StreamObserver<RespT>> responseProxy) {
    Objects.requireNonNull(responseProxy);
    this.responses.add(new ResponseProxyImpl<>(responseProxy));
    return this;
  }

  @Override
  public NextServerStreamingMethodResponseBuilderStep<ReqT, RespT> nextWillProxyTo(
      @Nonnull BiConsumer<ReqT, StreamObserver<RespT>> responseProxy) {
    return willProxyTo(responseProxy);
  }

  @Override
  public MethodStub<ReqT, RespT> build() {
    return new MethodStub<>(
        method,
        Collections.singletonList(new StubScenario<>(requestPatternBuilder.build(), responses))
    );
  }
}
