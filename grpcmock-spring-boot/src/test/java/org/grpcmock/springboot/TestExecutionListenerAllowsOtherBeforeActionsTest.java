package org.grpcmock.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import org.grpcmock.springboot.TestExecutionListenerAllowsOtherBeforeActionsTest.ApplicationEventListener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.test.annotation.DirtiesContext;

/**
 * @author Fadelis
 */
@SpringBootTest(classes = TestApplication.class, webEnvironment = WebEnvironment.NONE)
@AutoConfigureGrpcMock
@DirtiesContext
@Import(ApplicationEventListener.class)
class TestExecutionListenerAllowsOtherBeforeActionsTest extends TestBase {

  private static boolean beforeAllRunSuccessful;
  private static boolean applicationContextInitialized;

  @BeforeAll
  static void checkIfApplicationContextNotInitialized() {
    beforeAllRunSuccessful = !applicationContextInitialized;
  }

  @Test
  void should_run_before_all_action_before_application_context_is_available() {
    // check that beforeAll method was actually run before the application context was initialized
    assertThat(beforeAllRunSuccessful).isTrue();
    // check that grpc mock instance is running
    simpleHealthCheckRequest();
  }


  public static class ApplicationEventListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
      applicationContextInitialized = true;
    }
  }
}
