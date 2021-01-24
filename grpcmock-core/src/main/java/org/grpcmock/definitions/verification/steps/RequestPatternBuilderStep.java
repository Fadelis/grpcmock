package org.grpcmock.definitions.verification.steps;

import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.matcher.steps.HeadersMatcherBuilderStep;
import org.grpcmock.definitions.matcher.steps.RequestMatcherBuilderStep;
import org.grpcmock.definitions.matcher.steps.StreamRequestMatcherBuilderStep;
import org.grpcmock.definitions.verification.RequestPattern;

/**
 * @author Fadelis
 */
public interface RequestPatternBuilderStep<ReqT> extends
    BuilderStep,
    HeadersMatcherBuilderStep<RequestPatternBuilderStep<ReqT>>,
    RequestMatcherBuilderStep<RequestPatternBuilderStep<ReqT>, ReqT>,
    StreamRequestMatcherBuilderStep<RequestPatternBuilderStep<ReqT>, ReqT> {

  /**
   * Builds a {@link RequestPattern} with given configuration.
   */
  RequestPattern<ReqT> build();
}
