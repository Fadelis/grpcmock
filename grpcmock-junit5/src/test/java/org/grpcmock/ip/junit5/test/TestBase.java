package org.grpcmock.ip.junit5.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.grpcmock.ip.GrpcMock.response;
import static org.grpcmock.ip.GrpcMock.stubFor;
import static org.grpcmock.ip.GrpcMock.unaryMethod;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.health.v1.HealthGrpc.HealthBlockingStub;
import org.grpcmock.ip.GrpcMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Fadelis
 */
abstract class TestBase {

  private ManagedChannel serverChannel;

  @BeforeEach
  void setupChannel() {
    serverChannel = ManagedChannelBuilder.forAddress("localhost", GrpcMock.getGlobalPort())
        .usePlaintext()
        .build();
  }

  @AfterEach
  void tearDownChannel() {
    serverChannel.shutdownNow();
  }

  void runAndAssertHealthCheckRequest(HealthCheckRequest request, HealthCheckResponse response) {
    HealthBlockingStub serviceStub = HealthGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.check(HealthCheckRequest.getDefaultInstance())).isEqualTo(response);
  }

  void runAndAssertSimpleHealthCheckRequest() {
    HealthCheckResponse response = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.SERVING)
        .build();
    HealthCheckRequest request = HealthCheckRequest.getDefaultInstance();

    stubFor(unaryMethod(HealthGrpc.getCheckMethod())
        .willReturn(response(response)));

    runAndAssertHealthCheckRequest(request, response);
  }
}
