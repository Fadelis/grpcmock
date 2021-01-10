package org.grpcmock;

import static io.grpc.testing.protobuf.SimpleServiceGrpc.getServerStreamingRpcMethod;
import static io.grpc.testing.protobuf.SimpleServiceGrpc.getUnaryRpcMethod;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.grpcmock.GrpcMock.atLeast;
import static org.grpcmock.GrpcMock.atMost;
import static org.grpcmock.GrpcMock.calledMethod;
import static org.grpcmock.GrpcMock.exception;
import static org.grpcmock.GrpcMock.never;
import static org.grpcmock.GrpcMock.response;
import static org.grpcmock.GrpcMock.stubFor;
import static org.grpcmock.GrpcMock.times;
import static org.grpcmock.GrpcMock.unaryMethod;
import static org.grpcmock.GrpcMock.verifyThat;

import io.grpc.Status;
import io.grpc.internal.testing.StreamRecorder;
import io.grpc.testing.protobuf.SimpleRequest;
import io.grpc.testing.protobuf.SimpleResponse;
import io.grpc.testing.protobuf.SimpleServiceGrpc;
import io.grpc.testing.protobuf.SimpleServiceGrpc.SimpleServiceBlockingStub;
import io.grpc.testing.protobuf.SimpleServiceGrpc.SimpleServiceStub;
import org.junit.jupiter.api.Test;

/**
 * @author Fadelis
 */
class GrpcMockVerifyTest extends TestBase {

  private final SimpleRequest request1 = SimpleRequest.newBuilder()
      .setRequestMessage("message-1")
      .build();
  private final SimpleRequest request2 = SimpleRequest.newBuilder()
      .setRequestMessage("message-2")
      .build();
  private final SimpleResponse response = SimpleResponse.newBuilder()
      .setResponseMessage("message-1")
      .build();

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

    verifyThat(calledMethod(getUnaryRpcMethod()).withRequest(request1), times(5));
    verifyThat(calledMethod(getUnaryRpcMethod()).withRequest(request2), times(3));
    verifyThat(calledMethod(getUnaryRpcMethod()).withRequest(request), never());
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
            .withRequest(request1),
        times(3));
    verifyThat(
        calledMethod(getUnaryRpcMethod())
            .withHeader(HEADER_1, "value-3")
            .withHeader(HEADER_2, "value-4")
            .withRequest(request1),
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
            .withRequest(request1),
        never());
    verifyThat(
        calledMethod(getUnaryRpcMethod())
            .withHeader(HEADER_1, "value-1")
            .withHeader(HEADER_2, "value-2")
            .withRequest(request),
        never());
  }

  @Test
  void should_correctly_verify_once_called_methods() {
    stubFor(unaryMethod(getUnaryRpcMethod()).willReturn(response(response)));

    SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);
    serviceStub.unaryRpc(request1);

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

    assertThatThrownBy(() -> serviceStub.unaryRpc(request1))
        .hasMessageStartingWith("UNKNOWN");
    verifyThat(calledMethod(getUnaryRpcMethod())
        .withHeader(HEADER_1, "value-1")
        .withRequest(request1));
  }

  @Test
  void should_correctly_verify_unary_call_returning_a_status_exception() {
    stubFor(unaryMethod(getUnaryRpcMethod()).willReturn(Status.INVALID_ARGUMENT));

    SimpleServiceBlockingStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newBlockingStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2");

    assertThatThrownBy(() -> serviceStub.unaryRpc(request1))
        .hasMessageStartingWith("INVALID_ARGUMENT");
    verifyThat(calledMethod(getUnaryRpcMethod())
        .withHeader(HEADER_1, "value-1")
        .withRequest(request1));
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

    assertThatThrownBy(() -> serviceStub.unaryRpc(request1))
        .hasMessageStartingWith("UNIMPLEMENTED: No matching stub scenario was found for this method:");
    verifyThat(calledMethod(getUnaryRpcMethod())
        .withHeader(HEADER_1, "value-1")
        .withRequest(request1));
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
    serviceStub.serverStreamingRpc(request1, streamRecorder);

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

    assertThatThrownBy(() -> serviceStub.unaryRpc(request1))
        .hasMessageStartingWith("UNIMPLEMENTED: Method not found:");
    verifyThat(calledMethod(getUnaryRpcMethod())
        .withHeader(HEADER_1, "value-1")
        .withRequest(request1));
  }

  @Test
  void should_correctly_verify_non_mocked_server_streaming_method_call() throws Exception {
    SimpleServiceStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2");

    StreamRecorder<SimpleResponse> streamRecorder = StreamRecorder.create();
    serviceStub.serverStreamingRpc(request1, streamRecorder);

    streamRecorder.awaitCompletion();
    assertThat(streamRecorder.getValues()).isEmpty();
    assertThat(streamRecorder.getError())
        .hasMessageStartingWith("UNIMPLEMENTED: Method not found:");
    verifyThat(calledMethod(getServerStreamingRpcMethod())
        .withHeader(HEADER_1, "value-1")
        .withRequest(request1));
  }

  @Test
  void should_correctly_verify_a_non_mocked_method_called_when_a_different_one_is_registered() throws Exception {
    stubFor(unaryMethod(getUnaryRpcMethod()).willReturn(response));

    SimpleServiceStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2");

    StreamRecorder<SimpleResponse> streamRecorder = StreamRecorder.create();
    serviceStub.serverStreamingRpc(request1, streamRecorder);

    streamRecorder.awaitCompletion();
    assertThat(streamRecorder.getValues()).isEmpty();
    assertThat(streamRecorder.getError())
        .hasMessageStartingWith("UNIMPLEMENTED: Method not found:");
    verifyThat(getServerStreamingRpcMethod());
    verifyThat(getUnaryRpcMethod(), never());
  }

  private void performUnaryMultipleUnaryCalls() {
    stubFor(unaryMethod(getUnaryRpcMethod())
        .withRequest(request1)
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

    serviceStub1.unaryRpc(request1);
    serviceStub1.unaryRpc(request2);
    serviceStub2.unaryRpc(request1);
    serviceStub1.unaryRpc(request1);
    serviceStub2.unaryRpc(request2);
    serviceStub1.unaryRpc(request2);
    serviceStub1.unaryRpc(request1);
    serviceStub2.unaryRpc(request1);
  }
}
