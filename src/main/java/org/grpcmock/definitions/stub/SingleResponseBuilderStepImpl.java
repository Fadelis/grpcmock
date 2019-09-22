package org.grpcmock.definitions.stub;

import io.grpc.MethodDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.matcher.PredicateHeadersMatcher;
import org.grpcmock.definitions.matcher.PredicateRequestMatcher;
import org.grpcmock.definitions.response.ObjectResponse;
import org.grpcmock.definitions.response.Response;
import org.grpcmock.definitions.stub.steps.MappingStubBuilder;
import org.grpcmock.definitions.stub.steps.NextSingleResponseBuilderStep;
import org.grpcmock.definitions.stub.steps.SingleResponseBuilderStep;

public class SingleResponseBuilderStepImpl<ReqT, RespT> implements
    MappingStubBuilder,
    SingleResponseBuilderStep<ReqT, RespT>,
    NextSingleResponseBuilderStep<ReqT, RespT> {

  private final String serviceName;
  private final MethodDescriptor<ReqT, RespT> method;
  private final List<Response<ReqT, RespT>> responses = new ArrayList<>();
  private final Map<String, Predicate<String>> headerPredicates = new HashMap<>();
  private Predicate<ReqT> requestPredicate = request -> true;

  SingleResponseBuilderStepImpl(
      @Nonnull String serviceName,
      @Nonnull MethodDescriptor<ReqT, RespT> method
  ) {
    this.serviceName = serviceName;
    this.method = method;
  }

  @Override
  public SingleResponseBuilderStep<ReqT, RespT> withHeader(
      @Nonnull String headerName,
      @Nonnull Predicate<String> valuePredicate
  ) {
    Objects.requireNonNull(headerName);
    Objects.requireNonNull(valuePredicate);
    this.headerPredicates.put(headerName, valuePredicate);
    return this;
  }

  @Override
  public SingleResponseBuilderStep<ReqT, RespT> withRequest(
      @Nonnull Predicate<ReqT> requestPredicate
  ) {
    Objects.requireNonNull(requestPredicate);
    this.requestPredicate = requestPredicate;
    return this;
  }

  @Override
  public NextSingleResponseBuilderStep<ReqT, RespT> willReturn(
      @Nonnull Response<ReqT, RespT> response
  ) {
    Objects.requireNonNull(response);
    this.responses.add(response);
    return this;
  }

  @Override
  public NextSingleResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull Response<ReqT, RespT> response
  ) {
    Objects.requireNonNull(response);
    this.responses.add(response);
    return this;
  }

  @Override
  public ServiceStub<ReqT, RespT> build() {
    if (responses.isEmpty()) {
      responses.add(new ObjectResponse<>(null));
    }
    return new ServiceStub<>(
        serviceName,
        method,
        Collections.singletonList(new StubScenario<>(
            new PredicateHeadersMatcher(headerPredicates),
            new PredicateRequestMatcher<>(requestPredicate),
            responses
        ))
    );
  }
}
