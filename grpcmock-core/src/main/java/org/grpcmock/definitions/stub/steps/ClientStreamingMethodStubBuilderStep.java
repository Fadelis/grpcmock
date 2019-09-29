package org.grpcmock.definitions.stub.steps;

/**
 * @author Fadelis
 */
public interface ClientStreamingMethodStubBuilderStep<ReqT, RespT> extends
    MappingStubBuilder,
    HeadersMatcherBuilderStep<ClientStreamingMethodStubBuilderStep<ReqT, RespT>> {

}
