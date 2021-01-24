package org.grpcmock.definitions.stub.steps;

/**
 * @author Fadelis
 */
public interface NextBidiStreamingMethodStubBuilderStep<ReqT, RespT> extends
    MethodStubBuilder<ReqT, RespT>,
    NextStreamRequestProxyResponseBuilderStep<NextBidiStreamingMethodStubBuilderStep<ReqT, RespT>, ReqT, RespT> {

}
