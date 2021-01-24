package org.grpcmock.definitions.stub;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.response.Response;
import org.grpcmock.definitions.response.ResponseProxyImpl;
import org.grpcmock.definitions.stub.steps.BidiStreamingMethodStubBuilderStep;
import org.grpcmock.definitions.stub.steps.NextBidiStreamingMethodStubBuilderStep;
import org.grpcmock.definitions.verification.RequestPatternBuilderImpl;
import org.grpcmock.exception.GrpcMockException;

/**
 * @author Fadelis
 */
public final class BidiStreamingMethodStubBuilderImpl<ReqT, RespT> implements
    BidiStreamingMethodStubBuilderStep<ReqT, RespT>,
    NextBidiStreamingMethodStubBuilderStep<ReqT, RespT> {

  private final MethodDescriptor<ReqT, RespT> method;
  private final List<Response<ReqT, RespT>> responses;
  private final RequestPatternBuilderImpl<ReqT> requestPatternBuilder;

  public BidiStreamingMethodStubBuilderImpl(@Nonnull MethodDescriptor<ReqT, RespT> method) {
    Objects.requireNonNull(method);
    if (method.getType() != MethodType.BIDI_STREAMING) {
      throw new GrpcMockException("This builder accepts only bidi streaming methods");
    }
    this.method = method;
    this.responses = new ArrayList<>();
    this.requestPatternBuilder = new RequestPatternBuilderImpl<>(method);
  }

  @Override
  public <T> BidiStreamingMethodStubBuilderStep<ReqT, RespT> withHeader(
      @Nonnull Metadata.Key<T> headerKey,
      @Nonnull Predicate<T> predicate
  ) {
    this.requestPatternBuilder.withHeader(headerKey, predicate);
    return this;
  }

  @Override
  public BidiStreamingMethodStubBuilderStep<ReqT, RespT> withFirstRequest(
      @Nonnull Predicate<ReqT> requestPredicate
  ) {
    this.requestPatternBuilder.withRequestAtIndex(0, requestPredicate);
    return this;
  }

  @Override
  public NextBidiStreamingMethodStubBuilderStep<ReqT, RespT> willProxyTo(
      @Nonnull Function<StreamObserver<RespT>, StreamObserver<ReqT>> streamRequestResponseProxy) {
    Objects.requireNonNull(streamRequestResponseProxy);
    this.responses.add(new ResponseProxyImpl<>(streamRequestResponseProxy));
    return this;
  }

  @Override
  public NextBidiStreamingMethodStubBuilderStep<ReqT, RespT> nextWillProxyTo(
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
