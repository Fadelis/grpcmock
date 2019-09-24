package org.grpcmock.definitions.stub.steps;

import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.stub.ServiceStub;

public interface MappingStubBuilder extends BuilderStep {

  ServiceStub build();
}
