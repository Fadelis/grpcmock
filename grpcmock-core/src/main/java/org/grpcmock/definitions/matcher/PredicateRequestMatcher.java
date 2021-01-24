package org.grpcmock.definitions.matcher;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

/**
 * @author Fadelis
 */
public class PredicateRequestMatcher<ReqT> implements RequestMatcher<ReqT> {

  private final Predicate<List<ReqT>> requestsPredicate;

  public PredicateRequestMatcher(@Nonnull Predicate<List<ReqT>> requestsPredicate) {
    Objects.requireNonNull(requestsPredicate);
    this.requestsPredicate = requestsPredicate;
  }

  @Override
  public boolean matches(List<ReqT> requests) {
    return requestsPredicate.test(requests);
  }
}
