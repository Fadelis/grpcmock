package org.grpcmock.definitions.stub.steps;

import org.grpcmock.definitions.matcher.steps.HeadersMatcherBuilderStep;
import org.grpcmock.definitions.matcher.steps.RequestMatcherBuilderStep;

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
