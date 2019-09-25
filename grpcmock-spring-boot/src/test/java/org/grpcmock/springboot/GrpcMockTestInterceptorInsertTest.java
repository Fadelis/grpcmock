package org.grpcmock.springboot;

import static org.grpcmock.GrpcMock.response;
import static org.grpcmock.GrpcMock.service;
import static org.grpcmock.GrpcMock.stubFor;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.health.v1.HealthGrpc;
import org.grpcmock.springboot.GrpcMockTestInterceptorInsertTest.MyServerInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * @author Fadelis
 */
@SpringJUnitConfig
@SpringBootTest(classes = TestApplication.class, webEnvironment = WebEnvironment.NONE)
@AutoConfigureGrpcMock(port = 0, interceptors = MyServerInterceptor.class)
class GrpcMockTestInterceptorInsertTest extends TestBase {

  private static final String MY_HEADER = "my-header";
  private static final String HEADER_VALUE = "header-value";

  @Test
  void should_use_configured_interceptor() {
    HealthCheckResponse response = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.SERVING)
        .build();
    HealthCheckRequest request = HealthCheckRequest.getDefaultInstance();

    stubFor(service(HealthGrpc.SERVICE_NAME)
        .forMethod(HealthGrpc.getCheckMethod())
        .withHeader(MY_HEADER, HEADER_VALUE)
        .willReturn(response(response)));

    runAndAssertHealthCheckRequest(request, response);
  }

  public static class MyServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call,
        Metadata headers,
        ServerCallHandler<ReqT, RespT> next
    ) {
      headers.put(Metadata.Key.of(MY_HEADER, Metadata.ASCII_STRING_MARSHALLER), HEADER_VALUE);
      return next.startCall(call, headers);
    }
  }
}