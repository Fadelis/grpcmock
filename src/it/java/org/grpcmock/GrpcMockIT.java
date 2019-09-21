package org.grpcmock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.grpcmock.GrpcMock.getGlobalPort;
import static org.grpcmock.GrpcMock.grpcMock;
import static org.grpcmock.GrpcMock.response;
import static org.grpcmock.GrpcMock.service;
import static org.grpcmock.GrpcMock.statusException;
import static org.grpcmock.GrpcMock.stubFor;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.health.v1.HealthGrpc.HealthBlockingStub;
import java.io.IOException;
import java.net.ServerSocket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GrpcMockIT {

  private final ManagedChannel serverChannel = ManagedChannelBuilder
      .forAddress("localhost", getGlobalPort())
      .usePlaintext()
      .build();
  private final HealthCheckRequest request = HealthCheckRequest.getDefaultInstance();

  @BeforeAll
  static void createServer() {
    GrpcMock.configureFor(grpcMock(findFreePort()).build().start());
  }

  @BeforeEach
  void setup() {
    GrpcMock.resetMappings();
  }

  @Test
  void should_return_a_unary_response() {
    HealthCheckResponse expected = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.NOT_SERVING)
        .build();

    stubFor(service(HealthGrpc.getServiceDescriptor())
        .forMethod(HealthGrpc.getCheckMethod())
        .willReturn(response(expected)));

    HealthBlockingStub serviceStub = HealthGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.check(request)).isEqualTo(expected);
  }

  @Test
  void should_respond_with_error_status() {
    stubFor(service(HealthGrpc.getServiceDescriptor())
        .forMethod(HealthGrpc.getCheckMethod())
        .willReturn(statusException(Status.ALREADY_EXISTS.withDescription("some error"))));

    HealthBlockingStub serviceStub = HealthGrpc.newBlockingStub(serverChannel);

    assertThatThrownBy(() -> serviceStub.check(request)).hasMessage("ALREADY_EXISTS: some error");
  }

  @Test
  void should_return_a_unimplemented_error_if_no_responses_defined() {
    stubFor(service(HealthGrpc.getServiceDescriptor())
        .forMethod(HealthGrpc.getCheckMethod()));

    HealthBlockingStub serviceStub = HealthGrpc.newBlockingStub(serverChannel);

    assertThatThrownBy(() -> serviceStub.check(request))
        .hasMessage("UNIMPLEMENTED: No response for the stub was found");
  }

  @Test
  void should_return_a_unary_response_when_creating_service_via_name() {
    HealthCheckResponse expected = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.NOT_SERVING)
        .build();

    stubFor(service(HealthGrpc.SERVICE_NAME)
        .forMethod(HealthGrpc.getCheckMethod())
        .willReturn(response(expected)));

    HealthBlockingStub serviceStub = HealthGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.check(request)).isEqualTo(expected);
  }

  @Test
  void should_return_a_unary_response_for_server_streaming_request() {
    HealthCheckResponse expected = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.NOT_SERVING)
        .build();

    stubFor(service(HealthGrpc.SERVICE_NAME)
        .forMethod(HealthGrpc.getWatchMethod())
        .willReturn(response(expected)));

    HealthBlockingStub serviceStub = HealthGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.watch(request)).toIterable().containsExactly(expected);
  }

  @Test
  void should_return_multiple_unary_responses_for_multiple_requests() {
    HealthCheckResponse expected1 = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.NOT_SERVING)
        .build();
    HealthCheckResponse expected2 = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.NOT_SERVING)
        .build();

    stubFor(service(HealthGrpc.SERVICE_NAME)
        .forMethod(HealthGrpc.getCheckMethod())
        .willReturn(response(expected1))
        .nextWillReturn(response(expected2)));

    HealthBlockingStub serviceStub = HealthGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.check(request)).isEqualTo(expected1);
    assertThat(serviceStub.check(request)).isEqualTo(expected2);
    assertThat(serviceStub.check(request)).isEqualTo(expected2);
  }

  @Test
  void should_return_multiple_unary_object_or_error_responses_for_multiple_requests() {
    HealthCheckResponse expected1 = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.NOT_SERVING)
        .build();
    HealthCheckResponse expected2 = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.NOT_SERVING)
        .build();

    stubFor(service(HealthGrpc.SERVICE_NAME)
        .forMethod(HealthGrpc.getCheckMethod())
        .willReturn(response(expected1))
        .nextWillReturn(statusException(Status.INTERNAL))
        .nextWillReturn(response(expected2)));

    HealthBlockingStub serviceStub = HealthGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.check(request)).isEqualTo(expected1);
    assertThatThrownBy(() -> serviceStub.check(request)).hasMessage("INTERNAL");
    assertThat(serviceStub.check(request)).isEqualTo(expected1);
    assertThat(serviceStub.check(request)).isEqualTo(expected1);
  }

  private static int findFreePort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      socket.setReuseAddress(true);
      return socket.getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException("Failed finding free port", e);
    }
  }
}