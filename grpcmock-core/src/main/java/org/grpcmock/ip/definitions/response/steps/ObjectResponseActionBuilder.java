package org.grpcmock.ip.definitions.response.steps;

import org.grpcmock.ip.definitions.BuilderStep;
import org.grpcmock.ip.definitions.response.ResponseAction;

/**
 * @author Fadelis
 */
public interface ObjectResponseActionBuilder<RespT> extends
    BuilderStep,
    DelayBuilderStep<ObjectResponseActionBuilder<RespT>> {

  ResponseAction<RespT> build();
}
