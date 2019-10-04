package org.grpcmock.definitions.stub.steps;

import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.stub.MethodStub;

/**
 * @author Fadelis
 */
public interface MethodStubBuilder<ReqT, RespT> extends BuilderStep {

  MethodStub<ReqT, RespT> build();
}
