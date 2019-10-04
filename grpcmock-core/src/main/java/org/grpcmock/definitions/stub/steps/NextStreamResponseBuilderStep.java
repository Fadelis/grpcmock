package org.grpcmock.definitions.stub.steps;

import javax.annotation.Nonnull;
import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.Response;
import org.grpcmock.definitions.response.ResponseAction;
import org.grpcmock.definitions.response.steps.ExceptionResponseActionBuilder;
import org.grpcmock.definitions.response.steps.ObjectResponseActionBuilder;
import org.grpcmock.definitions.response.steps.StreamResponseBuilder;

/**
 * @author Fadelis
 */
public interface NextStreamResponseBuilderStep<ReqT, RespT> extends
    BuilderStep,
    MethodStubBuilder {

  /**
   * <p>Defines a stream {@link Response} for subsequent request call that will execute multiple
   * {@link ResponseAction}.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last stream response defined will be returned for those requests.
   */
  NextStreamResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull StreamResponseBuilder<RespT> response);

  /**
   * <p>Defines single action stream {@link Response} for subsequent request call to this stub.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last stream response defined will be returned for those requests.
   */
  NextStreamResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ObjectResponseActionBuilder<RespT> response);

  /**
   * <p>Defines exception {@link Response} for subsequent request call to this stub.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last stream response defined will be returned for those requests.
   */
  NextStreamResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ExceptionResponseActionBuilder response);
}
