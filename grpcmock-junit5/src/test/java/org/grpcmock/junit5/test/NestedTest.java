package org.grpcmock.junit5.test;

import org.grpcmock.junit5.GrpcMockExtension;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Fadelis
 */
@ExtendWith(GrpcMockExtension.class)
class NestedTest extends TestBase {

  @Nested
  class FirstNestedClass {

    @Test
    void should_run_nested_test_1() {
      runAndAssertSimpleHealthCheckRequest();
    }
  }

  @Nested
  class SecondNestedClass {

    @Test
    void should_run_nested_test_2() {
      runAndAssertSimpleHealthCheckRequest();
    }
  }
}
