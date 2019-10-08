package org.grpcmock.definitions.stub;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.matcher.HeadersMatcher;
import org.grpcmock.definitions.matcher.PredicateRequestMatcher;
import org.grpcmock.definitions.matcher.steps.HeadersMatcherBuilder;
import org.grpcmock.definitions.response.Response;
import org.grpcmock.definitions.response.ResponseImpl;
import org.grpcmock.definitions.response.steps.ExceptionResponseActionBuilder;
import org.grpcmock.definitions.response.steps.ObjectResponseActionBuilder;
import org.grpcmock.definitions.response.steps.StreamResponseBuilder;
import org.grpcmock.definitions.stub.steps.NextStreamResponseBuilderStep;
import org.grpcmock.definitions.stub.steps.ServerStreamingMethodStubBuilderStep;
import org.grpcmock.exception.GrpcMockException;

/**
 * @author Fadelis
 */
public class ServerStreamingMethodStubBuilderImpl<ReqT, RespT> implements
    ServerStreamingMethodStubBuilderStep<ReqT, RespT>,
    NextStreamResponseBuilderStep<ReqT, RespT> {

  private final MethodDescriptor<ReqT, RespT> method;
  private final List<Response<ReqT, RespT>> responses = new ArrayList<>();
  private final HeadersMatcherBuilder headersMatcherBuilder = HeadersMatcher.builder();
  private Predicate<ReqT> requestPredicate = request -> true;

  public ServerStreamingMethodStubBuilderImpl(@Nonnull MethodDescriptor<ReqT, RespT> method) {
    Objects.requireNonNull(method);
    if (method.getType() != MethodType.SERVER_STREAMING) {
      throw new GrpcMockException("This builder accepts only server streaming methods");
    }
    this.method = method;
  }

  @Override
  public <T> ServerStreamingMethodStubBuilderStep<ReqT, RespT> withHeader(
      @Nonnull Metadata.Key<T> headerKey,
      @Nonnull Predicate<T> predicate
  ) {
    headersMatcherBuilder.withHeader(headerKey, predicate);
    return this;
  }

  @Override
  public ServerStreamingMethodStubBuilderStep<ReqT, RespT> withRequest(
      @Nonnull Predicate<ReqT> requestPredicate
  ) {
    Objects.requireNonNull(requestPredicate);
    this.requestPredicate = requestPredicate;
    return this;
  }

  @Override
  public NextStreamResponseBuilderStep<ReqT, RespT> willReturn(
      @Nonnull ObjectResponseActionBuilder<RespT> response
  ) {
    Objects.requireNonNull(response);
    this.responses.add(new ResponseImpl<>(response.build()));
    return this;
  }

  @Override
  public NextStreamResponseBuilderStep<ReqT, RespT> willReturn(
      @Nonnull ExceptionResponseActionBuilder response
  ) {
    Objects.requireNonNull(response);
    this.responses.add(new ResponseImpl<>(response.build()));
    return this;
  }

  @Override
  public NextStreamResponseBuilderStep<ReqT, RespT> willReturn(
      @Nonnull StreamResponseBuilder<RespT> response
  ) {
    Objects.requireNonNull(response);
    this.responses.add(response.build());
    return this;
  }

  @Override
  public NextStreamResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull StreamResponseBuilder<RespT> response
  ) {
    return willReturn(response);
  }

  @Override
  public NextStreamResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ObjectResponseActionBuilder<RespT> response) {
    return willReturn(response);
  }

  @Override
  public NextStreamResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ExceptionResponseActionBuilder response) {
    return willReturn(response);
  }

  @Override
  public MethodStub<ReqT, RespT> build() {
    return new MethodStub<>(
        method,
        Collections.singletonList(new StubScenario<>(
            headersMatcherBuilder.build(),
            new PredicateRequestMatcher<>(requestPredicate),
            responses
        ))
    );
  }
}
