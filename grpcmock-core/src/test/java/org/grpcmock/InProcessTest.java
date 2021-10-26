package org.grpcmock;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.testing.protobuf.SimpleRequest;
import io.grpc.testing.protobuf.SimpleResponse;
import io.grpc.testing.protobuf.SimpleServiceGrpc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.grpcmock.GrpcMock.*;

class InProcessTest {

    static final String RESPONSE_MESSAGE = "message-1";
    static final String REQUEST_MESSAGE = "request-1";

    final SimpleRequest request = SimpleRequest.newBuilder().setRequestMessage(REQUEST_MESSAGE).build();
    final SimpleResponse response = SimpleResponse.newBuilder().setResponseMessage(RESPONSE_MESSAGE).build();

    private ManagedChannel serverChannel;

    @BeforeAll
    static void beforeAll() {
        GrpcMock.configureFor(grpcMock("server-1").build().start());
    }

    @AfterEach
    void cleanup() {
        serverChannel.shutdownNow();
    }

    @BeforeEach
    void setUp() {
        GrpcMock.resetMappings();

        serverChannel = InProcessChannelBuilder
                .forName("server-1")
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
}
