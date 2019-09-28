package org.grpcmock.junit5.test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.grpcmock.junit5.GrpcMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * @author Fadelis
 */
class ConfiguredExtensionTest extends TestBase {

  private static final String MY_HEADER = "my-header";
  private static final String HEADER_VALUE = "header-value";

  @RegisterExtension
  static GrpcMockExtension grpcMockExtension = GrpcMockExtension.builder()
      .withPort(0)
      .withInterceptor(new MyServerInterceptor())
      .build();

  @Test
  void should_test_default_extension_resetting_after_test_first() {
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

  @Test
  void should_test_default_extension_resetting_after_test_second() {
    HealthCheckResponse response = HealthCheckResponse.getDefaultInstance();
    HealthCheckRequest request = HealthCheckRequest.getDefaultInstance();

    assertThatThrownBy(() -> runAndAssertHealthCheckRequest(request, response))
        .hasMessage("UNIMPLEMENTED: Method not found: grpc.health.v1.Health/Check");
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
