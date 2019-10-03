package org.grpcmock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.grpcmock.GrpcMock.response;
import static org.grpcmock.GrpcMock.service;
import static org.grpcmock.GrpcMock.statusException;
import static org.grpcmock.GrpcMock.stream;
import static org.grpcmock.GrpcMock.stubFor;
import static org.grpcmock.GrpcMock.times;
import static org.grpcmock.GrpcMock.verifyThat;

import io.grpc.Status;
import io.grpc.internal.testing.StreamRecorder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.protobuf.SimpleRequest;
import io.grpc.testing.protobuf.SimpleResponse;
import io.grpc.testing.protobuf.SimpleServiceGrpc;
import io.grpc.testing.protobuf.SimpleServiceGrpc.SimpleServiceStub;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/**
 * @author Fadelis
 */
class GrpcMockServerStreamingMethodTest extends TestBase {

  @Test
  void should_return_a_unary_response() {
    SimpleResponse expected = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(response(expected)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).containsExactly(expected);
  }

  @Test
  void should_return_a_unary_response_with_a_delay() {
    long start = System.currentTimeMillis();
    SimpleResponse expected = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(response(expected)
            .withFixedDelay(200)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).containsExactly(expected);
    assertThat(System.currentTimeMillis() - start).isGreaterThan(200);
  }

