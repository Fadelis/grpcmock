package org.grpcmock.springboot;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@SpringBootTest(classes = TestApplication.class, webEnvironment = WebEnvironment.NONE)
@AutoConfigureGrpcMock(port = -1)
class TestClass {

  @Value("${grpcmock.server.name}")
  private String inProcessName;

  private ManagedChannel channel;

  @BeforeEach
  void setupChannel() {
    channel = InProcessChannelBuilder.forName(inProcessName).build();
  }

  @AfterEach
  void shutdownChannel() {
    Optional.ofNullable(channel).ifPresent(ManagedChannel::shutdownNow);
  }
}
