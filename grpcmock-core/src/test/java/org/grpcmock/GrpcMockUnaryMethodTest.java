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
import io.grpc.stub.AbstractStub;
import io.grpc.stub.MetadataUtils;
import io.grpc.testing.protobuf.SimpleRequest;
import io.grpc.testing.protobuf.SimpleResponse;
import io.grpc.testing.protobuf.SimpleServiceGrpc;
import io.grpc.testing.protobuf.SimpleServiceGrpc.SimpleServiceBlockingStub;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Fadelis
 */
class GrpcMockUnaryMethodTest {

  private static final String HEADER_1 = "header-1";
  private static final String HEADER_2 = "header-2";

  private final ManagedChannel serverChannel = ManagedChannelBuilder
      .forAddress("localhost", getGlobalPort())
      .usePlaintext()
      .build();
  private final SimpleRequest request = SimpleRequest.getDefaultInstance();

  @BeforeAll
  static void createServer() {
    GrpcMock.configureFor(grpcMock(0).build().start());
  }

  @BeforeEach
  void setup() {
    GrpcMock.resetMappings();
  }

  @Test
  void should_return_a_unary_response() {
    SimpleResponse expected = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(service(SimpleServiceGrpc.getServiceDescriptor())
        .forMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(expected)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected);
  }

  @Test
  void should_return_a_unary_response_with_a_delay() {
    long start = System.currentTimeMillis();
    SimpleResponse expected = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(service(SimpleServiceGrpc.getServiceDescriptor())
        .forMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(expected)
            .withFixedDelay(200)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected);
    assertThat(System.currentTimeMillis() - start).isGreaterThan(200);
  }

  @Test
  void should_respond_with_error_status() {
    stubFor(service(SimpleServiceGrpc.getServiceDescriptor())
        .forMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(statusException(Status.ALREADY_EXISTS.withDescription("some error"))));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThatThrownBy(() -> serviceStub.unaryRpc(request))
        .hasMessage("ALREADY_EXISTS: some error");
  }

  @Test
  void should_respond_with_error_status_with_a_delay() {
    long start = System.currentTimeMillis();
    stubFor(service(SimpleServiceGrpc.getServiceDescriptor())
        .forMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(statusException(Status.ALREADY_EXISTS.withDescription("some error"))
            .withFixedDelay(200)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThatThrownBy(() -> serviceStub.unaryRpc(request))
        .hasMessage("ALREADY_EXISTS: some error");
    assertThat(System.currentTimeMillis() - start).isGreaterThan(200);
  }

  @Test
  void should_return_a_unary_response_when_creating_service_via_name() {
    SimpleResponse expected = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(expected)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected);
  }

  @Test
  void should_return_a_unary_response_for_server_streaming_request() {
    SimpleResponse expected = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(response(expected)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.serverStreamingRpc(request)).toIterable().containsExactly(expected);
  }

  @Test
  @Disabled("no implemented yet")
  void should_register_multiple_method_stub_for_the_same_service() {
    SimpleResponse expected1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();
    SimpleResponse expected2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-2")
        .build();

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(expected1)));
    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(response(expected2)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected1);
    assertThat(serviceStub.serverStreamingRpc(request)).toIterable().containsExactly(expected1);
  }

  @Test
  void should_return_a_response_when_request_satisfies_defined_matching_condition() {
    SimpleRequest matchRequest = SimpleRequest.newBuilder()
        .setRequestMessage("message-1")
        .build();
    SimpleResponse expected = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .withRequest(matchRequest)
        .willReturn(response(expected)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(matchRequest)).isEqualTo(expected);
  }

  @Test
  void should_not_return_a_response_when_request_does_not_satisfy_matching_condition() {
    SimpleRequest matchRequest = SimpleRequest.newBuilder()
        .setRequestMessage("message-1")
        .build();
    SimpleResponse expected = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .withRequest(matchRequest)
        .willReturn(response(expected)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThatThrownBy(() -> serviceStub.unaryRpc(request)).hasMessageStartingWith("UNIMPLEMENTED");
  }

  @Test
  void should_return_a_response_when_headers_satisfies_defined_matching_condition() {
    SimpleResponse expected = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .withHeader(HEADER_1, "value-1")
        .withHeader(HEADER_2, value -> value.startsWith("value"))
        .willReturn(response(expected)));

    SimpleServiceBlockingStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newBlockingStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2"
    );

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected);
  }

  @Test
  void should_not_return_a_response_when_headers_does_not_satisfy_matching_condition() {
    SimpleResponse expected = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .withoutHeader(HEADER_2)
        .willReturn(response(expected)));

    SimpleServiceBlockingStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newBlockingStub(serverChannel),
        HEADER_1, "value-2",
        HEADER_2, "value-2"
    );

    assertThatThrownBy(() -> serviceStub.unaryRpc(request)).hasMessageStartingWith("UNIMPLEMENTED");
  }

  @Test
  void should_return_multiple_unary_responses_for_multiple_requests() {
    SimpleResponse expected1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();
    SimpleResponse expected2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(expected1))
        .nextWillReturn(response(expected2)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected1);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected2);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected2);
  }

  @Test
  void should_return_multiple_unary_object_or_error_responses_for_multiple_requests() {
    SimpleResponse expected1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();
    SimpleResponse expected2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(expected1))
        .nextWillReturn(statusException(Status.INTERNAL))
        .nextWillReturn(response(expected2)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected1);
    assertThatThrownBy(() -> serviceStub.unaryRpc(request)).hasMessage("INTERNAL");
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected1);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected1);
  }

  private <T extends AbstractStub<T>> T stubWithHeaders(
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