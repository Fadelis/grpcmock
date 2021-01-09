package org.grpcmock.examples.spring.service;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.CompletableFuture;
import org.grpcmock.examples.v1.GetNewsRequest;
import org.grpcmock.examples.v1.GetNewsResponse;
import org.grpcmock.examples.v1.NewsServiceGrpc.NewsServiceFutureStub;

/**
 * @author Fadelis
 */
public class DownstreamNewsService {

  private final NewsServiceFutureStub futureStub;

  public DownstreamNewsService(NewsServiceFutureStub futureStub) {
    this.futureStub = futureStub;
  }

  public CompletableFuture<GetNewsResponse> getNews(GetNewsRequest request) {
    return toCompletableFuture(futureStub.getNews(request));
  }

  private static <T> CompletableFuture<T> toCompletableFuture(ListenableFuture<T> listenableFuture) {
    final CompletableFuture<T> completableFuture = new CompletableFuture<>();
    Futures.addCallback(listenableFuture, new FutureCallback<T>() {
      @Override
      public void onFailure(Throwable throwable) {
        completableFuture.completeExceptionally(throwable);
      }

      @Override
      public void onSuccess(T t) {
        completableFuture.complete(t);
      }
    }, MoreExecutors.directExecutor());
    return completableFuture;
  }
}
