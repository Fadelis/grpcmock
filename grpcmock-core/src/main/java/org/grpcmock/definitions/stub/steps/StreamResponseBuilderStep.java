package org.grpcmock.definitions.stub.steps;

import javax.annotation.Nonnull;
import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.Response;
import org.grpcmock.definitions.response.ResponseAction;
import org.grpcmock.definitions.response.steps.StreamResponseBuilder;

/**
 * @author Fadelis
 */
public interface StreamResponseBuilderStep<ReqT, RespT> extends BuilderStep {

  /**
   * Defines a stream {@link Response} that will execute multiple {@link ResponseAction}.
   */
  NextStreamResponseBuilderStep<ReqT, RespT> willReturn(
      @Nonnull StreamResponseBuilder<RespT> response);
}
