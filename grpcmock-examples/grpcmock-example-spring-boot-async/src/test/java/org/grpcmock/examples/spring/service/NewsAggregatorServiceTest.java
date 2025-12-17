package org.grpcmock.examples.spring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.grpcmock.GrpcMock.calledMethod;
import static org.grpcmock.GrpcMock.response;
import static org.grpcmock.GrpcMock.stubFor;
import static org.grpcmock.GrpcMock.times;
import static org.grpcmock.GrpcMock.unaryMethod;
import static org.grpcmock.GrpcMock.verifyThat;
import static org.grpcmock.examples.spring.interceptor.ApikeyInterceptor.APIKEY_HEADER;
import static org.grpcmock.examples.spring.interceptor.ApikeyInterceptor.AUTHORIZATION_VALUE_PREFIX;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.internal.testing.StreamRecorder;
import java.util.Optional;
import org.grpcmock.examples.spring.Application;
import org.grpcmock.examples.v1.GetNewsRequest;
import org.grpcmock.examples.v1.GetNewsResponse;
import org.grpcmock.examples.v1.NewsAggregatorServiceGrpc;
import org.grpcmock.examples.v1.NewsAggregatorServiceGrpc.NewsAggregatorServiceStub;
import org.grpcmock.examples.v1.NewsServiceGrpc;
import org.grpcmock.springboot.AutoConfigureGrpcMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.grpc.test.autoconfigure.LocalGrpcPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * @author Fadelis
 */
@SpringJUnitConfig
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
@AutoConfigureGrpcMock(executorThreadCount = 2)
@ActiveProfiles("test")
class NewsAggregatorServiceTest {

  @LocalGrpcPort
  private int grpcPort;

  private ManagedChannel serverChannel;

  @BeforeEach
  public final void setup() {
    serverChannel = ManagedChannelBuilder.forAddress("localhost", grpcPort)
        .usePlaintext()
        .build();
  }

  @AfterEach
  public final void cleanup() {
    Optional.ofNullable(serverChannel).ifPresent(ManagedChannel::shutdownNow);
  }

  @Test
  void should_return_aggregated_news_async() throws Exception {
    GetNewsRequest request = GetNewsRequest.newBuilder().setRelevantIndustry("tech").build();
    GetNewsResponse response1 = GetNewsResponse.newBuilder().setNewsContent("some interesting thing").build();
    GetNewsResponse response2 = GetNewsResponse.newBuilder().setNewsContent("some other interesting thing").build();

    stubFor(unaryMethod(NewsServiceGrpc.getGetNewsMethod())
        .withHeader(APIKEY_HEADER, AUTHORIZATION_VALUE_PREFIX + "apikey-1")
        .willReturn(response(response1).withFixedDelay(1000L)));

    stubFor(unaryMethod(NewsServiceGrpc.getGetNewsMethod())
        .withHeader(APIKEY_HEADER, AUTHORIZATION_VALUE_PREFIX + "apikey-2")
        .willReturn(response(response2)));

    StreamRecorder<GetNewsResponse> streamRecorder = StreamRecorder.create();
    NewsAggregatorServiceStub aggregatorServiceStub = NewsAggregatorServiceGrpc.newStub(serverChannel);
    aggregatorServiceStub.getAggregatedNews(request, streamRecorder);

    streamRecorder.awaitCompletion();
    assertThat(streamRecorder.getValues()).containsExactly(response2, response1);
    verifyThat(NewsServiceGrpc.getGetNewsMethod(), times(2));
    verifyThat(calledMethod(NewsServiceGrpc.getGetNewsMethod())
        .withHeader(APIKEY_HEADER, AUTHORIZATION_VALUE_PREFIX + "apikey-1"));
    verifyThat(calledMethod(NewsServiceGrpc.getGetNewsMethod())
        .withHeader(APIKEY_HEADER, AUTHORIZATION_VALUE_PREFIX + "apikey-2"));
  }

  @Test
  void should_not_fail_aggregating_if_one_service_fails() throws Exception {
    GetNewsRequest request = GetNewsRequest.newBuilder().setRelevantIndustry("tech").build();
    GetNewsResponse response2 = GetNewsResponse.newBuilder().setNewsContent("some other interesting thing").build();

    stubFor(unaryMethod(NewsServiceGrpc.getGetNewsMethod())
        .withHeader(APIKEY_HEADER, AUTHORIZATION_VALUE_PREFIX + "apikey-1")
        .willReturn(Status.UNAVAILABLE));

    stubFor(unaryMethod(NewsServiceGrpc.getGetNewsMethod())
        .withHeader(APIKEY_HEADER, AUTHORIZATION_VALUE_PREFIX + "apikey-2")
        .willReturn(response(response2).withFixedDelay(200L)));

    StreamRecorder<GetNewsResponse> streamRecorder = StreamRecorder.create();
    NewsAggregatorServiceStub aggregatorServiceStub = NewsAggregatorServiceGrpc.newStub(serverChannel);
    aggregatorServiceStub.getAggregatedNews(request, streamRecorder);

    streamRecorder.awaitCompletion();
    assertThat(streamRecorder.getValues()).containsExactly(response2);
    verifyThat(NewsServiceGrpc.getGetNewsMethod(), times(2));
    verifyThat(calledMethod(NewsServiceGrpc.getGetNewsMethod())
        .withHeader(APIKEY_HEADER, AUTHORIZATION_VALUE_PREFIX + "apikey-1"));
    verifyThat(calledMethod(NewsServiceGrpc.getGetNewsMethod())
        .withHeader(APIKEY_HEADER, AUTHORIZATION_VALUE_PREFIX + "apikey-2"));
  }
}