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
   * Creates a stub for {@link MethodType#UNARY} method or {@link MethodType#SERVER_STREAMING}
   * method with a single response.
   */
  <ReqT, RespT> UnaryMethodStubBuilderStep<ReqT, RespT> forMethod(
      @Nonnull MethodDescriptor<ReqT, RespT> method);

  /**
   * Creates a stub for {@link MethodType#SERVER_STREAMING} method.
   */
  <ReqT, RespT> ServerStreamingMethodStubBuilderStep<ReqT, RespT> forServerStreamingMethod(
      @Nonnull MethodDescriptor<ReqT, RespT> method);

  /**
   * Creates a stub for {@link MethodType#CLIENT_STREAMING} method or {@link
   * MethodType#BIDI_STREAMING} method with a single response at request stream completion.
   */
  <ReqT, RespT> ClientStreamingMethodStubBuilderStep<ReqT, RespT> forClientStreamingMethod(
      @Nonnull MethodDescriptor<ReqT, RespT> method);

  /**
   * Creates a stub for {@link MethodType#BIDI_STREAMING} method.
   */
  <ReqT, RespT> BidiStreamingMethodStubBuilderStep<ReqT, RespT> forBidiStreamingMethod(
      @Nonnull MethodDescriptor<ReqT, RespT> method);
}
