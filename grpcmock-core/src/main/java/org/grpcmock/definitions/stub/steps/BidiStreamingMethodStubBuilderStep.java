package org.grpcmock.definitions.stub.steps;

/**
 * @author Fadelis
 */
public interface BidiStreamingMethodStubBuilderStep<ReqT, RespT> extends
    MappingStubBuilder,
    HeadersMatcherBuilderStep<BidiStreamingMethodStubBuilderStep<ReqT, RespT>> {

}
