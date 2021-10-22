package org.grpcmock.ip.definitions.response;

import io.grpc.stub.StreamObserver;

/**
 * @author Fadelis
 */
public interface Response<ReqT, RespT> extends Traceable {

  void execute(ReqT request, StreamObserver<RespT> streamObserver);

  StreamObserver<ReqT> execute(StreamObserver<RespT> streamObserver);
}
