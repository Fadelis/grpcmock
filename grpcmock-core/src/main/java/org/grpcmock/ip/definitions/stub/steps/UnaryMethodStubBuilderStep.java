package org.grpcmock.ip.definitions.stub.steps;

import org.grpcmock.ip.definitions.matcher.steps.HeadersMatcherBuilderStep;
import org.grpcmock.ip.definitions.matcher.steps.RequestMatcherBuilderStep;

/**
 * @author Fadelis
 */
public interface UnaryMethodStubBuilderStep<ReqT, RespT> extends
    MethodStubBuilder<ReqT, RespT>,
    HeadersMatcherBuilderStep<UnaryMethodStubBuilderStep<ReqT, RespT>>,
    RequestMatcherBuilderStep<UnaryMethodStubBuilderStep<ReqT, RespT>, ReqT>,
    SingleResponseBuilderStep<NextUnaryMethodResponseBuilderStep<ReqT, RespT>, RespT>,
    SingleRequestProxyResponseBuilderStep<NextUnaryMethodResponseBuilderStep<ReqT, RespT>, ReqT, RespT> {

}
