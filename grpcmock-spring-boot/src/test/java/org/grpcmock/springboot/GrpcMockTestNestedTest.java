package org.grpcmock.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

/**
 * Verifies that {@link AutoConfigureGrpcMock} on the outer class is inherited by {@link Nested}
 * inner test classes.
 *
 * @author Fadelis
 */
@SpringBootTest(classes = TestApplication.class, webEnvironment = WebEnvironment.NONE)
@AutoConfigureGrpcMock
class GrpcMockTestNestedTest extends TestBase {

  @Test
  void outer_class_should_have_valid_port() {
    assertThat(grpcMockPort).isGreaterThan(0);
    simpleHealthCheckRequest();
  }

  @Nested
  class InnerTest extends TestBase {

    @Test
    void nested_class_should_inherit_grpcmock_configuration() {
      assertThat(grpcMockPort).isGreaterThan(0);
      simpleHealthCheckRequest();
    }
  }
}
