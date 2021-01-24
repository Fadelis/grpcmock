package org.grpcmock;

import static io.grpc.testing.protobuf.SimpleServiceGrpc.getBidiStreamingRpcMethod;
import static io.grpc.testing.protobuf.SimpleServiceGrpc.getClientStreamingRpcMethod;
import static io.grpc.testing.protobuf.SimpleServiceGrpc.getServerStreamingRpcMethod;
import static io.grpc.testing.protobuf.SimpleServiceGrpc.getUnaryRpcMethod;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.grpcmock.GrpcMock.atLeast;
import static org.grpcmock.GrpcMock.atMost;
import static org.grpcmock.GrpcMock.calledMethod;
import static org.grpcmock.GrpcMock.clientStreamingMethod;
import static org.grpcmock.GrpcMock.exception;
import static org.grpcmock.GrpcMock.never;
import static org.grpcmock.GrpcMock.response;
import static org.grpcmock.GrpcMock.stubFor;
import static org.grpcmock.GrpcMock.times;
import static org.grpcmock.GrpcMock.unaryMethod;
import static org.grpcmock.GrpcMock.verifyThat;

import io.grpc.Status;
import io.grpc.internal.testing.StreamRecorder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.protobuf.SimpleRequest;
import io.grpc.testing.protobuf.SimpleResponse;
import io.grpc.testing.protobuf.SimpleServiceGrpc;
import io.grpc.testing.protobuf.SimpleServiceGrpc.SimpleServiceBlockingStub;
import io.grpc.testing.protobuf.SimpleServiceGrpc.SimpleServiceStub;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * @author Fadelis
 */
class GrpcMockVerifyTest extends TestBase {

  @Test
  void should_correctly_verify_call_count_for_unary_method() {
    performUnaryMultipleUnaryCalls();

    verifyThat(getUnaryRpcMethod(), times(8));
    verifyThat(getUnaryRpcMethod(), atLeast(3));
    verifyThat(getUnaryRpcMethod(), atLeast(8));
    verifyThat(getUnaryRpcMethod(), atMost(8));
    verifyThat(getUnaryRpcMethod(), atMost(10));
  }

  @Test
  void should_fail_verify_call_count_for_unary_method() {
    performUnaryMultipleUnaryCalls();

    assertThatThrownBy(() -> verifyThat(getUnaryRpcMethod(), times(5)));
    assertThatThrownBy(() -> verifyThat(getUnaryRpcMethod(), atLeast(9)));
    assertThatThrownBy(() -> verifyThat(getUnaryRpcMethod(), atMost(7)));
    assertThatThrownBy(() -> verifyThat(getUnaryRpcMethod(), never()));
  }

  @Test
  void should_correctly_verify_call_count_for_unary_method_with_request_matching() {
    performUnaryMultipleUnaryCalls();

    verifyThat(calledMethod(getUnaryRpcMethod()).withRequest(request), times(5));
    verifyThat(calledMethod(getUnaryRpcMethod()).withRequest(request2), times(3));
    verifyThat(calledMethod(getUnaryRpcMethod()).withRequest(SimpleRequest.getDefaultInstance()), never());
  }

  @Test
  void should_correctly_verify_call_count_for_unary_method_with_header_matching() {
    performUnaryMultipleUnaryCalls();

    verifyThat(
        calledMethod(getUnaryRpcMethod())
            .withHeader(HEADER_1, "value-1")
            .withHeader(HEADER_2, "value-2"),
        times(5));
    verifyThat(
        calledMethod(getUnaryRpcMethod())
            .withHeader(HEADER_1, "value-3")
            .withHeader(HEADER_2, "value-4"),
        times(3));
    verifyThat(
        calledMethod(getUnaryRpcMethod())
            .withHeader(HEADER_1, "value-4"),
        never());
  }

