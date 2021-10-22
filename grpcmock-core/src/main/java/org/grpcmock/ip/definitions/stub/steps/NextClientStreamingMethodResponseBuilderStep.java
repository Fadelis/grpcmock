package org.grpcmock.ip.definitions.stub.steps;

import org.grpcmock.ip.definitions.BuilderStep;

/**
 * @author Fadelis
 */
public interface NextClientStreamingMethodResponseBuilderStep<ReqT, RespT> extends
    BuilderStep,
    MethodStubBuilder<ReqT, RespT>,
    NextSingleResponseBuilderStep<NextClientStreamingMethodResponseBuilderStep<ReqT, RespT>, ReqT, RespT>,
    NextStreamRequestProxyResponseBuilderStep<NextClientStreamingMethodResponseBuilderStep<ReqT, RespT>, ReqT, RespT> {

}
