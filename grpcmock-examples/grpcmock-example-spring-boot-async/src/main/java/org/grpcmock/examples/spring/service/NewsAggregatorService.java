package org.grpcmock.examples.spring.service;

import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import org.grpcmock.examples.v1.GetNewsRequest;
import org.grpcmock.examples.v1.GetNewsResponse;
import org.grpcmock.examples.v1.NewsAggregatorServiceGrpc.NewsAggregatorServiceImplBase;
import org.lognet.springboot.grpc.GRpcService;

/**
 * @author Fadelis
 */
@GRpcService
public class NewsAggregatorService extends NewsAggregatorServiceImplBase {

  private final List<DownstreamNewsService> downstreamServices;

  public NewsAggregatorService(List<DownstreamNewsService> downstreamServices) {
    this.downstreamServices = downstreamServices;
  }

  @Override
  public void getAggregatedNews(GetNewsRequest request, StreamObserver<GetNewsResponse> responseObserver) {
    StreamObserver<GetNewsResponse> synchronizedResponseObserver = synchronizedStreamObserver(responseObserver);
    CompletableFuture[] newsFutures = downstreamServices.stream()
        .map(downstreamNewsService -> downstreamNewsService.getNews(request)
            .thenAccept(synchronizedResponseObserver::onNext))
        .toArray(CompletableFuture[]::new);

    CompletableFuture.allOf(newsFutures)
        .whenComplete((ignore, exception) -> synchronizedResponseObserver.onCompleted());
  }

  /**
   * @return A wrapped synchronized stream observer, which can safely handle multiple concurrent writes to it.
   */
  public static <T> StreamObserver<T> synchronizedStreamObserver(StreamObserver<T> delegate) {
    ReentrantLock lock = new ReentrantLock();
    return new StreamObserver<T>() {
      @Override
      public void onNext(T value) {
        lock.lock();
        try {
          delegate.onNext(value);
        } finally {
          lock.unlock();
        }
      }

      @Override
      public void onError(Throwable error) {
        delegate.onError(error);
      }

      @Override
      public void onCompleted() {
        delegate.onCompleted();
      }
    };
  }
}
