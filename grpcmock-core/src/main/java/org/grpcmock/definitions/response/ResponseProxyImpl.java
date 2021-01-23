package org.grpcmock.definitions.response;

import io.grpc.stub.StreamObserver;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 * @author Fadelis
 */
public class ResponseProxyImpl<ReqT, RespT> implements Response<ReqT, RespT> {

  private final AtomicInteger counter = new AtomicInteger();
  private final BiConsumer<ReqT, StreamObserver<RespT>> singleRequestResponse;
  private final Function<StreamObserver<RespT>, StreamObserver<ReqT>> streamRequestResponse;

  public ResponseProxyImpl(@Nonnull BiConsumer<ReqT, StreamObserver<RespT>> singleRequestResponse) {
    Objects.requireNonNull(singleRequestResponse);
    this.singleRequestResponse = singleRequestResponse;
    this.streamRequestResponse = null;
  }

  public ResponseProxyImpl(@Nonnull Function<StreamObserver<RespT>, StreamObserver<ReqT>> streamRequestResponse) {
    Objects.requireNonNull(streamRequestResponse);
    this.singleRequestResponse = null;
    this.streamRequestResponse = streamRequestResponse;
  }

  @Override
  public void execute(ReqT request, StreamObserver<RespT> responseObserver) {
    if (singleRequestResponse == null) {
      throw new UnsupportedOperationException("Unsupported single request call");
    }
    counter.incrementAndGet();
    singleRequestResponse.accept(request, responseObserver);
  }

  @Override
  public StreamObserver<ReqT> execute(StreamObserver<RespT> streamObserver) {
    if (streamRequestResponse == null) {
      throw new UnsupportedOperationException("Unsupported stream request call");
    }
    counter.incrementAndGet();
    return streamRequestResponse.apply(streamObserver);
  }

  @Override
  public int timesCalled() {
    return counter.get();
  }
}
