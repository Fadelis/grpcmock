package org.grpcmock.junit5.test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.inprocess.InProcessChannelBuilder;
import org.grpcmock.GrpcMock;
import org.grpcmock.junit5.InProcessGrpcMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Fadelis
 */
@ExtendWith(InProcessGrpcMockExtension.class)
class InProcessExtensionTest extends TestBase {

  @BeforeEach
  void setupChannel() {
    serverChannel = InProcessChannelBuilder.forName(GrpcMock.getGlobalInProcessName())
        .usePlaintext()
        .build();
  }
  
  @Test
  void should_test_default_extension_resetting_after_test_first() {
    runAndAssertSimpleHealthCheckRequest();
  }

  @Test
  void should_test_default_extension_resetting_after_test_second() {
    HealthCheckResponse response = HealthCheckResponse.getDefaultInstance();
    HealthCheckRequest request = HealthCheckRequest.getDefaultInstance();

    assertThatThrownBy(() -> runAndAssertHealthCheckRequest(request, response))
        .hasMessage("UNIMPLEMENTED: Method not found: grpc.health.v1.Health/Check");
  }
}
