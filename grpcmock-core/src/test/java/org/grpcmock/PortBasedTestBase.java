package org.grpcmock;

import static org.grpcmock.GrpcMock.getGlobalPort;
import static org.grpcmock.GrpcMock.grpcMock;

import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Fadelis
 */
public class PortBasedTestBase extends TestBase {

  @BeforeAll
  static void createServer() {
    GrpcMock.configureFor(grpcMock().build().start());
  }

  @BeforeEach
  void setup() {
    GrpcMock.resetMappings();

    serverChannel = ManagedChannelBuilder.forAddress("localhost", getGlobalPort())
        .usePlaintext()
        .build();
  }

  @AfterEach
  void cleanup() {
    serverChannel.shutdownNow();
  }
}
