package org.grpcmock.definitions.matcher.steps;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.BuilderStep;

/**
 * @author Fadelis
 */
public interface RequestMatcherBuilderStep<BUILDER extends BuilderStep, ReqT> extends BuilderStep {

  /**
   * <p>Adds a request matcher for the stub, which will trigger only if incoming request
   * satisfies given {@link Predicate}.
   * <p>Subsequent matchers for the request will replace the old one.
   */
  BUILDER withRequest(@Nonnull Predicate<ReqT> requestPredicate);

  /**
   * <p>Adds a request matcher for the stub, which will trigger only if incoming request
   * is equal to the provided one.
   * <p>Subsequent matchers for the request will replace the old one.
   */
  default BUILDER withRequest(@Nonnull ReqT request) {
    Objects.requireNonNull(request);
    return withRequest(request::equals);
  }
}
