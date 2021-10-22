package org.grpcmock.ip.definitions.response.steps;

import org.grpcmock.ip.definitions.BuilderStep;
import org.grpcmock.ip.definitions.response.Response;

/**
 * @author Fadelis
 */
public interface StreamResponseBuilder<RespT> extends BuilderStep {

  <ReqT> Response<ReqT, RespT> build();
}
