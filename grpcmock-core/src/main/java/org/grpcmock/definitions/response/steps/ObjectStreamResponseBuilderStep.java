package org.grpcmock.definitions.response.steps;

import javax.annotation.Nonnull;
import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.ResponseAction;

/**
 * @author Fadelis
 */
public interface ObjectStreamResponseBuilderStep<RespT> extends
    BuilderStep,
    StreamResponseBuilder<RespT> {

  /**
   * Defines the next {@link ResponseAction} in the stream response.
   */
  ObjectStreamResponseBuilderStep<RespT> and(
      @Nonnull ObjectResponseActionBuilder<RespT> responseAction);

  /**
   * Defines a exception {@link ResponseAction} for the stream response. This is a terminating
   * action so no further actions can be added to the stream response.
   */
  ExceptionStreamResponseBuildersStep<RespT> and(
      @Nonnull ExceptionResponseActionBuilder responseAction);
}
