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
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.health.v1.HealthGrpc.HealthBlockingStub;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.MetadataUtils;
import java.io.IOException;
import java.net.ServerSocket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GrpcMockIT {

  private static final String HEADER_1 = "header-1";
  private static final String HEADER_2 = "header-2";
  private static final String HEADER_UNKNOWN = "header-unknown";

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
  void should_return_a_response_when_request_satisfies_defined_matching_condition() {
    HealthCheckRequest matchRequest = HealthCheckRequest.newBuilder()
        .setService("service-1")
        .build();
    HealthCheckResponse expected = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.NOT_SERVING)
        .build();

    stubFor(service(HealthGrpc.SERVICE_NAME)
        .forMethod(HealthGrpc.getCheckMethod())
        .withRequest(matchRequest)
        .willReturn(response(expected)));

    HealthBlockingStub serviceStub = HealthGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.check(matchRequest)).isEqualTo(expected);
  }

  @Test
  void should_not_return_a_response_when_request_does_not_satisfy_matching_condition() {
    HealthCheckRequest matchRequest = HealthCheckRequest.newBuilder()
        .setService("service-1")
        .build();
    HealthCheckResponse expected = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.NOT_SERVING)
        .build();

    stubFor(service(HealthGrpc.SERVICE_NAME)
        .forMethod(HealthGrpc.getCheckMethod())
        .withRequest(matchRequest)
        .willReturn(response(expected)));

    HealthBlockingStub serviceStub = HealthGrpc.newBlockingStub(serverChannel);

    assertThatThrownBy(() -> serviceStub.check(request)).hasMessageStartingWith("UNIMPLEMENTED");
  }

  @Test
  void should_return_a_response_when_headers_satisfies_defined_matching_condition() {
    HealthCheckResponse expected = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.NOT_SERVING)
        .build();

    stubFor(service(HealthGrpc.SERVICE_NAME)
        .forMethod(HealthGrpc.getCheckMethod())
        .withHeader(HEADER_1, "value-1")
        .withHeader(HEADER_2, value -> value.startsWith("value"))
        .willReturn(response(expected)));

    HealthBlockingStub serviceStub = stubWithHeaders(
        HealthGrpc.newBlockingStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2"
    );

    assertThat(serviceStub.check(request)).isEqualTo(expected);
  }

  @Test
  void should_not_return_a_response_when_headers_does_not_satisfy_matching_condition() {
    HealthCheckResponse expected = HealthCheckResponse.newBuilder()
        .setStatus(ServingStatus.NOT_SERVING)
        .build();

    stubFor(service(HealthGrpc.SERVICE_NAME)
        .forMethod(HealthGrpc.getCheckMethod())
        .withoutHeader(HEADER_2)
        .willReturn(response(expected)));

    HealthBlockingStub serviceStub = stubWithHeaders(
        HealthGrpc.newBlockingStub(serverChannel),
        HEADER_1, "value-2",
        HEADER_2, "value-2"
    );

    assertThatThrownBy(() -> serviceStub.check(request)).hasMessageStartingWith("UNIMPLEMENTED");
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

  protected <T extends AbstractStub<T>> T stubWithHeaders(
      T baseStub,
      String headerName1, String headerValue1,
      String headerName2, String headerValue2
  ) {
    Metadata metadata = new Metadata();
    metadata.put(Metadata.Key.of(headerName1, Metadata.ASCII_STRING_MARSHALLER), headerValue1);
    metadata.put(Metadata.Key.of(headerName2, Metadata.ASCII_STRING_MARSHALLER), headerValue2);

    return MetadataUtils.attachHeaders(baseStub, metadata);
  }
}