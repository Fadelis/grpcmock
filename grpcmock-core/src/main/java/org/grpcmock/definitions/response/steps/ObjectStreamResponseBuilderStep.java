package org.grpcmock.definitions.response.steps;

import io.grpc.Status;
import javax.annotation.Nonnull;
import org.grpcmock.GrpcMock;
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

  /**
   * Defines the next {@link ResponseAction} in the stream response.
   */
  default ObjectStreamResponseBuilderStep<RespT> and(@Nonnull RespT responseObject) {
    return and(GrpcMock.response(responseObject));
  }

  /**
   * Defines a status exception {@link ResponseAction} for the stream response. This is a
   * terminating action so no further actions can be added to the stream response.
   */
  default ExceptionStreamResponseBuildersStep<RespT> and(@Nonnull Status status) {
    return and(GrpcMock.statusException(status));
  }
}
