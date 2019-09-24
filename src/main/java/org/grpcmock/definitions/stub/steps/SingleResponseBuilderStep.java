package org.grpcmock.definitions.stub.steps;

import javax.annotation.Nonnull;
import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.Response;
import org.grpcmock.definitions.response.steps.ExceptionResponseActionBuilderStep;
import org.grpcmock.definitions.response.steps.ObjectResponseActionBuilderStep;

/**
 * @author Fadelis
 */
public interface SingleResponseBuilderStep<ReqT, RespT> extends
    BuilderStep,
    MappingStubBuilder,
    HeadersMatcherBuilderStep<SingleResponseBuilderStep<ReqT, RespT>>,
    RequestMatcherBuilderStep<SingleResponseBuilderStep<ReqT, RespT>, ReqT> {

  /**
   * Defines the single {@link Response} that will be returned for the request and complete it.
   */
  NextSingleResponseBuilderStep<ReqT, RespT> willReturn(
      @Nonnull ObjectResponseActionBuilderStep<RespT> response);

  /**
   * Defines the single {@link Response} that will be returned for the request and complete it.
   */
  NextSingleResponseBuilderStep<ReqT, RespT> willReturn(
      @Nonnull ExceptionResponseActionBuilderStep response);
}
