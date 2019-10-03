package org.grpcmock.definitions.stub.steps;

import org.grpcmock.definitions.matcher.steps.HeadersMatcherBuilderStep;

/**
 * @author Fadelis
 */
public interface ClientStreamingMethodStubBuilderStep<ReqT, RespT> extends
    MappingStubBuilder,
    HeadersMatcherBuilderStep<ClientStreamingMethodStubBuilderStep<ReqT, RespT>> {

}
