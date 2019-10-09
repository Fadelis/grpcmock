package org.grpcmock.definitions.stub;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.matcher.HeadersMatcher;
import org.grpcmock.definitions.matcher.PredicateRequestMatcher;
import org.grpcmock.definitions.matcher.steps.HeadersMatcherBuilder;
import org.grpcmock.definitions.response.Response;
import org.grpcmock.definitions.response.ResponseImpl;
import org.grpcmock.definitions.response.ResponseProxyImpl;
import org.grpcmock.definitions.response.steps.ExceptionResponseActionBuilder;
import org.grpcmock.definitions.response.steps.ObjectResponseActionBuilder;
import org.grpcmock.definitions.stub.steps.NextUnaryMethodResponseBuilderStep;
import org.grpcmock.definitions.stub.steps.UnaryMethodStubBuilderStep;
import org.grpcmock.exception.GrpcMockException;

/**
 * @author Fadelis
 */
public class UnaryMethodStubBuilderImpl<ReqT, RespT> implements
    UnaryMethodStubBuilderStep<ReqT, RespT>,
    NextUnaryMethodResponseBuilderStep<ReqT, RespT> {

  private final MethodDescriptor<ReqT, RespT> method;
  private final List<Response<ReqT, RespT>> responses = new ArrayList<>();
  private final HeadersMatcherBuilder headersMatcherBuilder = HeadersMatcher.builder();
  private Predicate<ReqT> requestPredicate = request -> true;

  public UnaryMethodStubBuilderImpl(@Nonnull MethodDescriptor<ReqT, RespT> method) {
    Objects.requireNonNull(method);
    if (!method.getType().clientSendsOneMessage()) {
      throw new GrpcMockException("This builder accepts only unary and server streaming methods");
    }
    this.method = method;
  }

  @Override
  public <T> UnaryMethodStubBuilderStep<ReqT, RespT> withHeader(
      @Nonnull Metadata.Key<T> headerKey,
      @Nonnull Predicate<T> predicate
  ) {
    headersMatcherBuilder.withHeader(headerKey, predicate);
    return this;
  }

  @Override
  public UnaryMethodStubBuilderStep<ReqT, RespT> withRequest(
      @Nonnull Predicate<ReqT> requestPredicate
  ) {
    Objects.requireNonNull(requestPredicate);
    this.requestPredicate = requestPredicate;
    return this;
  }

  @Override
  public NextUnaryMethodResponseBuilderStep<ReqT, RespT> willReturn(
      @Nonnull ObjectResponseActionBuilder<RespT> response
  ) {
    Objects.requireNonNull(response);
    this.responses.add(new ResponseImpl<>(response.build()));
    return this;
  }

  @Override
  public NextUnaryMethodResponseBuilderStep<ReqT, RespT> willReturn(
      @Nonnull ExceptionResponseActionBuilder response
  ) {
    Objects.requireNonNull(response);
    this.responses.add(new ResponseImpl<>(response.build()));
    return this;
  }

  @Override
  public NextUnaryMethodResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ObjectResponseActionBuilder<RespT> response
  ) {
    return willReturn(response);
  }

  @Override
  public NextUnaryMethodResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ExceptionResponseActionBuilder response
  ) {
    return willReturn(response);
  }

  @Override
  public NextUnaryMethodResponseBuilderStep<ReqT, RespT> willProxyTo(
      @Nonnull BiConsumer<ReqT, StreamObserver<RespT>> responseProxy) {
    Objects.requireNonNull(responseProxy);
    this.responses.add(new ResponseProxyImpl<>(responseProxy));
    return this;
  }

  @Override
  public NextUnaryMethodResponseBuilderStep<ReqT, RespT> nextWillProxyTo(
      @Nonnull BiConsumer<ReqT, StreamObserver<RespT>> responseProxy) {
    return willProxyTo(responseProxy);
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
