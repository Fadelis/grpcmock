package org.grpcmock.definitions.stub;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.response.Response;
import org.grpcmock.definitions.response.ResponseImpl;
import org.grpcmock.definitions.response.ResponseProxyImpl;
import org.grpcmock.definitions.response.steps.ExceptionResponseActionBuilder;
import org.grpcmock.definitions.response.steps.ObjectResponseActionBuilder;
import org.grpcmock.definitions.stub.steps.ClientStreamingMethodStubBuilderStep;
import org.grpcmock.definitions.stub.steps.NextClientStreamingMethodResponseBuilderStep;
import org.grpcmock.definitions.verification.RequestPatternBuilderImpl;
import org.grpcmock.exception.GrpcMockException;

/**
 * @author Fadelis
 */
public final class ClientStreamingMethodStubBuilderImpl<ReqT, RespT> implements
    ClientStreamingMethodStubBuilderStep<ReqT, RespT>,
    NextClientStreamingMethodResponseBuilderStep<ReqT, RespT> {

  private final MethodDescriptor<ReqT, RespT> method;
  private final List<Response<ReqT, RespT>> responses;
  private final RequestPatternBuilderImpl<ReqT> requestPatternBuilder;

  public ClientStreamingMethodStubBuilderImpl(@Nonnull MethodDescriptor<ReqT, RespT> method) {
    Objects.requireNonNull(method);
    if (method.getType().clientSendsOneMessage()) {
      throw new GrpcMockException("This builder accepts only client or bidi streaming methods");
    }
    this.method = method;
    this.responses = new ArrayList<>();
    this.requestPatternBuilder = new RequestPatternBuilderImpl<>(method);
  }

  @Override
  public <T> ClientStreamingMethodStubBuilderStep<ReqT, RespT> withHeader(
      @Nonnull Metadata.Key<T> headerKey,
      @Nonnull Predicate<T> predicate
  ) {
    this.requestPatternBuilder.withHeader(headerKey, predicate);
    return this;
  }

  @Override
  public ClientStreamingMethodStubBuilderStep<ReqT, RespT> withFirstRequest(
      @Nonnull Predicate<ReqT> requestPredicate
  ) {
    this.requestPatternBuilder.clearRequestsPredicates();
    this.requestPatternBuilder.withFirstRequest(requestPredicate);
    return this;
  }

  @Override
  public NextClientStreamingMethodResponseBuilderStep<ReqT, RespT> willReturn(
      @Nonnull ObjectResponseActionBuilder<RespT> response
  ) {
    Objects.requireNonNull(response);
    this.responses.add(new ResponseImpl<>(response.build()));
    return this;
  }

  @Override
  public NextClientStreamingMethodResponseBuilderStep<ReqT, RespT> willReturn(
      @Nonnull ExceptionResponseActionBuilder response
  ) {
    Objects.requireNonNull(response);
    this.responses.add(new ResponseImpl<>(response.build()));
    return this;
  }

  @Override
  public NextClientStreamingMethodResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ObjectResponseActionBuilder<RespT> response
  ) {
    return willReturn(response);
  }

  @Override
  public NextClientStreamingMethodResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ExceptionResponseActionBuilder response
  ) {
    return willReturn(response);
  }

  @Override
  public NextClientStreamingMethodResponseBuilderStep<ReqT, RespT> willProxyTo(
      @Nonnull Function<StreamObserver<RespT>, StreamObserver<ReqT>> streamRequestResponseProxy) {
    Objects.requireNonNull(streamRequestResponseProxy);
    this.responses.add(new ResponseProxyImpl<>(streamRequestResponseProxy));
    return this;
  }

  @Override
  public NextClientStreamingMethodResponseBuilderStep<ReqT, RespT> nextWillProxyTo(
      @Nonnull Function<StreamObserver<RespT>, StreamObserver<ReqT>> streamRequestResponseProxy) {
    return willProxyTo(streamRequestResponseProxy);
  }

  @Override
  public MethodStub<ReqT, RespT> build() {
    return new MethodStub<>(
        method,
        Collections.singletonList(new StubScenario<>(requestPatternBuilder.build(), responses))
    );
  }
}
