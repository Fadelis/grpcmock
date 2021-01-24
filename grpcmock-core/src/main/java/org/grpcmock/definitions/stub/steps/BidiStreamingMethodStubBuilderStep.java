package org.grpcmock.definitions.stub.steps;

import org.grpcmock.definitions.matcher.steps.FirstRequestMatcherBuilderStep;
import org.grpcmock.definitions.matcher.steps.HeadersMatcherBuilderStep;

/**
 * @author Fadelis
 */
public interface BidiStreamingMethodStubBuilderStep<ReqT, RespT> extends
    MethodStubBuilder<ReqT, RespT>,
    HeadersMatcherBuilderStep<BidiStreamingMethodStubBuilderStep<ReqT, RespT>>,
    FirstRequestMatcherBuilderStep<BidiStreamingMethodStubBuilderStep<ReqT, RespT>, ReqT>,
    StreamRequestProxyResponseBuilderStep<NextBidiStreamingMethodStubBuilderStep<ReqT, RespT>, ReqT, RespT> {

}
