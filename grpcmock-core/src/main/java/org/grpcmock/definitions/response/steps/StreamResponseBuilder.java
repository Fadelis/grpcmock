package org.grpcmock.definitions.response.steps;

import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.Response;

/**
 * @author Fadelis
 */
public interface StreamResponseBuilder<RespT> extends BuilderStep {

  <ReqT> Response<ReqT, RespT> build();
}
