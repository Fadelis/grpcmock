package org.grpcmock.definitions.response.steps;

import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.ResponseAction;

public interface ExceptionResponseActionBuilderStep extends
    BuilderStep,
    DelayBuilderStep<ExceptionResponseActionBuilderStep> {

  <RespT> ResponseAction<RespT> build();
}
