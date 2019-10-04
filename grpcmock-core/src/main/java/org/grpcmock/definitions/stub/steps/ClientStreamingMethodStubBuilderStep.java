package org.grpcmock.definitions.stub.steps;

import org.grpcmock.definitions.matcher.steps.HeadersMatcherBuilderStep;

/**
 * @author Fadelis
 */
public interface ClientStreamingMethodStubBuilderStep<ReqT, RespT> extends
    MethodStubBuilder,
    HeadersMatcherBuilderStep<ClientStreamingMethodStubBuilderStep<ReqT, RespT>> {

}
