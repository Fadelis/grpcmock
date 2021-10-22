package org.grpcmock.ip.definitions.stub.steps;

import org.grpcmock.ip.definitions.BuilderStep;
import org.grpcmock.ip.definitions.stub.MethodStub;

/**
 * @author Fadelis
 */
public interface MethodStubBuilder<ReqT, RespT> extends BuilderStep {

  MethodStub<ReqT, RespT> build();
}
