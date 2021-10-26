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
import java.time.Duration;
import org.junit.jupiter.api.Test;

/**
 * @author Fadelis
 */
class GrpcMockUnaryMethodPortBasedTest extends PortBasedTestBase {

  @Test
  void should_return_a_unary_response() {
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(response)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
  }

  @Test
  void should_return_a_unary_response_with_a_delay() {
    long start = System.currentTimeMillis();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(response)
            .withFixedDelay(200)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
    assertThat(System.currentTimeMillis() - start).isGreaterThan(200);
  }

  @Test
  void should_return_a_unary_response_with_a_random_delay() {
    long start = System.currentTimeMillis();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(response)
            .withRandomDelay(50, 200)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
    assertThat(System.currentTimeMillis() - start).isGreaterThan(50L);
  }

  @Test
  void should_return_a_unary_response_with_a_duration_delay() {
    long start = System.currentTimeMillis();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(response)
            .withFixedDelay(Duration.ofMillis(200))));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
    assertThat(System.currentTimeMillis() - start).isGreaterThan(200);
  }

  @Test
  void should_return_a_unary_response_with_a_duration_random_delay() {
    long start = System.currentTimeMillis();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(response)
            .withRandomDelay(Duration.ofMillis(50), Duration.ofMillis(200))));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
    assertThat(System.currentTimeMillis() - start).isGreaterThan(50L);
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
    stubFor(unaryMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(response(response)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.serverStreamingRpc(request)).toIterable().containsExactly(response);
  }

  @Test
  void should_register_multiple_method_stub_for_the_same_service() {
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response));
    stubFor(unaryMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(response2));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
    assertThat(serviceStub.serverStreamingRpc(request)).toIterable().containsExactly(response2);
  }

  @Test
  void should_return_a_response_when_request_satisfies_defined_matching_condition() {
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .withRequest(request)
        .willReturn(response));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
  }

  @Test
  void should_overwrite_request_matching_condition_on_subsequent_call() {
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .withRequest(request2)
        .withRequest(request)
        .willReturn(response));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
  }

  @Test
  void should_not_return_a_response_when_request_does_not_satisfy_matching_condition() {
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .withRequest(request2)
        .willReturn(response));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThatThrownBy(() -> serviceStub.unaryRpc(request)).hasMessageStartingWith("UNIMPLEMENTED");
  }

  @Test
  void should_return_a_response_when_headers_satisfies_defined_matching_condition() {
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
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response)
        .nextWillReturn(response2));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
  }

  @Test
  void should_return_multiple_unary_object_or_error_responses_for_multiple_requests() {
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response)
        .nextWillReturn(Status.INTERNAL)
        .nextWillReturn(response(response2)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
    assertThatThrownBy(() -> serviceStub.unaryRpc(request)).hasMessage("INTERNAL");
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
  }

  @Test
  void should_return_multiple_object_or_error_responses_for_multiple_requests_passing_with_delay() {
    long start = System.currentTimeMillis();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(response).withFixedDelay(100))
        .nextWillReturn(statusException(Status.INVALID_ARGUMENT).withFixedDelay(100))
        .nextWillReturn(response(response2).withFixedDelay(100)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
    assertThatThrownBy(() -> serviceStub.unaryRpc(request)).hasMessage("INVALID_ARGUMENT");
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
    assertThat(System.currentTimeMillis() - start).isGreaterThan(300);
  }

  @Test
  void should_register_multiple_same_method_scenarios_with_different_matching_conditions() {
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .withRequest(request)
        .willReturn(response(response)));
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .withRequest(request2)
        .willReturn(response(response2)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
    assertThat(serviceStub.unaryRpc(request2)).isEqualTo(response2);
  }

  @Test
  void should_last_register_method_scenario_should_be_triggered_when_multiple_matches_available() {
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(response)));
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(response2)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    // check multiple times
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
  }

  @Test
  void should_call_proxying_response_as_initial_response() {
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willProxyTo((request, responseObserver) -> {
          responseObserver.onNext(response);
          responseObserver.onCompleted();
        })
        .nextWillReturn(response2));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
  }

  @Test
  void should_call_proxying_response_as_subsequent_call_response() {
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response)
        .nextWillProxyTo((request, responseObserver) -> {
          responseObserver.onNext(response2);
          responseObserver.onCompleted();
        }));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
  }

  @Test
  void should_call_proxying_response_built_based_on_request() {
    SimpleResponse response1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1 for " + REQUEST_MESSAGE)
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
    SimpleResponse response2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-2 for " + REQUEST_MESSAGE)
        .build();

    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response)
        .nextWillReturn(request -> SimpleResponse.newBuilder()
            .setResponseMessage("message-2 for " + request.getRequestMessage())
            .build()));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response2);
  }

  @Test
  void should_call_proxying_response_passed_from_bindable_service_impl() {
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