package org.grpcmock.examples.spring.service;

import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
    CompletableFuture[] newsFutures = downstreamServices.stream()
        .map(downstreamNewsService -> downstreamNewsService.getNews(request)
            .thenAccept(responseObserver::onNext))
        .toArray(CompletableFuture[]::new);

    CompletableFuture.allOf(newsFutures)
        .whenComplete((ignore, exception) -> responseObserver.onCompleted());
  }
}
