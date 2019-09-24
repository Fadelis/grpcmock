package org.grpcmock.definitions.response;

import io.grpc.stub.StreamObserver;

/**
 * @author Fadelis
 */
public interface Response<ReqT, RespT> extends Traceable {

  void execute(ReqT request, StreamObserver<RespT> streamObserver);
}
