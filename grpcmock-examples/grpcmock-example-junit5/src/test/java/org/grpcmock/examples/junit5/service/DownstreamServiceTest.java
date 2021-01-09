package org.grpcmock.examples.junit5.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.grpcmock.GrpcMock.calledMethod;
import static org.grpcmock.GrpcMock.stubFor;
import static org.grpcmock.GrpcMock.unaryMethod;
import static org.grpcmock.GrpcMock.verifyThat;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import java.util.Optional;
import org.grpcmock.GrpcMock;
import org.grpcmock.examples.v1.DownstreamServiceGrpc;
import org.grpcmock.examples.v1.GetDownstreamMessageRequest;
import org.grpcmock.examples.v1.GetDownstreamMessageResponse;
import org.grpcmock.junit5.GrpcMockExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Fadelis
 */
@ExtendWith(GrpcMockExtension.class)
class DownstreamServiceTest {

  private ManagedChannel channel;
  private DownstreamService downstreamService;

  @BeforeEach
  void setup() {
    channel = ManagedChannelBuilder.forAddress("localhost", GrpcMock.getGlobalPort())
        .usePlaintext()
        .build();
    downstreamService = new DownstreamService(DownstreamServiceGrpc.newBlockingStub(channel));
  }

  @AfterEach
  void cleanup() {
    Optional.ofNullable(channel).ifPresent(ManagedChannel::shutdownNow);
  }

  @Test
  void should_return_correct_downstream_message() {
    String requestMessage = "my-message";
    String responseMessage = "this-is-your-response";
    GetDownstreamMessageRequest expectedRequest = GetDownstreamMessageRequest.newBuilder()
        .setMessage(requestMessage)
        .build();

    stubFor(unaryMethod(DownstreamServiceGrpc.getGetDownstreamMessageMethod())
        .willReturn(GetDownstreamMessageResponse.newBuilder()
            .setMessage(responseMessage)
            .build()));

    assertThat(downstreamService.getDownstreamMessage(requestMessage))
        .isEqualTo(responseMessage);
    verifyThat(calledMethod(DownstreamServiceGrpc.getGetDownstreamMessageMethod())
        .withRequest(expectedRequest));
  }

  @Test
  void should_propagate_downstream_error() {
    String requestMessage = "my-message";
    GetDownstreamMessageRequest expectedRequest = GetDownstreamMessageRequest.newBuilder()
        .setMessage(requestMessage)
        .build();

    stubFor(unaryMethod(DownstreamServiceGrpc.getGetDownstreamMessageMethod())
        .willReturn(Status.INVALID_ARGUMENT.withDescription("some error")));

    assertThatThrownBy(() -> downstreamService.getDownstreamMessage(requestMessage))
        .hasMessage("INVALID_ARGUMENT: some error");
    verifyThat(calledMethod(DownstreamServiceGrpc.getGetDownstreamMessageMethod())
        .withRequest(expectedRequest));
  }
}