  @Test
  void should_return_multiple_responses() {
    List<SimpleResponse> responses = IntStream.rangeClosed(1, 3)
        .mapToObj(id -> SimpleResponse.newBuilder()
            .setResponseMessage("message-" + id)
            .build())
        .collect(Collectors.toList());

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(stream(response(responses.get(0)))
            .and(response(responses.get(1)))
            .and(response(responses.get(2)))));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).isEqualTo(responses);
  }

  @Test
  void should_return_multiple_responses_with_delay() {
    long start = System.currentTimeMillis();
    List<SimpleResponse> responses = IntStream.rangeClosed(1, 3)
        .mapToObj(id -> SimpleResponse.newBuilder()
            .setResponseMessage("message-" + id)
            .build())
        .collect(Collectors.toList());

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(stream(response(responses.get(0)).withFixedDelay(200))
            .and(response(responses.get(1)).withFixedDelay(100))
            .and(response(responses.get(2)).withFixedDelay(200))));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).isEqualTo(responses);
    assertThat(System.currentTimeMillis() - start).isGreaterThan(500);
  }

  @Test
  void should_respond_with_error_status() {
    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(statusException(Status.ALREADY_EXISTS.withDescription("some error"))));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThatThrownBy(() -> asyncStubCall(request, serviceStub::serverStreamingRpc))
        .hasMessage("ALREADY_EXISTS: some error");
  }

  @Test
  void should_respond_with_error_status_with_a_delay() {
    long start = System.currentTimeMillis();
    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(statusException(Status.ALREADY_EXISTS.withDescription("some error"))
            .withFixedDelay(200)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThatThrownBy(() -> asyncStubCall(request, serviceStub::serverStreamingRpc))
        .hasMessage("ALREADY_EXISTS: some error");
    assertThat(System.currentTimeMillis() - start).isGreaterThan(200);
  }

  @Test
  void should_respond_with_error_status_after_multiple_responses() throws Exception {
    List<SimpleResponse> responses = IntStream.rangeClosed(1, 2)
        .mapToObj(id -> SimpleResponse.newBuilder()
            .setResponseMessage("message-" + id)
            .build())
        .collect(Collectors.toList());

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(stream(response(responses.get(0)))
            .and(response(responses.get(1)))
            .and(statusException(Status.ALREADY_EXISTS.withDescription("some error")))));

    StreamRecorder<SimpleResponse> streamRecorder = StreamRecorder.create();
    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);
    serviceStub.serverStreamingRpc(request, streamRecorder);

    streamRecorder.awaitCompletion(10, TimeUnit.SECONDS);
    assertThat(streamRecorder.getValues()).isEqualTo(responses);
    assertThat(streamRecorder.getError()).hasMessage("ALREADY_EXISTS: some error");
  }

  @Test
  void should_respond_with_error_status_after_multiple_responses_with_delay() throws Exception {
    long start = System.currentTimeMillis();
    List<SimpleResponse> responses = IntStream.rangeClosed(1, 2)
        .mapToObj(id -> SimpleResponse.newBuilder()
            .setResponseMessage("message-" + id)
            .build())
        .collect(Collectors.toList());

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(stream(response(responses.get(0)).withFixedDelay(200))
            .and(response(responses.get(1)).withFixedDelay(100))
            .and(statusException(Status.ALREADY_EXISTS.withDescription("some error"))
                .withFixedDelay(200))));

    StreamRecorder<SimpleResponse> streamRecorder = StreamRecorder.create();
    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);
    serviceStub.serverStreamingRpc(request, streamRecorder);

    streamRecorder.awaitCompletion(10, TimeUnit.SECONDS);
    assertThat(streamRecorder.getValues()).isEqualTo(responses);
    assertThat(streamRecorder.getError()).hasMessage("ALREADY_EXISTS: some error");
    assertThat(System.currentTimeMillis() - start).isGreaterThan(500);
  }

  @Test
  void should_return_a_response_when_request_equals_to_defined_condition_one() {
    SimpleRequest matchRequest = SimpleRequest.newBuilder()
        .setRequestMessage("message-1")
        .build();
    SimpleResponse expected = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .withRequest(matchRequest)
        .willReturn(response(expected)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncStubCall(matchRequest, serviceStub::serverStreamingRpc))
        .containsExactly(expected);
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
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .withRequest(req -> req.getRequestMessage().endsWith("1"))
        .willReturn(response(expected)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncStubCall(matchRequest, serviceStub::serverStreamingRpc))
        .containsExactly(expected);
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
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .withRequest(matchRequest)
        .willReturn(response(expected)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThatThrownBy(() -> asyncStubCall(request, serviceStub::serverStreamingRpc))
        .hasMessageStartingWith("UNIMPLEMENTED");
  }

  @Test
  void should_return_a_response_when_headers_satisfies_defined_matching_condition() {
    SimpleResponse expected = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .withHeader(HEADER_1, "value-1")
        .withHeader(HEADER_2, value -> value.startsWith("value"))
        .willReturn(response(expected)));

    SimpleServiceStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newStub(serverChannel),
        HEADER_1, "value-1",
        HEADER_2, "value-2"
    );

    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).containsExactly(expected);
  }

  @Test
  void should_not_return_a_response_when_headers_does_not_satisfy_matching_condition() {
    SimpleResponse expected = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .withoutHeader(HEADER_2)
        .willReturn(response(expected)));

    SimpleServiceStub serviceStub = stubWithHeaders(
        SimpleServiceGrpc.newStub(serverChannel),
        HEADER_1, "value-2",
        HEADER_2, "value-2"
    );

    assertThatThrownBy(() -> asyncStubCall(request, serviceStub::serverStreamingRpc))
        .hasMessageStartingWith("UNIMPLEMENTED");
  }

  @Test
  void should_return_multiple_unary_responses_for_multiple_requests() {
    SimpleResponse expected1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();
    SimpleResponse expected2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-2")
        .build();

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(response(expected1))
        .nextWillReturn(response(expected2)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).containsExactly(expected1);
    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).containsExactly(expected2);
    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).containsExactly(expected2);
  }

  @Test
  void should_return_multiple_stream_responses_for_multiple_requests() {
    List<SimpleResponse> responses1 = IntStream.rangeClosed(1, 2)
        .mapToObj(id -> SimpleResponse.newBuilder()
            .setResponseMessage("message-" + id)
            .build())
        .collect(Collectors.toList());
    List<SimpleResponse> responses2 = IntStream.rangeClosed(3, 4)
        .mapToObj(id -> SimpleResponse.newBuilder()
            .setResponseMessage("message-" + id)
            .build())
        .collect(Collectors.toList());

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(stream(response(responses1.get(0)))
            .and(response(responses1.get(1))))
        .nextWillReturn(stream(response(responses2.get(0)))
            .and(response(responses2.get(1)))));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).isEqualTo(responses1);
    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).isEqualTo(responses2);
    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).isEqualTo(responses2);
  }

  @Test
  void should_return_multiple_unary_object_or_error_responses_for_multiple_requests() {
    SimpleResponse expected1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();
    SimpleResponse expected2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-2")
        .build();

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(response(expected1))
        .nextWillReturn(statusException(Status.INTERNAL))
        .nextWillReturn(response(expected2)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).containsExactly(expected1);
    assertThatThrownBy(() -> asyncStubCall(request, serviceStub::serverStreamingRpc))
        .hasMessage("INTERNAL");
    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).containsExactly(expected2);
    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).containsExactly(expected2);
  }

  @Test
  void should_return_multiple_stream_responses_or_error_responses_for_multiple_requests() {
    List<SimpleResponse> responses1 = IntStream.rangeClosed(1, 2)
        .mapToObj(id -> SimpleResponse.newBuilder()
            .setResponseMessage("message-" + id)
            .build())
        .collect(Collectors.toList());
    List<SimpleResponse> responses2 = IntStream.rangeClosed(3, 4)
        .mapToObj(id -> SimpleResponse.newBuilder()
            .setResponseMessage("message-" + id)
            .build())
        .collect(Collectors.toList());

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(stream(response(responses1.get(0)))
            .and(response(responses1.get(1))))
        .nextWillReturn(stream(response(responses1.get(0)))
            .and(statusException(Status.INTERNAL)))
        .nextWillReturn(stream(response(responses2.get(0)))
            .and(response(responses2.get(1)))));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).isEqualTo(responses1);
    assertThatThrownBy(() -> asyncStubCall(request, serviceStub::serverStreamingRpc))
        .hasMessage("INTERNAL");
    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).isEqualTo(responses2);
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

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .withRequest(request1)
        .willReturn(response(expected1)));
    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .withRequest(request2)
        .willReturn(response(expected2)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncStubCall(request1, serviceStub::serverStreamingRpc)).containsExactly(expected1);
    assertThat(asyncStubCall(request2, serviceStub::serverStreamingRpc)).containsExactly(expected2);
    verifyThat(SimpleServiceGrpc.getServerStreamingRpcMethod(), times(2));
  }

  @Test
  void should_last_register_method_scenario_should_be_triggered_when_multiple_matches_available() {
    SimpleResponse expected1 = SimpleResponse.newBuilder()
        .setResponseMessage("message-1")
        .build();
    SimpleResponse expected2 = SimpleResponse.newBuilder()
        .setResponseMessage("message-2")
        .build();

    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(response(expected1)));
    stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(response(expected2)));

    SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    // check multiple times
    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).containsExactly(expected2);
    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).containsExactly(expected2);
  }

  private <ReqT, RespT> List<RespT> asyncStubCall(
      ReqT request,
      BiConsumer<ReqT, StreamObserver<RespT>> callMethod
  ) {
    StreamRecorder<RespT> streamRecorder = StreamRecorder.create();
    callMethod.accept(request, streamRecorder);

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