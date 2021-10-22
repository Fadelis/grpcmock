package org.grpcmock.ip.definitions.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.ip.definitions.matcher.steps.RequestMatcherBuilderStep;
import org.grpcmock.ip.definitions.matcher.steps.StreamRequestMatcherBuilderStep;

/**
 * @author Fadelis
 */
public class RequestMatcherBuilderImpl<ReqT> implements
    RequestMatcherBuilderStep<RequestMatcherBuilderImpl<ReqT>, ReqT>,
    StreamRequestMatcherBuilderStep<RequestMatcherBuilderImpl<ReqT>, ReqT> {

  private final List<Predicate<List<ReqT>>> requestsPredicates = new ArrayList<>();

  RequestMatcherBuilderImpl() {
  }

  @Override
  public RequestMatcherBuilderImpl<ReqT> withRequest(@Nonnull Predicate<ReqT> requestPredicate) {
    Objects.requireNonNull(requestPredicate);
    clearRequestsPredicates();
    this.requestsPredicates.add(list -> list.size() == 1 && requestPredicate.test(list.get(0)));
    return this;
  }

  @Override
  public RequestMatcherBuilderImpl<ReqT> withRequests(@Nonnull Predicate<List<ReqT>> requestsPredicate) {
    Objects.requireNonNull(requestsPredicate);
    this.requestsPredicates.add(requestsPredicate);
    return this;
  }

  public void clearRequestsPredicates() {
    this.requestsPredicates.clear();
  }

  public RequestMatcher<ReqT> build() {
    return requests -> requestsPredicates.stream().allMatch(predicate -> predicate.test(requests));
  }
}
