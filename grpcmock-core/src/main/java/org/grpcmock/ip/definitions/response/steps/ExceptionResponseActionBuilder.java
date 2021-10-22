package org.grpcmock.ip.definitions.response.steps;

import org.grpcmock.ip.definitions.BuilderStep;
import org.grpcmock.ip.definitions.response.TerminatingResponseAction;

/**
 * @author Fadelis
 */
public interface ExceptionResponseActionBuilder extends
    BuilderStep,
    DelayBuilderStep<ExceptionResponseActionBuilder> {

  <RespT> TerminatingResponseAction<RespT> build();
}
