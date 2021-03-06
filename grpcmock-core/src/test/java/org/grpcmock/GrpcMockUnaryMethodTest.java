package org.grpcmock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.grpcmock.GrpcMock.response;
import static org.grpcmock.GrpcMock.statusException;
import static org.grpcmock.GrpcMock.stubFor;
import static org.grpcmock.GrpcMock.unaryMethod;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.protobuf.SimpleRequest;
import io.grpc.testing.protobuf.SimpleResponse;
import io.grpc.testing.protobuf.SimpleServiceGrpc;
import io.grpc.testing.protobuf.SimpleServiceGrpc.SimpleServiceBlockingStub;
import io.grpc.testing.protobuf.SimpleServiceGrpc.SimpleServiceImplBase;
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
    SimpleResponse response1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();
    SimpleResponse response2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-2")
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response1));
    stubFor(unaryMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(response2));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response1);
    assertThat(serviceStub.serverStreamingRpc(request)).toIterable().containsExactly(response2);
  }

  @Test
  void should_return_a_response_when_request_satisfies_defined_matching_condition() {
    SimpleRequest matchRequest = SimpleRequest.newBuilder()
        .setRequestMessage("message-1")
        .build();
    SimpleResponse response = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .withRequest(matchRequest)
        .willReturn(response));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(matchRequest)).isEqualTo(response);
  }

  @Test
  void should_not_return_a_response_when_request_does_not_satisfy_matching_condition() {
    SimpleRequest matchRequest = SimpleRequest.newBuilder()
        .setRequestMessage("message-1")
        .build();
    SimpleResponse response = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .withRequest(matchRequest)
        .willReturn(response));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThatThrownBy(() -> serviceStub.unaryRpc(request)).hasMessageStartingWith("UNIMPLEMENTED");
  }

  @Test
  void should_return_a_response_when_headers_satisfies_defined_matching_condition() {
    SimpleResponse response = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .withHeader(HEADER_1, "value-1")
        .withHeader(HEADER_2, value -> value.startsWith("value"))
        .willReturn(response));

    SimpleServiceBlockingStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newBlockingStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2"
    );

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
  }

  @Test
  void should_not_return_a_response_when_headers_does_not_satisfy_matching_condition() {
    SimpleResponse response = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .withoutHeader(HEADER_2)
        .willReturn(response));

    SimpleServiceBlockingStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newBlockingStub(serverChannel),
        HEADER_1, "value-2",
        HEADER_2, "value-2"
    );

    assertThatThrownBy(() -> serviceStub.unaryRpc(request)).hasMessageStartingWith("UNIMPLEMENTED");
  }

  @Test
  void should_return_multiple_unary_responses_for_multiple_requests() {
    SimpleResponse response1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();
    SimpleResponse response2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response1)
        .nextWillReturn(response2));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response1);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
  }

  @Test
  void should_return_multiple_unary_object_or_error_responses_for_multiple_requests() {
    SimpleResponse response1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();
    SimpleResponse response2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response1)
        .nextWillReturn(Status.INTERNAL)
        .nextWillReturn(response(response2)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response1);
    assertThatThrownBy(() -> serviceStub.unaryRpc(request)).hasMessage("INTERNAL");
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
  }

  @Test
  void should_return_multiple_object_or_error_responses_for_multiple_requests_passing_with_delay() {
    long start = System.currentTimeMillis();
    SimpleResponse response1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();
    SimpleResponse response2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(response1).withFixedDelay(100))
        .nextWillReturn(statusException(Status.INVALID_ARGUMENT).withFixedDelay(100))
        .nextWillReturn(response(response2).withFixedDelay(100)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response1);
    assertThatThrownBy(() -> serviceStub.unaryRpc(request)).hasMessage("INVALID_ARGUMENT");
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
    assertThat(System.currentTimeMillis() - start).isGreaterThan(300);
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

  @Test
  void should_call_proxying_response_as_initial_response() {
    SimpleResponse response1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();
    SimpleResponse response2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-2")
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willProxyTo((request, responseObserver) -> {
          responseObserver.onNext(response1);
          responseObserver.onCompleted();
        })
        .nextWillReturn(response2));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response1);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
  }

  @Test
  void should_call_proxying_response_as_subsequent_call_response() {
    SimpleResponse response1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();
    SimpleResponse response2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-2")
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response1)
        .nextWillProxyTo((request, responseObserver) -> {
          responseObserver.onNext(response2);
          responseObserver.onCompleted();
        }));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response1);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
  }

  @Test
  void should_call_proxying_response_built_based_on_request() {
    SimpleResponse response1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1 for " + REQUEST_MESSAGE)
        .build();
    SimpleResponse response2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-2")
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(request -> SimpleResponse.newBuilder()
            .setResponseMessage("message-1 for " + request.getRequestMessage())
            .build())
        .nextWillReturn(response2));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response1);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
  }

  @Test
  void should_call_proxying_response_built_based_on_request_as_subsequent_call_response() {
    SimpleResponse response1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();
    SimpleResponse response2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-2 for " + REQUEST_MESSAGE)
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response1)
        .nextWillReturn(request -> SimpleResponse.newBuilder()
            .setResponseMessage("message-2 for " + request.getRequestMessage())
            .build()));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response1);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
  }

  @Test
  void should_call_proxying_response_passed_from_bindable_service_impl() {
    SimpleResponse response = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    SimpleServiceImplBase service = new SimpleServiceImplBase() {
      @Override
      public void unaryRpc(SimpleRequest request, StreamObserver<SimpleResponse> responseObserver) {
        responseObserver.onNext(response);
        responseObserver.onCompleted();
      }
    };

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willProxyTo(service::unaryRpc));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
  }
}