package org.grpcmock.ip.definitions.stub.steps;

import org.grpcmock.ip.definitions.matcher.steps.FirstRequestMatcherBuilderStep;
import org.grpcmock.ip.definitions.matcher.steps.HeadersMatcherBuilderStep;

/**
 * @author Fadelis
 */
public interface ClientStreamingMethodStubBuilderStep<ReqT, RespT> extends
    MethodStubBuilder<ReqT, RespT>,
    HeadersMatcherBuilderStep<ClientStreamingMethodStubBuilderStep<ReqT, RespT>>,
    FirstRequestMatcherBuilderStep<ClientStreamingMethodStubBuilderStep<ReqT, RespT>, ReqT>,
    SingleResponseBuilderStep<NextClientStreamingMethodResponseBuilderStep<ReqT, RespT>, RespT>,
    StreamRequestProxyResponseBuilderStep<NextClientStreamingMethodResponseBuilderStep<ReqT, RespT>, ReqT, RespT> {

}
