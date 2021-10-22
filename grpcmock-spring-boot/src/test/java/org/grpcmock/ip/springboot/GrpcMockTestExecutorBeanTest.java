package org.grpcmock.ip.springboot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import org.grpcmock.ip.springboot.GrpcMockTestExecutorBeanTest.ExecutorConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * @author Fadelis
 */
@SpringJUnitConfig
@Import(ExecutorConfiguration.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = WebEnvironment.NONE)
@AutoConfigureGrpcMock(executorBeanName = "myExecutor")
class GrpcMockTestExecutorBeanTest extends TestBase {

  @MockBean
  private Executor myExecutor;

  @BeforeEach
  void setup() {
    doAnswer(i -> {
      ForkJoinPool.commonPool().execute((Runnable) i.getArgument(0));
      return null;
    }).when(myExecutor).execute(any());
  }

  @Test
  void should_use_configured_executor_bean() {
    simpleHealthCheckRequest();

    verify(myExecutor, atLeastOnce()).execute(any());
  }

  @TestConfiguration
  public static class ExecutorConfiguration {

    @Bean
    public Executor myExecutor() {
      return Executors.newSingleThreadExecutor();
    }
  }
}