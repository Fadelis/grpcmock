package org.grpcmock.springboot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.grpcmock.GrpcMock.response;
import static org.grpcmock.GrpcMock.stubFor;
import static org.grpcmock.GrpcMock.unaryMethod;

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
class GrpcMockTestDynamicPortResetSecond extends TestBase {

  @Test
  void should_reset_mappings_for_dynamic_port_test1() {
    HealthCheckResponse response = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.SERVING)
        .build();

    stubFor(unaryMethod(HealthGrpc.getCheckMethod())
        .willReturn(response(response)));

    runAndAssertHealthCheckRequest(response);

    assertThat(grpcMockPort)
        .as("Port should be assigned by OS (should not be 0)")
        .isGreaterThan(0)
        .isLessThan(65536);

    assertThat(grpcMock.getPort())
        .as("Server's actual port should match injected port property")
        .isEqualTo(grpcMockPort);
  }
}
