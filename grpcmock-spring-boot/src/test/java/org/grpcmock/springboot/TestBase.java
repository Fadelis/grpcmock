package org.grpcmock.springboot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.grpcmock.GrpcMock.response;
import static org.grpcmock.GrpcMock.stubFor;
import static org.grpcmock.GrpcMock.unaryMethod;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.health.v1.HealthGrpc.HealthBlockingStub;
import io.grpc.inprocess.InProcessChannelBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

/**
 * @author Fadelis
 */
abstract class TestBase {

  @Value("${grpcmock.server.port}")
  protected int grpcMockPort;

  @Value("${grpcmock.server.name}")
  private String inProcessName;

  private ManagedChannel serverChannel;

  @BeforeEach
  void setupChannel() {
    if (StringUtils.hasText(inProcessName)) {
      serverChannel = InProcessChannelBuilder.forName(inProcessName).build();
    } else {
      serverChannel = ManagedChannelBuilder.forAddress("localhost", grpcMockPort)
          .usePlaintext()
          .build();
    }
  }

  @AfterEach
  void tearDownChannel() {
    serverChannel.shutdownNow();
  }

  void runAndAssertHealthCheckRequest(HealthCheckRequest request, HealthCheckResponse response) {
    HealthBlockingStub serviceStub = HealthGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.check(HealthCheckRequest.getDefaultInstance())).isEqualTo(response);
  }

  void simpleHealthCheckRequest() {
    HealthCheckResponse response = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.SERVING)
        .build();
    HealthCheckRequest request = HealthCheckRequest.getDefaultInstance();

    stubFor(unaryMethod(HealthGrpc.getCheckMethod())
        .willReturn(response(response)));

    runAndAssertHealthCheckRequest(request, response);
  }
}
