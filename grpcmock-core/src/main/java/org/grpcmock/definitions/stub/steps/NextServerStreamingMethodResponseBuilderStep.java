package org.grpcmock.definitions.stub.steps;

import org.grpcmock.definitions.BuilderStep;

/**
 * @author Fadelis
 */
public interface NextServerStreamingMethodResponseBuilderStep<ReqT, RespT> extends
    BuilderStep,
    MethodStubBuilder<ReqT, RespT>,
    NextStreamResponseBuilderStep<NextServerStreamingMethodResponseBuilderStep<ReqT, RespT>, ReqT, RespT>,
    NextSingleRequestProxyResponseBuilderStep<NextServerStreamingMethodResponseBuilderStep<ReqT, RespT>, ReqT, RespT> {

}
