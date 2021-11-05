package org.grpcmock;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.internal.testing.StreamRecorder;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.protobuf.SimpleRequest;
import io.grpc.testing.protobuf.SimpleResponse;
import org.grpcmock.util.FunctionalResponseObserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.fail;

/**
 * @author Fadelis
 */
abstract class TestBase {

  static final String HEADER_1 = "header-1";
  static final String HEADER_2 = "header-2";
  static final String RESPONSE_MESSAGE = "message-1";
  static final String RESPONSE_MESSAGE_2 = "message-2";
  static final String REQUEST_MESSAGE = "request-1";
  static final String REQUEST_MESSAGE_2 = "request-2";

  final SimpleRequest request = SimpleRequest.newBuilder().setRequestMessage(REQUEST_MESSAGE).build();
  final SimpleRequest request2 = SimpleRequest.newBuilder().setRequestMessage(REQUEST_MESSAGE_2).build();
  final SimpleResponse response = SimpleResponse.newBuilder().setResponseMessage(RESPONSE_MESSAGE).build();
  final SimpleResponse response2 = SimpleResponse.newBuilder().setResponseMessage(RESPONSE_MESSAGE_2).build();
  ManagedChannel serverChannel;

  <T extends AbstractStub<T>> T stubWithHeaders(
      T baseStub,
      String headerName1, String headerValue1,
      String headerName2, String headerValue2
  ) {
    Map<String, String> headers = new HashMap<>();
    headers.put(headerName1, headerValue1);
    headers.put(headerName2, headerValue2);
    return stubWithHeaders(baseStub, headers);
  }

  <T extends AbstractStub<T>> T stubWithHeaders(T baseStub, Map<String, String> headers) {
    Metadata metadata = new Metadata();
    headers.forEach((key, value) -> metadata.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value));

    return MetadataUtils.attachHeaders(baseStub, metadata);
  }

  <ReqT, RespT> List<RespT> asyncClientStreamingCall(
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

  <ReqT, RespT> List<RespT> asyncStubCall(
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

  <ReqT, RespT> Function<StreamObserver<RespT>, StreamObserver<ReqT>> proxyingResponse(RespT response) {
    return responseObserver -> FunctionalResponseObserver.<ReqT>builder()
            .onCompleted(() -> {
              responseObserver.onNext(response);
              responseObserver.onCompleted();
            })
            .build();
  }
}
