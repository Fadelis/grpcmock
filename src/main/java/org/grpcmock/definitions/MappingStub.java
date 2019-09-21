package org.grpcmock.definitions;

import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import java.util.List;

public class MappingStub<ReqT, RespT> {

  private final String serviceName;
  private final MethodDescriptor<ReqT, RespT> method;
  private final List<Response<ReqT, RespT>> responses;

  MappingStub(
      String serviceName,
      MethodDescriptor<ReqT, RespT> method,
      List<Response<ReqT, RespT>> responses
  ) {
    this.serviceName = serviceName;
    this.method = method;
    this.responses = responses;
  }

  public ServerServiceDefinition serverServiceDefinition() {
    return ServerServiceDefinition.builder(serviceName)
        .addMethod(method, ServerCalls.asyncUnaryCall(this::unaryCall))
        .build();
  }

  private void unaryCall(ReqT request, StreamObserver<RespT> streamObserver) {
    responses.stream()
        .filter(response -> !response.wasCalled())
        .findFirst()
        .orElseGet(() -> responses.get(responses.size() - 1))
        .execute(request, streamObserver);
  }
}
