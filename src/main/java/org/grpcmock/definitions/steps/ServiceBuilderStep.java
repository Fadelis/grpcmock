package org.grpcmock.definitions.steps;

import io.grpc.MethodDescriptor;

public interface ServiceBuilderStep {

  public <ReqT, RespT> SingleResponseBuilderStep<ReqT, RespT> forMethod(
      MethodDescriptor<ReqT, RespT> method);
}
