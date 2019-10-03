package org.grpcmock;

import static org.grpcmock.GrpcMock.getGlobalPort;
import static org.grpcmock.GrpcMock.grpcMock;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.MetadataUtils;
import io.grpc.testing.protobuf.SimpleRequest;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Fadelis
 */
public class TestBase {

  static final String HEADER_1 = "header-1";
  static final String HEADER_2 = "header-2";

  final SimpleRequest request = SimpleRequest.getDefaultInstance();
  ManagedChannel serverChannel;

  @BeforeAll
  static void createServer() {
    GrpcMock.configureFor(grpcMock(0).build().start());
  }

  @BeforeEach
  void setup() {
    GrpcMock.resetMappings();
    serverChannel = ManagedChannelBuilder
        .forAddress("localhost", getGlobalPort())
        .usePlaintext()
        .build();
  }

  @AfterEach
  void cleanup() {
    serverChannel.shutdownNow();
  }

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
    headers.forEach((key, value) -> metadata
        .put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value));

    return MetadataUtils.attachHeaders(baseStub, metadata);
  }
}
