package org.grpcmock.definitions.matcher.steps;

import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.matcher.HeadersMatcher;

/**
 * @author Fadelis
 */
public interface HeadersMatcherBuilder extends
    BuilderStep,
    HeadersMatcherBuilderStep<HeadersMatcherBuilder> {

  HeadersMatcher build();
}
