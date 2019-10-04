package org.grpcmock.definitions.stub.steps;

import javax.annotation.Nonnull;
import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.Response;
import org.grpcmock.definitions.response.steps.ExceptionResponseActionBuilder;
import org.grpcmock.definitions.response.steps.ObjectResponseActionBuilder;

/**
 * @author Fadelis
 */
public interface NextSingleResponseBuilderStep<ReqT, RespT> extends
    BuilderStep,
    MethodStubBuilder {

  /**
   * <p>Defines {@link Response} for subsequent request call to this stub.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last response defined will be returned for those requests.
   */
  NextSingleResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ObjectResponseActionBuilder<RespT> response);

  /**
   * <p>Defines exception {@link Response} for subsequent request call to this stub.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last response defined will be returned for those requests.
   */
  NextSingleResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ExceptionResponseActionBuilder response);
}
