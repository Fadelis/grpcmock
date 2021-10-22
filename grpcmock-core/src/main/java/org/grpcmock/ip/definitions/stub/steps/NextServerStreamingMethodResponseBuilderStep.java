package org.grpcmock.ip.definitions.stub.steps;

import org.grpcmock.ip.definitions.BuilderStep;

/**
 * @author Fadelis
 */
public interface NextServerStreamingMethodResponseBuilderStep<ReqT, RespT> extends
    BuilderStep,
    MethodStubBuilder<ReqT, RespT>,
    NextStreamResponseBuilderStep<NextServerStreamingMethodResponseBuilderStep<ReqT, RespT>, ReqT, RespT>,
    NextSingleRequestProxyResponseBuilderStep<NextServerStreamingMethodResponseBuilderStep<ReqT, RespT>, ReqT, RespT> {

}
