package org.grpcmock.ip.definitions.stub.steps;

import org.grpcmock.ip.definitions.BuilderStep;

/**
 * @author Fadelis
 */
public interface NextUnaryMethodResponseBuilderStep<ReqT, RespT> extends
    BuilderStep,
    MethodStubBuilder<ReqT, RespT>,
    NextSingleResponseBuilderStep<NextUnaryMethodResponseBuilderStep<ReqT, RespT>, ReqT, RespT>,
    NextSingleRequestProxyResponseBuilderStep<NextUnaryMethodResponseBuilderStep<ReqT, RespT>, ReqT, RespT> {

}
