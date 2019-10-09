package org.grpcmock.definitions.stub.steps;

import org.grpcmock.definitions.matcher.steps.HeadersMatcherBuilderStep;
import org.grpcmock.definitions.matcher.steps.RequestMatcherBuilderStep;

/**
 * @author Fadelis
 */
public interface ServerStreamingMethodStubBuilderStep<ReqT, RespT> extends
    MethodStubBuilder<ReqT, RespT>,
    HeadersMatcherBuilderStep<ServerStreamingMethodStubBuilderStep<ReqT, RespT>>,
    RequestMatcherBuilderStep<ServerStreamingMethodStubBuilderStep<ReqT, RespT>, ReqT>,
    StreamResponseBuilderStep<NextServerStreamingMethodResponseBuilderStep<ReqT, RespT>, ReqT, RespT>,
    SingleRequestProxyResponseBuilderStep<NextServerStreamingMethodResponseBuilderStep<ReqT, RespT>, ReqT, RespT> {

}
