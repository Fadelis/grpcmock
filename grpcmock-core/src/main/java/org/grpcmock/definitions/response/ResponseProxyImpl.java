package org.grpcmock.definitions.response;

import io.grpc.stub.StreamObserver;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;

/**
 * @author Fadelis
 */
public class ResponseProxyImpl<ReqT, RespT> implements Response<ReqT, RespT> {

  private final AtomicInteger counter = new AtomicInteger();
  private final BiConsumer<ReqT, StreamObserver<RespT>> response;

  public ResponseProxyImpl(@Nonnull BiConsumer<ReqT, StreamObserver<RespT>> response) {
    Objects.requireNonNull(response);
    this.response = response;
  }

  @Override
  public void execute(ReqT request, StreamObserver<RespT> responseObserver) {
    counter.incrementAndGet();
    response.accept(request, responseObserver);
  }

  @Override
  public int timesCalled() {
    return counter.get();
  }
}
