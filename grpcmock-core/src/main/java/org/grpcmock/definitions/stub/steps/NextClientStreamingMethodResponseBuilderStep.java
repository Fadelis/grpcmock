package org.grpcmock.definitions.stub.steps;

import org.grpcmock.definitions.BuilderStep;

/**
 * @author Fadelis
 */
public interface NextClientStreamingMethodResponseBuilderStep<ReqT, RespT> extends
    BuilderStep,
    MethodStubBuilder<ReqT, RespT>,
    NextSingleResponseBuilderStep<NextClientStreamingMethodResponseBuilderStep<ReqT, RespT>, ReqT, RespT>,
    NextStreamRequestProxyResponseBuilderStep<NextClientStreamingMethodResponseBuilderStep<ReqT, RespT>, ReqT, RespT> {

}
