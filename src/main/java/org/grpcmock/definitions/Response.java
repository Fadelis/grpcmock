package org.grpcmock.definitions;

import io.grpc.stub.StreamObserver;

public interface Response<ReqT, RespT> extends Traceable {

  void execute(ReqT request, StreamObserver<RespT> streamObserver);
}
