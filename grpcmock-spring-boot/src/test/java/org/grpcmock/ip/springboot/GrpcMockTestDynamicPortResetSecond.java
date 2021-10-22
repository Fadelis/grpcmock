package org.grpcmock.ip.springboot;

import static org.grpcmock.ip.GrpcMock.response;
import static org.grpcmock.ip.GrpcMock.stubFor;
import static org.grpcmock.ip.GrpcMock.unaryMethod;

import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.health.v1.HealthGrpc;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * @author Fadelis
 */
@SpringJUnitConfig
@SpringBootTest(classes = TestApplication.class, webEnvironment = WebEnvironment.NONE)
@AutoConfigureGrpcMock
class GrpcMockTestDynamicPortResetSecond extends TestBase {

  @Test
  void should_reset_mappings_for_dynamic_port_test1() {
    HealthCheckResponse response = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.SERVING)
        .build();
    HealthCheckRequest request = HealthCheckRequest.getDefaultInstance();

    stubFor(unaryMethod(HealthGrpc.getCheckMethod())
        .willReturn(response(response)));

    runAndAssertHealthCheckRequest(request, response);
  }
}