package org.grpcmock.definitions.stub.steps;

import javax.annotation.Nonnull;
import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.Response;
import org.grpcmock.definitions.response.steps.ExceptionResponseActionBuilder;
import org.grpcmock.definitions.response.steps.ObjectResponseActionBuilder;

/**
 * @author Fadelis
 */
public interface SingleResponseBuilderStep<BUILDER extends BuilderStep, RespT> extends BuilderStep {

  /**
   * Defines a single {@link Response} that will be returned for the request and complete it.
   */
  BUILDER willReturn(@Nonnull ObjectResponseActionBuilder<RespT> response);

  /**
   * Defines a exception {@link Response} that will terminate the request.
   */
  BUILDER willReturn(@Nonnull ExceptionResponseActionBuilder response);
}
