package org.grpcmock.definitions.stub.steps;

import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.BuilderStep;

/**
 * @author Fadelis
 */
public interface ServiceBuilderStep extends BuilderStep {

  /**
   * Creates a stub for {@link MethodType#UNARY} or {@link MethodType#SERVER_STREAMING} with a
   * single response.
   */
  <ReqT, RespT> SingleResponseBuilderStep<ReqT, RespT> forMethod(
      @Nonnull MethodDescriptor<ReqT, RespT> method);
}
