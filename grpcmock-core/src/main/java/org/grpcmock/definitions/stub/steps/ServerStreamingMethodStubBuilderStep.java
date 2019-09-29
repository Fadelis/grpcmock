package org.grpcmock.definitions.stub.steps;

/**
 * @author Fadelis
 */
public interface ServerStreamingMethodStubBuilderStep<ReqT, RespT> extends
    MappingStubBuilder,
    HeadersMatcherBuilderStep<ServerStreamingMethodStubBuilderStep<ReqT, RespT>>,
    RequestMatcherBuilderStep<ServerStreamingMethodStubBuilderStep<ReqT, RespT>, ReqT>,
    SingleResponseBuilderStep<NextStreamResponseBuilderStep<ReqT, RespT>, RespT>,
    StreamResponseBuilderStep<ReqT, RespT> {

}
