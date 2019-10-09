package org.grpcmock.definitions.stub.steps;

import org.grpcmock.definitions.BuilderStep;

/**
 * @author Fadelis
 */
public interface NextUnaryMethodResponseBuilderStep<ReqT, RespT> extends
    BuilderStep,
    MethodStubBuilder<ReqT, RespT>,
    NextSingleResponseBuilderStep<NextUnaryMethodResponseBuilderStep<ReqT, RespT>, ReqT, RespT>,
    NextSingleRequestProxyResponseBuilderStep<NextUnaryMethodResponseBuilderStep<ReqT, RespT>, ReqT, RespT> {

}