  @Test
  void should_correctly_verify_call_count_for_unary_method_with_combined_matching() {
    performUnaryMultipleUnaryCalls();

    verifyThat(
        calledMethod(getUnaryRpcMethod())
            .withHeader(HEADER_1, "value-1")
            .withHeader(HEADER_2, "value-2")
            .withRequest(request),
        times(3));
    verifyThat(
        calledMethod(getUnaryRpcMethod())
            .withHeader(HEADER_1, "value-3")
            .withHeader(HEADER_2, "value-4")
            .withRequest(request),
        times(2));
    verifyThat(
        calledMethod(getUnaryRpcMethod())
            .withHeader(HEADER_1, "value-1")
            .withHeader(HEADER_2, "value-2")
            .withRequest(request2),
        times(2));
    verifyThat(
        calledMethod(getUnaryRpcMethod())
            .withHeader(HEADER_1, "value-3")
            .withHeader(HEADER_2, "value-4")
            .withRequest(request2),
        times(1));
    verifyThat(
        calledMethod(getUnaryRpcMethod())
            .withHeader(HEADER_1, "value-4")
            .withRequest(request),
        never());
    verifyThat(
        calledMethod(getUnaryRpcMethod())
            .withHeader(HEADER_1, "value-1")
            .withHeader(HEADER_2, "value-2")
            .withRequest(SimpleRequest.getDefaultInstance()),
        never());
  }

