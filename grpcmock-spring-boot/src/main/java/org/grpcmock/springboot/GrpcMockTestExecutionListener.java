package org.grpcmock.springboot;

import org.grpcmock.GrpcMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Resets gRPC Mock mappings between tests and dirties the context for fixed port/name configs.
 *
 * @author Fadelis
 */
public final class GrpcMockTestExecutionListener extends AbstractTestExecutionListener {

  private static final Logger log = LoggerFactory.getLogger(GrpcMockTestExecutionListener.class);

  @Override
  public void prepareTestInstance(TestContext testContext) {
    if (isInvalidContext(testContext)) {
      return;
    }
    if (!portOrNameIsFixed(testContext)) {
      grpcMock(testContext).resetAll();
      GrpcMock.configureFor(grpcMock(testContext));
    }
  }

  @Override
  public void afterTestClass(TestContext testContext) {
    if (isInvalidContext(testContext)) {
      return;
    }
    if (portOrNameIsFixed(testContext)) {
      log.warn("You've used fixed ports or InProcess server names for GrpcMock setup - "
          + "will mark context as dirty. Please use random ports or names, as much "
          + "as possible. Your tests will be faster and more reliable and this "
          + "warning will go away");
      testContext.markApplicationContextDirty(DirtiesContext.HierarchyMode.EXHAUSTIVE);
    } else {
      log.debug("Resetting gRPC Mock mappings after test class for dynamic port server");
      grpcMock(testContext).resetAll();
    }
  }

  @Override
  public void afterTestMethod(TestContext testContext) {
    if (!isInvalidContext(testContext)) {
      log.debug("Resetting gRPC Mock mappings after a test");
      grpcMock(testContext).resetAll();
    }
  }

  private boolean isInvalidContext(TestContext testContext) {
    return applicationContextBroken(testContext)
        || annotationMissing(testContext)
        || grpcMockBeanMissing(testContext);
  }

  private boolean annotationMissing(TestContext testContext) {
    if (testContext.getTestClass().getAnnotationsByType(AutoConfigureGrpcMock.class).length == 0) {
      log.debug("No @AutoConfigureGrpcMock annotation found on [{}]. Skipping",
          testContext.getTestClass());
      return true;
    }
    return false;
  }

  private boolean grpcMockBeanMissing(TestContext testContext) {
    boolean missing = !context(testContext).containsBean(GrpcMockContextCustomizer.GRPCMOCK_BEAN_NAME);
    log.debug("GrpcMock bean is missing [{}]", missing);
    return missing;
  }

  private ApplicationContext context(TestContext testContext) {
    return testContext.getApplicationContext();
  }

  private boolean applicationContextBroken(TestContext testContext) {
    try {
      if (testContext.hasApplicationContext()) {
        testContext.getApplicationContext();
      }
      return false;
    } catch (Exception ex) {
      log.debug("Application context is broken due to", ex);
      return true;
    }
  }

  private GrpcMock grpcMock(TestContext testContext) {
    return context(testContext).getBean(GrpcMockContextCustomizer.GRPCMOCK_BEAN_NAME, GrpcMock.class);
  }

  private boolean portOrNameIsFixed(TestContext testContext) {
    return !Boolean.TRUE.equals(
        context(testContext).getEnvironment().getProperty("grpcmock.server.port-dynamic", Boolean.class));
  }
}
