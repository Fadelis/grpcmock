package org.grpcmock.definitions.stub;

import io.grpc.MethodDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
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

  SingleResponseBuilderStepImpl(
      @Nonnull String serviceName,
      @Nonnull MethodDescriptor<ReqT, RespT> method
  ) {
    this.serviceName = serviceName;
    this.method = method;
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
  public MappingStub<ReqT, RespT> build() {
    if (responses.isEmpty()) {
      responses.add(new ObjectResponse<>(null));
    }
    return new MappingStub<>(serviceName, method, Collections.unmodifiableList(responses));
  }
}
