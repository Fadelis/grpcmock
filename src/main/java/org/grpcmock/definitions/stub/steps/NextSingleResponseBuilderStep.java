package org.grpcmock.definitions.stub.steps;

import javax.annotation.Nonnull;
import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.Response;
import org.grpcmock.definitions.response.steps.ExceptionResponseActionBuilderStep;
import org.grpcmock.definitions.response.steps.ObjectResponseActionBuilderStep;

public interface NextSingleResponseBuilderStep<ReqT, RespT> extends
    BuilderStep,
    MappingStubBuilder {

  /**
   * <p>Defines {@link Response} for subsequent request call for this stub.
   * <p>If there are more request coming in for this stub than responses defined,
   * the last response defined will be returned for those requests.
   */
  NextSingleResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ObjectResponseActionBuilderStep<RespT> response);

  /**
   * <p>Defines {@link Response} for subsequent request call for this stub.
   * <p>If there are more request coming in for this stub than responses defined,
   * the last response defined will be returned for those requests.
   */
  NextSingleResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ExceptionResponseActionBuilderStep response);
}
