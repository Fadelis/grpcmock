package org.grpcmock.ip.definitions.matcher.steps;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.ip.definitions.BuilderStep;

/**
 * @author Fadelis
 */
public interface FirstRequestMatcherBuilderStep<BUILDER extends FirstRequestMatcherBuilderStep<BUILDER, ReqT>, ReqT> extends
    BuilderStep {

  /**
   * <p>Adds a request matcher for the stub, which will trigger only if incoming first request
   * satisfies given {@link Predicate}.
   * <p>This is used for client or bidi streaming method calls. Only the first request can be used for matching,
   * as a stub needs to be selected at this point or the call will be rejected.
   * <p>Subsequent request matchers will replace the old one.
   */
  BUILDER withFirstRequest(@Nonnull Predicate<ReqT> requestPredicate);

  /**
   * <p>Adds a request matcher for the stub, which will trigger only if incoming first request
   * is equal to the provided one.
   * <p>This is used for client or bidi streaming method calls. Only the first request can be used for matching,
   * as a stub needs to be selected at this point or the call will be rejected.
   * <p>Subsequent request matchers will replace the old one.
   */
  default BUILDER withFirstRequest(@Nonnull ReqT request) {
    Objects.requireNonNull(request);
    return withFirstRequest(request::equals);
  }
}
