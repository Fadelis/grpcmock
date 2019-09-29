package org.grpcmock.definitions.response.steps;

import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.ResponseAction;

/**
 * @author Fadelis
 */
public interface ObjectResponseActionBuilder<RespT> extends
    BuilderStep,
    DelayBuilderStep<ObjectResponseActionBuilder<RespT>> {

  ResponseAction<RespT> build();
}
