package org.grpcmock.ip.definitions.verification.steps;

import org.grpcmock.ip.definitions.BuilderStep;
import org.grpcmock.ip.definitions.matcher.steps.HeadersMatcherBuilderStep;
import org.grpcmock.ip.definitions.matcher.steps.RequestMatcherBuilderStep;
import org.grpcmock.ip.definitions.matcher.steps.StatusMatcherBuilderStep;
import org.grpcmock.ip.definitions.matcher.steps.StreamRequestMatcherBuilderStep;
import org.grpcmock.ip.definitions.verification.RequestPattern;

/**
 * @author Fadelis
 */
public interface RequestPatternBuilderStep<ReqT> extends
    BuilderStep,
    StatusMatcherBuilderStep<RequestPatternBuilderStep<ReqT>>,
    HeadersMatcherBuilderStep<RequestPatternBuilderStep<ReqT>>,
    RequestMatcherBuilderStep<RequestPatternBuilderStep<ReqT>, ReqT>,
    StreamRequestMatcherBuilderStep<RequestPatternBuilderStep<ReqT>, ReqT> {

  /**
   * Builds a {@link RequestPattern} with given configuration.
   */
  RequestPattern<ReqT> build();
}
