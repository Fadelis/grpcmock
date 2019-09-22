package org.grpcmock.definitions.stub.steps;

import io.grpc.MethodDescriptor;
import javax.annotation.Nonnull;

public interface ServiceBuilderStep extends BuilderStep {

  <ReqT, RespT> SingleResponseBuilderStep<ReqT, RespT> forMethod(
      @Nonnull MethodDescriptor<ReqT, RespT> method);
}