  @Test
  void should_correctly_verify_once_called_methods() {
    stubFor(unaryMethod(getUnaryRpcMethod()).willReturn(response(response)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);
    serviceStub.unaryRpc(request);

    // whole method
    verifyThat(getUnaryRpcMethod());

    performUnaryMultipleUnaryCalls();

    // specific invocation conditions
    verifyThat(calledMethod(getUnaryRpcMethod())
        .withHeader(HEADER_1, "value-3")
        .withHeader(HEADER_2, "value-4")
        .withRequest(request2));
  }

  @Test
  void should_correctly_verify_unary_call_returning_an_exception() {
    stubFor(unaryMethod(getUnaryRpcMethod()).willReturn(exception(new IllegalStateException())));

    SimpleServiceBlockingStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newBlockingStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2");

    assertThatThrownBy(() -> serviceStub.unaryRpc(request))
        .hasMessageStartingWith("UNKNOWN");
    verifyThat(calledMethod(getUnaryRpcMethod())
        .withHeader(HEADER_1, "value-1")
        .withRequest(request));
  }

  @Test
  void should_correctly_verify_unary_call_returning_a_status_exception() {
    stubFor(unaryMethod(getUnaryRpcMethod()).willReturn(Status.INVALID_ARGUMENT));

    SimpleServiceBlockingStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newBlockingStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2");

    assertThatThrownBy(() -> serviceStub.unaryRpc(request))
        .hasMessageStartingWith("INVALID_ARGUMENT");
    verifyThat(calledMethod(getUnaryRpcMethod())
        .withHeader(HEADER_1, "value-1")
        .withRequest(request));
  }

  @Test
  void should_correctly_verify_call_count_for_client_streaming_method() {
    performUnaryMultipleClientStreamingCalls();

    verifyThat(getClientStreamingRpcMethod(), times(8));
    verifyThat(getClientStreamingRpcMethod(), atLeast(3));
    verifyThat(getClientStreamingRpcMethod(), atLeast(8));
    verifyThat(getClientStreamingRpcMethod(), atMost(8));
    verifyThat(getClientStreamingRpcMethod(), atMost(9));
  }

  @Test
  void should_fail_verify_call_count_for_client_streaming_method() {
    performUnaryMultipleClientStreamingCalls();

    assertThatThrownBy(() -> verifyThat(getClientStreamingRpcMethod(), times(5)));
    assertThatThrownBy(() -> verifyThat(getClientStreamingRpcMethod(), atLeast(9)));
    assertThatThrownBy(() -> verifyThat(getClientStreamingRpcMethod(), atMost(7)));
    assertThatThrownBy(() -> verifyThat(getClientStreamingRpcMethod(), never()));
  }

  @Test
  void should_correctly_verify_call_count_for_client_streaming_method_with_requests_matching() {
    performUnaryMultipleClientStreamingCalls();

    verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequests(request), times(2));
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequests(request2), times(1));
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequests(request, request2), times(3));
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequests(request2, request), times(2));
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequests(request, request), never());
  }

  @Test
  void should_correctly_verify_call_count_for_client_streaming_method_with_different_requests_matchers() {
    performUnaryMultipleClientStreamingCalls();

    verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequests(request2, request), times(2));
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequests(Arrays.asList(request, request2)), times(3));
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequests(list -> !list.isEmpty()), times(8));
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withNumberOfRequests(2), times(5));
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequestsContaining(request2::equals), times(6));
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequestsContaining(request), times(7));
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withFirstRequest(request2), times(3));
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withFirstRequest(Objects::nonNull), times(8));
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequestAtIndex(1, request2), times(3));
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequestAtIndex(1, Objects::nonNull), times(5));
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequests(request2, request, request2), never());
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequests(Arrays.asList(request2, request, request2)), never());
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequests(List::isEmpty), never());
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequestsContaining(SimpleRequest.getDefaultInstance()), never());
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequestAtIndex(3, request), never());
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withNumberOfRequests(4), never());
    verifyThat(calledMethod(getClientStreamingRpcMethod()).withFirstRequest(SimpleRequest.getDefaultInstance()), never());
  }

  @Test
  void should_correctly_verify_call_count_for_client_streaming_method_with_header_matching() {
    performUnaryMultipleClientStreamingCalls();

    verifyThat(
        calledMethod(getClientStreamingRpcMethod())
            .withHeader(HEADER_1, "value-1")
            .withHeader(HEADER_2, "value-2"),
        times(4));
    verifyThat(
        calledMethod(getClientStreamingRpcMethod())
            .withHeader(HEADER_1, "value-3")
            .withHeader(HEADER_2, "value-4"),
        times(4));
    verifyThat(
        calledMethod(getClientStreamingRpcMethod())
            .withHeader(HEADER_1, "value-4"),
        never());
  }

  @Test
  void should_correctly_verify_call_count_for_client_streaming_method_with_combined_matching() {
    performUnaryMultipleClientStreamingCalls();

    verifyThat(
        calledMethod(getClientStreamingRpcMethod())
            .withHeader(HEADER_1, "value-1")
            .withHeader(HEADER_2, "value-2")
            .withRequests(request, request2),
        times(2));
    verifyThat(
        calledMethod(getClientStreamingRpcMethod())
            .withHeader(HEADER_1, "value-3")
            .withHeader(HEADER_2, "value-4")
            .withRequests(request),
        times(1));
    verifyThat(
        calledMethod(getClientStreamingRpcMethod())
            .withHeader(HEADER_1, "value-1")
            .withHeader(HEADER_2, "value-2")
            .withRequests(request2, request),
        times(1));
    verifyThat(
        calledMethod(getClientStreamingRpcMethod())
            .withHeader(HEADER_1, "value-3")
            .withHeader(HEADER_2, "value-4")
            .withRequests(request2, request),
        times(1));
    verifyThat(
        calledMethod(getClientStreamingRpcMethod())
            .withHeader(HEADER_1, "value-4")
            .withRequests(request),
        never());
    verifyThat(
        calledMethod(getClientStreamingRpcMethod())
            .withHeader(HEADER_1, "value-1")
            .withHeader(HEADER_2, "value-2")
            .withRequests(request2),
        never());
  }

  @Test
  void should_fail_verify_if_using_stream_requests_matchers_from_unary_or_server_streaming_methods() {
    assertThatThrownBy(() -> verifyThat(calledMethod(getUnaryRpcMethod()).withRequests(request)));
    assertThatThrownBy(() -> verifyThat(calledMethod(getServerStreamingRpcMethod()).withRequests(request)));
  }

  @Test
  void should_fail_verify_if_using_single_request_matchers_from_client_or_bidi_streaming_methods() {
    assertThatThrownBy(() -> verifyThat(calledMethod(getClientStreamingRpcMethod()).withRequest(request)));
    assertThatThrownBy(() -> verifyThat(calledMethod(getBidiStreamingRpcMethod()).withRequest(request)));
  }

  @Test
  void should_correctly_verify_mocked_unary_method_call_but_with_no_matching_stub() {
    stubFor(unaryMethod(getUnaryRpcMethod())
        .withRequest(request2)
        .willReturn(response(response)));

    SimpleServiceBlockingStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newBlockingStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2");

    assertThatThrownBy(() -> serviceStub.unaryRpc(request))
        .hasMessageStartingWith("UNIMPLEMENTED: No matching stub scenario was found for this method:");
    verifyThat(calledMethod(getUnaryRpcMethod())
        .withHeader(HEADER_1, "value-1")
        .withRequest(request));
  }

  @Test
  void should_correctly_verify_a_mocked_method_not_called_when_a_different_one_is() throws Exception {
    stubFor(unaryMethod(getUnaryRpcMethod()).willReturn(response));
    stubFor(unaryMethod(getServerStreamingRpcMethod()).willReturn(response));

    SimpleServiceStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2");

    StreamRecorder<SimpleResponse> streamRecorder = StreamRecorder.create();
    serviceStub.serverStreamingRpc(request, streamRecorder);

    streamRecorder.awaitCompletion();
    assertThat(streamRecorder.getError()).isNull();
    assertThat(streamRecorder.getValues()).containsExactly(response);
    verifyThat(getServerStreamingRpcMethod());
    verifyThat(getUnaryRpcMethod(), never());
  }

  @Test
  void should_correctly_verify_non_mocked_unary_method_call() {
    SimpleServiceBlockingStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newBlockingStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2");

    assertThatThrownBy(() -> serviceStub.unaryRpc(request))
        .hasMessageStartingWith("UNIMPLEMENTED: Method not found:");
    verifyThat(calledMethod(getUnaryRpcMethod())
        .withHeader(HEADER_1, "value-1")
        .withRequest(request));
  }

  @Test
  void should_correctly_verify_non_mocked_server_streaming_method_call() throws Exception {
    SimpleServiceStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2");

    StreamRecorder<SimpleResponse> streamRecorder = StreamRecorder.create();
    serviceStub.serverStreamingRpc(request, streamRecorder);

    streamRecorder.awaitCompletion();
    assertThat(streamRecorder.getValues()).isEmpty();
    assertThat(streamRecorder.getError())
        .hasMessageStartingWith("UNIMPLEMENTED: Method not found:");
    verifyThat(calledMethod(getServerStreamingRpcMethod())
        .withHeader(HEADER_1, "value-1")
        .withRequest(request));
  }

  @Test
  void should_correctly_verify_non_mocked_client_streaming_method_call() throws Exception {
    SimpleServiceStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2");

    StreamRecorder<SimpleResponse> responseRecorder = StreamRecorder.create();
    StreamObserver<SimpleRequest> requestObserver = serviceStub.clientStreamingRpc(responseRecorder);

    requestObserver.onNext(request);
    requestObserver.onNext(request2); // only first request will be captured, as an error is throw after it

    responseRecorder.awaitCompletion();
    assertThat(responseRecorder.getValues()).isEmpty();
    assertThat(responseRecorder.getError())
        .hasMessageStartingWith("UNIMPLEMENTED: Method not found:");
    verifyThat(calledMethod(getClientStreamingRpcMethod())
        .withHeader(HEADER_1, "value-1")
        .withFirstRequest(request));
  }

  @Test
  void should_correctly_verify_non_mocked_bidi_streaming_method_call() throws Exception {
    SimpleServiceStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2");

    StreamRecorder<SimpleResponse> responseRecorder = StreamRecorder.create();
    StreamObserver<SimpleRequest> requestObserver = serviceStub.bidiStreamingRpc(responseRecorder);

    requestObserver.onNext(request);
    requestObserver.onNext(request2); // only first request will be captured, as an error is throw after it

    responseRecorder.awaitCompletion();
    assertThat(responseRecorder.getValues()).isEmpty();
    assertThat(responseRecorder.getError())
        .hasMessageStartingWith("UNIMPLEMENTED: Method not found:");
    verifyThat(calledMethod(getBidiStreamingRpcMethod())
        .withHeader(HEADER_1, "value-1")
        .withFirstRequest(request));
  }

  @Test
  void should_correctly_verify_a_non_mocked_method_called_when_a_different_one_is_registered() throws Exception {
    stubFor(unaryMethod(getUnaryRpcMethod()).willReturn(response));

    SimpleServiceStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2");

    StreamRecorder<SimpleResponse> streamRecorder = StreamRecorder.create();
    serviceStub.serverStreamingRpc(request, streamRecorder);

    streamRecorder.awaitCompletion();
    assertThat(streamRecorder.getValues()).isEmpty();
    assertThat(streamRecorder.getError())
        .hasMessageStartingWith("UNIMPLEMENTED: Method not found:");
    verifyThat(getServerStreamingRpcMethod());
    verifyThat(getUnaryRpcMethod(), never());
  }

  private void performUnaryMultipleUnaryCalls() {
    stubFor(unaryMethod(getUnaryRpcMethod())
        .withRequest(request)
        .willReturn(response(response)));
    stubFor(unaryMethod(getUnaryRpcMethod())
        .withRequest(request2)
        .willReturn(response(response)));

    SimpleServiceBlockingStub serviceStub1 = stubWithHeaders(
        SimpleServiceGrpc.newBlockingStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2");
    SimpleServiceBlockingStub serviceStub2 = stubWithHeaders(
        SimpleServiceGrpc.newBlockingStub(serverChannel),
        HEADER_1, "value-3",
        HEADER_2, "value-4");

    serviceStub1.unaryRpc(request);
    serviceStub1.unaryRpc(request2);
    serviceStub2.unaryRpc(request);
    serviceStub1.unaryRpc(request);
    serviceStub2.unaryRpc(request2);
    serviceStub1.unaryRpc(request2);
    serviceStub1.unaryRpc(request);
    serviceStub2.unaryRpc(request);
  }

  private void performUnaryMultipleClientStreamingCalls() {
    stubFor(clientStreamingMethod(getClientStreamingRpcMethod())
        .withFirstRequest(request)
        .willReturn(response(response)));
    stubFor(clientStreamingMethod(getClientStreamingRpcMethod())
        .withFirstRequest(request2)
        .willReturn(response(response)));

    SimpleServiceStub serviceStub1 = stubWithHeaders(
        SimpleServiceGrpc.newStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2");
    SimpleServiceStub serviceStub2 = stubWithHeaders(
        SimpleServiceGrpc.newStub(serverChannel),
        HEADER_1, "value-3",
        HEADER_2, "value-4");

    asyncClientStreamingCall(serviceStub1::clientStreamingRpc, request, request2);
    asyncClientStreamingCall(serviceStub1::clientStreamingRpc, request);
    asyncClientStreamingCall(serviceStub1::clientStreamingRpc, request2, request);
    asyncClientStreamingCall(serviceStub2::clientStreamingRpc, request, request2);
    asyncClientStreamingCall(serviceStub2::clientStreamingRpc, request);
    asyncClientStreamingCall(serviceStub2::clientStreamingRpc, request2, request);
    asyncClientStreamingCall(serviceStub2::clientStreamingRpc, request2);
    asyncClientStreamingCall(serviceStub1::clientStreamingRpc, request, request2);
  }

  private <ReqT, RespT> List<RespT> asyncClientStreamingCall(
      Function<StreamObserver<RespT>, StreamObserver<ReqT>> callMethod,
      ReqT... requests
  ) {
    StreamRecorder<RespT> streamRecorder = StreamRecorder.create();
    StreamObserver<ReqT> requestObserver = callMethod.apply(streamRecorder);
    Stream.of(requests).forEach(requestObserver::onNext);
    requestObserver.onCompleted();

    try {
      streamRecorder.awaitCompletion(10, TimeUnit.SECONDS);
    } catch (Exception e) {
      fail("failed waiting for response");
    }

    if (Objects.nonNull(streamRecorder.getError())) {
      throw Status.fromThrowable(streamRecorder.getError()).asRuntimeException();
    }
    return streamRecorder.getValues();
  }
}
