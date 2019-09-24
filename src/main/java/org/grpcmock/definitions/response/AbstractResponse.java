package org.grpcmock.definitions.response;

import io.grpc.stub.StreamObserver;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Fadelis
 */
public abstract class AbstractResponse<ReqT, RespT> implements Response<ReqT, RespT> {

  private final AtomicInteger counter = new AtomicInteger();

  @Override
  public void execute(ReqT request, StreamObserver<RespT> streamObserver) {
    counter.incrementAndGet();
  }

  @Override
  public int timesCalled() {
    return counter.get();
  }
}
