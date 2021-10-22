package org.grpcmock.ip.junit5.test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import org.grpcmock.ip.junit5.GrpcMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Fadelis
 */
@ExtendWith(GrpcMockExtension.class)
class DefaultExtensionTest extends TestBase {

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
