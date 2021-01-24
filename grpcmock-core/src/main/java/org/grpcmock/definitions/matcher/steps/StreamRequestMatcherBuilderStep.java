package org.grpcmock.definitions.matcher.steps;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.BuilderStep;

/**
 * @author Fadelis
 */
public interface StreamRequestMatcherBuilderStep<BUILDER extends StreamRequestMatcherBuilderStep<BUILDER, ReqT>, ReqT> extends
    BuilderStep {

  /**
   * <p>Adds a requests' matcher, which checks if all received stream requests satisfies given {@link Predicate}.
   * <p>Only applicable to client and bidi streaming methods.
   * <p>Subsequent requests matchers will added as additional conditions.
   */
  BUILDER withRequests(@Nonnull Predicate<List<ReqT>> requestsPredicate);

  /**
   * <p>Adds a requests' matcher, which checks if all received stream requests are equal to provided list.
   * <p>Only applicable to client and bidi streaming methods.
   * <p>Subsequent requests matchers will added as additional conditions.
   */
  default BUILDER withRequests(@Nonnull List<ReqT> requests) {
    Objects.requireNonNull(requests);
    return withRequests(requests::equals);
  }

  /**
   * <p>Adds a requests' matcher, which checks if all received stream requests are equal to provided array.
   * <p>Only applicable to client and bidi streaming methods.
   * <p>Subsequent requests matchers will added as additional conditions.
   */
  default BUILDER withRequests(@Nonnull ReqT... requests) {
    Objects.requireNonNull(requests);
    return withRequests(Arrays.asList(requests));
  }

  /**
   * <p>Adds a requests' matcher, which checks if received stream requests number is equal to provided one.
   * <p>Only applicable to client or bidi streaming methods.
   * <p>Subsequent requests matchers will added as additional conditions.
   */
  default BUILDER withNumberOfRequests(int size) {
    return withRequests(list -> list.size() == size);
  }

  /**
   * <p>Adds a requests' matcher, which checks if any of the received stream requests satisfies given {@link Predicate}.
   * <p>Only applicable to client and bidi streaming methods.
   * <p>Subsequent requests matchers will added as additional conditions.
   */
  default BUILDER withRequestsContaining(@Nonnull Predicate<ReqT> requestPredicate) {
    Objects.requireNonNull(requestPredicate);
    return withRequests(list -> list.stream().anyMatch(requestPredicate));
  }

  /**
   * <p>Adds a requests' matcher, which checks if any of the received stream requests is equal to given request object.
   * <p>Only applicable to client and bidi streaming methods.
   * <p>Subsequent requests matchers will added as additional conditions.
   */
  default BUILDER withRequestsContaining(@Nonnull ReqT request) {
    Objects.requireNonNull(request);
    return withRequestsContaining(request::equals);
  }

  /**
   * <p>Adds a requests' matcher, which checks if a request at given index satisfies given {@link Predicate}.
   * <p>Only applicable to client and bidi streaming methods.
   * <p>Subsequent requests matchers will added as additional conditions.
   */
  default BUILDER withRequestAtIndex(int index, @Nonnull Predicate<ReqT> requestPredicate) {
    Objects.requireNonNull(requestPredicate);
    return withRequests(list -> list.size() > index && requestPredicate.test(list.get(index)));
  }

  /**
   * <p>Adds a requests' matcher, which checks if a request at given index is equal to given request object.
   * <p>Only applicable to client or bidi streaming methods.
   * <p>Subsequent requests matchers will added as additional conditions.
   */
  default BUILDER withRequestAtIndex(int index, @Nonnull ReqT request) {
    Objects.requireNonNull(request);
    return withRequestAtIndex(index, request::equals);
  }

  /**
   * <p>Adds a requests' matcher, which checks if the first received request satisfies given {@link Predicate}.
   * <p>Only applicable to client or bidi streaming methods.
   * <p>Subsequent requests matchers will added as additional conditions.
   */
  default BUILDER withFirstRequest(@Nonnull Predicate<ReqT> requestPredicate) {
    return withRequestAtIndex(0, requestPredicate);
  }

  /**
   * <p>Adds a requests' matcher, which checks if the first received request is equal to given request object.
   * <p>Only applicable to client or bidi streaming methods.
   * <p>Subsequent requests matchers will added as additional conditions.
   */
  default BUILDER withFirstRequest(@Nonnull ReqT request) {
    Objects.requireNonNull(request);
    return withFirstRequest(request::equals);
  }
}
