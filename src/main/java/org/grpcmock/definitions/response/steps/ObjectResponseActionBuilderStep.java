package org.grpcmock.definitions.response.steps;

import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.ResponseAction;

public interface ObjectResponseActionBuilderStep<RespT> extends
    BuilderStep,
    DelayBuilderStep<ObjectResponseActionBuilderStep<RespT>> {

  ResponseAction<RespT> build();
}
