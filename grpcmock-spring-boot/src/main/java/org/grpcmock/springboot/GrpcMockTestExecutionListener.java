package org.grpcmock.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Dirties the test context if WireMock was running on a fixed port.
 *
 * @author Fadelis
 */
public final class GrpcMockTestExecutionListener extends AbstractTestExecutionListener {

  private static final Logger log = LoggerFactory.getLogger(GrpcMockTestExecutionListener.class);

  @Override
  public void beforeTestClass(TestContext testContext) {
    if (isInvalidContext(testContext)) {
      return;
    }
    if (!portOrNameIsFixed(testContext)) {
      grpcMockConfig(testContext).init();
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
      grpcMockConfig(testContext).resetAll();
    }
  }

  @Override
  public void afterTestMethod(TestContext testContext) {
    if (!isInvalidContext(testContext)) {
      log.debug("Resetting gRPC Mock mappings after a test");
      grpcMockConfig(testContext).resetAll();
    }
  }

  private boolean isInvalidContext(TestContext testContext) {
    return applicationContextBroken(testContext)
        || wireMockConfigurationMissing(testContext)
        || annotationMissing(testContext);
  }

  private boolean annotationMissing(TestContext testContext) {
    if (testContext.getTestClass().getAnnotationsByType(AutoConfigureGrpcMock.class).length == 0) {
      log.debug(String.format(
          "No @AutoConfigureGrpcMock annotation found on [%s]. Skipping",
          testContext.getTestClass()));
      return true;
    }
    return false;
  }

  private boolean wireMockConfigurationMissing(TestContext testContext) {
    boolean missing = !testContext(testContext).containsBean(GrpcMockConfiguration.class.getName());
    log.debug("GrpcMockConfiguration is missing [" + missing + "]");
    return missing;
  }

  private ApplicationContext testContext(TestContext testContext) {
    return testContext.getApplicationContext();
  }

  private boolean applicationContextBroken(TestContext testContext) {
    try {
      testContext.getApplicationContext();
      return false;
    } catch (Exception ex) {
      log.debug("Application context is broken due to", ex);
      return true;
    }
  }

  private GrpcMockConfiguration grpcMockConfig(TestContext testContext) {
    return testContext(testContext).getBean(GrpcMockConfiguration.class);
  }

  private GrpcMockProperties grpcMockProperties(TestContext testContext) {
    return testContext(testContext).getBean(GrpcMockProperties.class);
  }

  private boolean portOrNameIsFixed(TestContext testContext) {
    return !grpcMockProperties(testContext).getServer().isPortDynamic();
  }
}
