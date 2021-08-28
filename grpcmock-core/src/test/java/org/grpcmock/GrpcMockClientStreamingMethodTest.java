package org.grpcmock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.grpcmock.GrpcMock.clientStreamingMethod;
import static org.grpcmock.GrpcMock.response;
import static org.grpcmock.GrpcMock.statusException;
import static org.grpcmock.GrpcMock.stubFor;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.internal.testing.StreamRecorder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.protobuf.SimpleRequest;
import io.grpc.testing.protobuf.SimpleResponse;
import io.grpc.testing.protobuf.SimpleServiceGrpc;
import io.grpc.testing.protobuf.SimpleServiceGrpc.SimpleServiceImplBase;
import io.grpc.testing.protobuf.SimpleServiceGrpc.SimpleServiceStub;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.grpcmock.util.FunctionalResponseObserver;
import org.junit.jupiter.api.Test;

/**
 * @author Fadelis
 */
class GrpcMockClientStreamingMethodTest extends TestBase {


  @Test
  void should_return_a_response() {
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .willReturn(response));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request, request2)).containsExactly(response);
  }

  @Test
  void should_return_a_response_with_a_delay() {
    long start = System.currentTimeMillis();
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .willReturn(response(response)
            .withFixedDelay(200)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request, request2)).containsExactly(response);
    assertThat(System.currentTimeMillis() - start).isGreaterThan(200);
  }

  @Test
  void should_respond_with_error_status() {
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .willReturn(statusException(Status.ALREADY_EXISTS.withDescription("some error"))));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThatThrownBy(() -> asyncClientStreamingCall(serviceStub::clientStreamingRpc, request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessage("ALREADY_EXISTS: some error");
  }

  @Test
  void should_respond_with_error_status_with_a_delay() {
    long start = System.currentTimeMillis();
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .willReturn(statusException(Status.ALREADY_EXISTS.withDescription("some error"))
            .withFixedDelay(200)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThatThrownBy(() -> asyncClientStreamingCall(serviceStub::clientStreamingRpc, request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessage("ALREADY_EXISTS: some error");
    assertThat(System.currentTimeMillis() - start).isGreaterThan(200);
  }

  @Test
  void should_not_return_a_response_when_throwing_an_error_from_request_observer() throws Exception {
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .willReturn(response));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);
    ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    StreamRecorder<SimpleResponse> responseRecorder = StreamRecorder.create();
    StreamObserver<SimpleRequest> requestObserver = serviceStub.clientStreamingRpc(responseRecorder);

    requestObserver.onNext(request);
    // run async so request would be started
    scheduledExecutor.schedule(() -> requestObserver.onError(Status.ABORTED.asRuntimeException()), 200, TimeUnit.MILLISECONDS);

    responseRecorder.awaitCompletion(10, TimeUnit.SECONDS);
    assertThat(responseRecorder.getValues()).isEmpty();
    assertThat(responseRecorder.getError())
        .isInstanceOf(StatusRuntimeException.class)
        .hasRootCauseMessage("ABORTED");
  }

  @Test
  void should_return_a_single_response_for_bidi_streaming_request() {
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
        .willReturn(response));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request)).containsExactly(response);
  }

  @Test
  void should_return_a_response_when_first_request_satisfies_defined_matching_condition() {
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .withFirstRequest(request)
        .willReturn(response(response)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request, request2)).containsExactly(response);
  }

  @Test
  void should_overwrite_first_request_matching_condition_on_subsequent_call() {
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .withFirstRequest(request2)
        .withFirstRequest(request)
        .willReturn(response(response)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).containsExactly(response);
  }

  @Test
  void should_not_return_a_response_when_first_request_does_not_satisfy_matching_condition() {
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .withFirstRequest(request2)
        .willReturn(response));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThatThrownBy(() -> asyncClientStreamingCall(serviceStub::clientStreamingRpc, request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageStartingWith("UNIMPLEMENTED: No matching stub scenario was found for this method: ");
  }

  @Test
  void should_return_a_response_when_headers_satisfies_defined_matching_condition() {
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .withHeader(HEADER_1, "value-1")
        .withHeader(HEADER_2, value -> value.startsWith("value"))
        .willReturn(response));

    SimpleServiceStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2"
    );

    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request, request2)).containsExactly(response);
  }

  @Test
  void should_not_return_a_response_when_headers_does_not_satisfy_matching_condition() {
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .withoutHeader(HEADER_2)
        .willReturn(response));

    SimpleServiceStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newStub(serverChannel),
        HEADER_1, "value-2",
        HEADER_2, "value-2"
    );

    assertThatThrownBy(() -> asyncClientStreamingCall(serviceStub::clientStreamingRpc, request, request2))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageStartingWith("UNIMPLEMENTED: No matching stub scenario was found for this method: ");
  }

  @Test
  void should_overwrite_first_request_condition_on_subsequent_call() {
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .withFirstRequest(request)
        .withFirstRequest(request2)
        .willReturn(response));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThatThrownBy(() -> asyncClientStreamingCall(serviceStub::clientStreamingRpc, request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageStartingWith("UNIMPLEMENTED: No matching stub scenario was found for this method: ");
  }

  @Test
  void should_return_method_not_found_error_when_no_stub_registered() {
    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThatThrownBy(() -> asyncClientStreamingCall(serviceStub::clientStreamingRpc, request, request2))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageStartingWith("UNIMPLEMENTED: Method not found: ");
  }

  @Test
  void should_return_multiple_unary_responses_for_multiple_requests() {
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .willReturn(response)
        .nextWillReturn(response2));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).containsExactly(response);
    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).containsExactly(response2);
  }

  @Test
  void should_return_multiple_unary_object_or_error_responses_for_multiple_requests() {
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .willReturn(response)
        .nextWillReturn(Status.INTERNAL)
        .nextWillReturn(response(response2)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).containsExactly(response);
    assertThatThrownBy(() -> asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).hasMessage("INTERNAL");
    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).containsExactly(response2);
    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).containsExactly(response2);
  }

  @Test
  void should_return_multiple_object_or_error_responses_for_multiple_requests_passing_with_delay() {
    long start = System.currentTimeMillis();
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .willReturn(response(response).withFixedDelay(100))
        .nextWillReturn(statusException(Status.INVALID_ARGUMENT).withFixedDelay(100))
        .nextWillReturn(response(response2).withFixedDelay(100)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).containsExactly(response);
    assertThatThrownBy(() -> asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).hasMessage("INVALID_ARGUMENT");
    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).containsExactly(response2);
    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).containsExactly(response2);
    assertThat(System.currentTimeMillis() - start).isGreaterThan(300);
  }

  @Test
  void should_register_multiple_same_method_scenarios_with_different_matching_conditions() {
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .withFirstRequest(request)
        .willReturn(response(response)));
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .withFirstRequest(request2)
        .willReturn(response(response2)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).containsExactly(response);
    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request2)).containsExactly(response2);
  }

  @Test
  void should_last_register_method_scenario_should_be_triggered_when_multiple_matches_available() {
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .willReturn(response(response)));
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .willReturn(response(response2)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    // check multiple times
    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).containsExactly(response2);
    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).containsExactly(response2);
  }

  @Test
  void should_call_proxying_response_as_initial_response() {
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .willProxyTo(responseObserver -> FunctionalResponseObserver.<SimpleRequest>builder()
            .onCompleted(() -> {
              responseObserver.onNext(response);
              responseObserver.onCompleted();
            })
            .build())
        .nextWillReturn(response2));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).containsExactly(response);
    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).containsExactly(response2);
  }

  @Test
  void should_call_proxying_response_as_subsequent_call_response() {
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .willReturn(response)
        .nextWillProxyTo(responseObserver -> FunctionalResponseObserver.<SimpleRequest>builder()
            .onCompleted(() -> {
              responseObserver.onNext(response2);
              responseObserver.onCompleted();
            })
            .build()));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).containsExactly(response);
    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).containsExactly(response2);
  }

  @Test
  void should_call_proxying_response_passed_from_bindable_service_impl() {
    SimpleServiceImplBase service = new SimpleServiceImplBase() {
      @Override
      public StreamObserver<SimpleRequest> clientStreamingRpc(StreamObserver<SimpleResponse> responseObserver) {
        return FunctionalResponseObserver.<SimpleRequest>builder()
            .onCompleted(() -> {
              responseObserver.onNext(response);
              responseObserver.onCompleted();
            })
            .build();
      }
    };

    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .willProxyTo(service::clientStreamingRpc));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request)).containsExactly(response);
  }

  @Test
  void should_call_proxying_response_with_all_request_when_stub_is_selected_based_on_request_pattern() {
    List<SimpleRequest> receivedRequests = new ArrayList<>();
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .withFirstRequest(req -> req.getRequestMessage().equals(REQUEST_MESSAGE))
        .willProxyTo(responseObserver -> FunctionalResponseObserver.<SimpleRequest>builder()
            .onNext(receivedRequests::add)
            .onCompleted(() -> {
              responseObserver.onNext(response);
              responseObserver.onCompleted();
            })
            .build()));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request, request2)).containsExactly(response);
    assertThat(receivedRequests).containsExactly(request, request2);
  }
}