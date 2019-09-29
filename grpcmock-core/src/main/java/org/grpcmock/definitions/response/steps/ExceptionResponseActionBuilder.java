package org.grpcmock.definitions.response.steps;

import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.TerminatingResponseAction;

/**
 * @author Fadelis
 */
public interface ExceptionResponseActionBuilder extends
    BuilderStep,
    DelayBuilderStep<ExceptionResponseActionBuilder> {

  <RespT> TerminatingResponseAction<RespT> build();
}
