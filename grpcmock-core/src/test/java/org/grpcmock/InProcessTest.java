package org.grpcmock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.grpcmock.GrpcMock.bidiStreamingMethod;
import static org.grpcmock.GrpcMock.clientStreamingMethod;
import static org.grpcmock.GrpcMock.getGlobalInProcessName;
import static org.grpcmock.GrpcMock.inProcessGrpcMock;
import static org.grpcmock.GrpcMock.response;
import static org.grpcmock.GrpcMock.serverStreamingMethod;
import static org.grpcmock.GrpcMock.stubFor;
import static org.grpcmock.GrpcMock.unaryMethod;

import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.testing.protobuf.SimpleServiceGrpc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InProcessTest extends TestBase {

  @BeforeAll
  static void createServer() {
    GrpcMock.configureFor(inProcessGrpcMock().build().start());
  }

  @Override
  @BeforeEach
  void setup() {
    GrpcMock.resetMappings();

    serverChannel = InProcessChannelBuilder.forName(getGlobalInProcessName())
        .usePlaintext()
        .build();
  }

  @Test
  void should_return_a_unary_response() {
    stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response(response)));

    SimpleServiceGrpc.SimpleServiceBlockingStub serviceStub = SimpleServiceGrpc.newBlockingStub(serverChannel);

    assertThat(serviceStub.unaryRpc(request)).isEqualTo(response);
  }

  @Test
  void should_return_a_unary_response_with_server_streaming() {
    stubFor(serverStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .willReturn(response));

    SimpleServiceGrpc.SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncStubCall(request, serviceStub::serverStreamingRpc)).containsExactly(response);
  }

  @Test
  void should_return_a_response_with_client_streaming() {
    stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
        .willReturn(response));

    SimpleServiceGrpc.SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::clientStreamingRpc, request, request2)).containsExactly(response);
  }

  @Test
  void should_return_a_response_when_first_request_satisfies_defined_matching_condition() {
    stubFor(bidiStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
        .withFirstRequest(request)
        .willProxyTo(proxyingResponse(response)));

    SimpleServiceGrpc.SimpleServiceStub serviceStub = SimpleServiceGrpc.newStub(serverChannel);

    assertThat(asyncClientStreamingCall(serviceStub::bidiStreamingRpc, request, request2)).containsExactly(response);
  }
}
