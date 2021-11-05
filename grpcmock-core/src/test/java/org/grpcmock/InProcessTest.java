package org.grpcmock;

import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.testing.protobuf.SimpleServiceGrpc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.grpcmock.GrpcMock.*;

class InProcessTest extends TestBase {

    private static String name;

    @BeforeAll
    static void beforeAll() {
        InProcessGrpcMockBuilder inProcessGrpcMockBuilder = inProcessGrpcMock();
        name = inProcessGrpcMockBuilder.getName();
        GrpcMock.configureFor(inProcessGrpcMockBuilder.build().start());
    }

    @AfterEach
    void cleanup() {
        serverChannel.shutdownNow();
    }

    @BeforeEach
    void setUp() {
        GrpcMock.resetMappings();

        serverChannel = InProcessChannelBuilder
                .forName(name)
                .directExecutor()
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
