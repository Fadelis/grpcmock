package org.grpcmock.springboot;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

import java.util.List;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

/**
 * Creates a {@link GrpcMockContextCustomizer} when {@link AutoConfigureGrpcMock} is present
 * on the test class.
 *
 * @author Fadelis
 */
public class GrpcMockContextCustomizerFactory implements ContextCustomizerFactory {

  @Override
  public ContextCustomizer createContextCustomizer(
      Class<?> testClass,
      List<ContextConfigurationAttributes> configAttributes
  ) {
    AutoConfigureGrpcMock annotation = findMergedAnnotation(testClass, AutoConfigureGrpcMock.class);
    if (annotation == null) {
      return null;
    }
    return new GrpcMockContextCustomizer(annotation);
  }
}
