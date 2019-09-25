package org.grpcmock.definitions.matcher;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

/**
 * @author Fadelis
 */
public class PredicateRequestMatcher<ReqT> implements RequestMatcher<ReqT> {

  private final Predicate<ReqT> requestPredicate;

  public PredicateRequestMatcher(@Nonnull Predicate<ReqT> requestPredicate) {
    Objects.requireNonNull(requestPredicate);
    this.requestPredicate = requestPredicate;
  }

  @Override
  public boolean matches(ReqT request) {
    return requestPredicate.test(request);
  }
}
