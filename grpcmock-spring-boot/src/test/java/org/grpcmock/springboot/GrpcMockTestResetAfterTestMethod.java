package org.grpcmock.springboot;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.grpcmock.GrpcMock.response;
import static org.grpcmock.GrpcMock.stubFor;
import static org.grpcmock.GrpcMock.unaryMethod;

import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.health.v1.HealthGrpc;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

/**
 * @author Fadelis
 */
@SpringBootTest(classes = TestApplication.class, webEnvironment = WebEnvironment.NONE)
@AutoConfigureGrpcMock
class GrpcMockTestResetAfterTestMethod extends TestBase {

  @Test
  void should_respond_with_response() {
    HealthCheckResponse response = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.NOT_SERVING)
        .build();
    HealthCheckRequest request = HealthCheckRequest.getDefaultInstance();

    stubFor(unaryMethod(HealthGrpc.getCheckMethod())
        .willReturn(response(response)));

    runAndAssertHealthCheckRequest(request, response);
  }

  @Test
  void should_respond_with_unimplemented_error() {
    HealthCheckResponse response = HealthCheckResponse.getDefaultInstance();
    HealthCheckRequest request = HealthCheckRequest.getDefaultInstance();

    assertThatThrownBy(() -> runAndAssertHealthCheckRequest(request, response))
        .hasMessage("UNIMPLEMENTED: Method not found: grpc.health.v1.Health/Check");
  }
}
