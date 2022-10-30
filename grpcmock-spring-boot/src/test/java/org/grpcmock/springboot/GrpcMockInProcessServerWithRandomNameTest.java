package org.grpcmock.springboot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.grpcmock.GrpcMock.response;
import static org.grpcmock.GrpcMock.stubFor;
import static org.grpcmock.GrpcMock.unaryMethod;

import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.health.v1.HealthGrpc;
import java.util.UUID;
import org.grpcmock.GrpcMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

/**
 * @author Fadelis
 */
@SpringBootTest(classes = TestApplication.class, webEnvironment = WebEnvironment.NONE)
@AutoConfigureGrpcMock(useInProcessServer = true)
class GrpcMockInProcessServerWithRandomNameTest extends TestBase {

  @Autowired
  private GrpcMock grpcMock;

  @Test
  void should_reset_mappings_for_dynamic_port_test1() {
    HealthCheckResponse response = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.NOT_SERVING)
        .build();
    HealthCheckRequest request = HealthCheckRequest.getDefaultInstance();

    stubFor(unaryMethod(HealthGrpc.getCheckMethod())
        .willReturn(response(response)));

    runAndAssertHealthCheckRequest(request, response);
  }

  @Test
  void should_create_a_correct_bean() {
    assertThat(grpcMock).isNotNull();
    assertThat(grpcMock.getInProcessName()).satisfies(UUID::fromString);
  }
}
