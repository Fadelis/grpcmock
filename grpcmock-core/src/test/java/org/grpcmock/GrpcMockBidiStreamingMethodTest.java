package org.grpcmock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.grpcmock.GrpcMock.bidiStreamingMethod;
import static org.grpcmock.GrpcMock.stubFor;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.protobuf.SimpleRequest;
import io.grpc.testing.protobuf.SimpleResponse;
import io.grpc.testing.protobuf.SimpleServiceGrpc;
import io.grpc.testing.protobuf.SimpleServiceGrpc.SimpleServiceImplBase;
import io.grpc.testing.protobuf.SimpleServiceGrpc.SimpleServiceStub;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.grpcmock.util.FunctionalResponseObserver;
import org.junit.jupiter.api.Test;

/**
 * @author Fadelis
 */
class GrpcMockBidiStreamingMethodTest extends TestBase {

  @Test
  void should_return_a_response_when_first_request_satisfies_defined_matching_condition() {
    stubFor(bidiStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
        .withFirstRequest(request)
        .willProxyTo(proxyingResponse(response)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request, request2)).containsExactly(response);
  }

  @Test
  void should_overwrite_first_request_matching_condition_on_subsequent_call() {
    stubFor(bidiStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
        .withFirstRequest(request2)
        .withFirstRequest(request)
        .willProxyTo(proxyingResponse(response)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request)).containsExactly(response);
  }

  @Test
  void should_not_return_a_response_when_first_request_does_not_satisfy_matching_condition() {
    stubFor(bidiStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
        .withFirstRequest(request2)
        .willProxyTo(proxyingResponse(response)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThatThrownBy(() -> asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageStartingWith("UNIMPLEMENTED: No matching stub scenario was found for this method: ");
  }

  @Test
  void should_return_a_response_when_headers_satisfies_defined_matching_condition() {
    stubFor(bidiStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
        .withHeader(HEADER_1, "value-1")
        .withHeader(HEADER_2, value -> value.startsWith("value"))
        .willProxyTo(proxyingResponse(response)));

    SimpleServiceStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2"
    );

    assertThat(asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request, request2)).containsExactly(response);
  }

  @Test
  void should_not_return_a_response_when_headers_does_not_satisfy_matching_condition() {
    stubFor(bidiStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
        .withoutHeader(HEADER_2)
        .willProxyTo(proxyingResponse(response)));

    SimpleServiceStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newStub(serverChannel),
        HEADER_1, "value-2",
        HEADER_2, "value-2"
    );

    assertThatThrownBy(() -> asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request, request2))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageStartingWith("UNIMPLEMENTED: No matching stub scenario was found for this method: ");
  }

  @Test
  void should_overwrite_first_request_condition_on_subsequent_call() {
    stubFor(bidiStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
        .withFirstRequest(request)
        .withFirstRequest(request2)
        .willProxyTo(proxyingResponse(response)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThatThrownBy(() -> asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageStartingWith("UNIMPLEMENTED: No matching stub scenario was found for this method: ");
  }

  @Test
  void should_return_method_not_found_error_when_no_stub_registered() {
    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThatThrownBy(() -> asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request, request2))
        .isInstanceOf(StatusRuntimeException.class)
        .hasMessageStartingWith("UNIMPLEMENTED: Method not found: ");
  }

  @Test
  void should_register_multiple_same_method_scenarios_with_different_matching_conditions() {
    stubFor(bidiStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
        .withFirstRequest(request)
        .willProxyTo(proxyingResponse(response)));
    stubFor(bidiStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
        .withFirstRequest(request2)
        .willProxyTo(proxyingResponse(response2)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request)).containsExactly(response);
    assertThat(asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request2)).containsExactly(response2);
  }

  @Test
  void should_triggered_last_register_method_scenario_when_multiple_matches_available() {
    stubFor(bidiStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
        .willProxyTo(proxyingResponse(response)));
    stubFor(bidiStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
        .willProxyTo(proxyingResponse(response2)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    // check multiple times
    assertThat(asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request)).containsExactly(response2);
    assertThat(asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request)).containsExactly(response2);
  }

  @Test
  void should_call_proxying_response_as_initial_response() {
    stubFor(bidiStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
        .willProxyTo(proxyingResponse(response)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request)).containsExactly(response);
    assertThat(asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request)).containsExactly(response);
  }

  @Test
  void should_call_proxying_response_as_subsequent_call_response() {
    stubFor(bidiStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
        .willProxyTo(proxyingResponse(response))
        .nextWillProxyTo(proxyingResponse(response2)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request)).containsExactly(response);
    assertThat(asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request)).containsExactly(response2);
  }

  @Test
  void should_call_proxying_response_passed_from_bindable_service_impl() {
    SimpleServiceImplBase service = new SimpleServiceImplBase() {
      @Override
      public StreamObserver<SimpleRequest> bidiStreamingRpc(StreamObserver<SimpleResponse> responseObserver) {
        return FunctionalResponseObserver.<SimpleRequest>builder()
            .onCompleted(() -> {
              responseObserver.onNext(response);
              responseObserver.onCompleted();
            })
            .build();
      }
    };

    stubFor(bidiStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
        .willProxyTo(service::bidiStreamingRpc));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request)).containsExactly(response);
  }

  @Test
  void should_call_proxying_response_based_on_incoming_requests() {
    stubFor(bidiStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
        .willProxyTo(responseObserver -> FunctionalResponseObserver.<SimpleRequest>builder()
            .onNext(request -> responseObserver.onNext(SimpleResponse.newBuilder()
                .setResponseMessage(request.getRequestMessage().replace("request", "message"))
                .build()))
            .onCompleted(responseObserver::onCompleted)
            .build()));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request, request2, request))
        .containsExactly(response, response2, response);
  }

  @Test
  void should_call_proxying_response_with_all_request_when_stub_is_selected_based_on_request_pattern() {
    List<SimpleRequest> receivedRequests = new ArrayList<>();
    stubFor(bidiStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
        .withFirstRequest(req -> req.getRequestMessage().equals(REQUEST_MESSAGE))
        .willProxyTo(responseObserver -> FunctionalResponseObserver.<SimpleRequest>builder()
            .onNext(receivedRequests::add)
            .onCompleted(() -> {
              responseObserver.onNext(response);
              responseObserver.onCompleted();
            })
            .build()));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request, request2)).containsExactly(response);
    assertThat(receivedRequests).containsExactly(request, request2);
  }

  private <ReqT, RespT> Function<StreamObserver<RespT>, StreamObserver<ReqT>> proxyingResponse(RespT response) {
    return responseObserver -> FunctionalResponseObserver.<ReqT>builder()
        .onCompleted(() -> {
          responseObserver.onNext(response);
          responseObserver.onCompleted();
        })
        .build();
  }
}