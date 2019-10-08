package org.grpcmock.definitions.stub.steps;

import org.grpcmock.definitions.matcher.steps.HeadersMatcherBuilderStep;

/**
 * @author Fadelis
 */
public interface BidiStreamingMethodStubBuilderStep<ReqT, RespT> extends
    MethodStubBuilder<ReqT, RespT>,
    HeadersMatcherBuilderStep<BidiStreamingMethodStubBuilderStep<ReqT, RespT>> {

}
