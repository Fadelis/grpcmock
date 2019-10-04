package org.grpcmock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.grpcmock.GrpcMock.response;
import static org.grpcmock.GrpcMock.statusException;
import static org.grpcmock.GrpcMock.stubFor;
import static org.grpcmock.GrpcMock.unaryMethod;

import io.grpc.Status;
import io.grpc.testing.protobuf.SimpleRequest;
import io.grpc.testing.protobuf.SimpleResponse;
import io.grpc.testing.protobuf.SimpleServiceGrpc;
import io.grpc.testing.protobuf.SimpleServiceGrpc.SimpleServiceBlockingStub;
import org.junit.jupiter.api.Test;

/**
 * @author Fadelis
 */
class GrpcMockUnaryMethodTest extends TestBase {

  @Test
  void should_return_a_unary_response() {
    SimpleResponse expected = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
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

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(expected)
            .withFixedDelay(200)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected);
    assertThat(System.currentTimeMillis() - start).isGreaterThan(200);
  }

  @Test
  void should_respond_with_error_status() {
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(statusException(Status.ALREADY_EXISTS.withDescription("some error"))));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThatThrownBy(() -> serviceStub.unaryRpc(request))
        .hasMessage("ALREADY_EXISTS: some error");
  }

  @Test
  void should_respond_with_error_status_with_a_delay() {
    long start = System.currentTimeMillis();
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(statusException(Status.ALREADY_EXISTS.withDescription("some error"))
            .withFixedDelay(200)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThatThrownBy(() -> serviceStub.unaryRpc(request))
        .hasMessage("ALREADY_EXISTS: some error");
    assertThat(System.currentTimeMillis() - start).isGreaterThan(200);
  }

  @Test
  void should_return_a_unary_response_for_server_streaming_request() {
    SimpleResponse expected = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(response(expected)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.serverStreamingRpc(request)).toIterable().containsExactly(expected);
  }

  @Test
  void should_register_multiple_method_stub_for_the_same_service() {
    SimpleResponse expected1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();
    SimpleResponse expected2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-2")
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(expected1)));
    stubFor(unaryMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(response(expected2)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected1);
    assertThat(serviceStub.serverStreamingRpc(request)).toIterable().containsExactly(expected2);
  }

  @Test
  void should_return_a_response_when_request_satisfies_defined_matching_condition() {
    SimpleRequest matchRequest = SimpleRequest.newBuilder()
        .setRequestMessage("message-1")
        .build();
    SimpleResponse expected = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
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

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
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

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
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

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
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

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
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

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(expected1))
        .nextWillReturn(statusException(Status.INTERNAL))
        .nextWillReturn(response(expected2)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected1);
    assertThatThrownBy(() -> serviceStub.unaryRpc(request)).hasMessage("INTERNAL");
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected1);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected1);
  }

  @Test
  void should_register_multiple_same_method_scenarios_with_different_matching_conditions() {
    SimpleRequest request1 = SimpleRequest.newBuilder()
        .setRequestMessage("message-1")
        .build();
    SimpleRequest request2 = SimpleRequest.newBuilder()
        .setRequestMessage("message-2")
        .build();
    SimpleResponse expected1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();
    SimpleResponse expected2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-2")
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .withRequest(request1)
        .willReturn(response(expected1)));
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .withRequest(request2)
        .willReturn(response(expected2)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request1)).isEqualTo(expected1);
    assertThat(serviceStub.unaryRpc(request2)).isEqualTo(expected2);
  }

  @Test
  void should_last_register_method_scenario_should_be_triggered_when_multiple_matches_available() {
    SimpleResponse expected1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();
    SimpleResponse expected2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-2")
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(expected1)));
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(expected2)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    // check multiple times
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected2);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(expected2);
  }
}