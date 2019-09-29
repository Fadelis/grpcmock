package org.grpcmock.definitions.stub.steps;

/**
 * @author Fadelis
 */
public interface UnaryMethodStubBuilderStep<ReqT, RespT> extends
    MappingStubBuilder,
    HeadersMatcherBuilderStep<UnaryMethodStubBuilderStep<ReqT, RespT>>,
    RequestMatcherBuilderStep<UnaryMethodStubBuilderStep<ReqT, RespT>, ReqT>,
    SingleResponseBuilderStep<NextSingleResponseBuilderStep<ReqT, RespT>, RespT> {

}
