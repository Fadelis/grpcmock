package org.grpcmock.definitions.verification;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.matcher.HeadersMatcher;
import org.grpcmock.definitions.matcher.PredicateRequestMatcher;
import org.grpcmock.definitions.matcher.steps.HeadersMatcherBuilder;
import org.grpcmock.definitions.verification.steps.RequestPatternBuilderStep;
import org.grpcmock.exception.GrpcMockException;

/**
 * Builder for {@link RequestPattern} to define conditions for {@link CapturedRequest} matching.
 *
 * @author Fadelis
 */
public class RequestPatternBuilderImpl<ReqT> implements RequestPatternBuilderStep<ReqT> {

  private final MethodDescriptor<ReqT, ?> method;
  private final HeadersMatcherBuilder headersMatcherBuilder = HeadersMatcher.builder();
  private final List<Predicate<List<ReqT>>> requestsPredicates = new ArrayList<>();

  public RequestPatternBuilderImpl(@Nonnull MethodDescriptor<ReqT, ?> method) {
    Objects.requireNonNull(method);
    this.method = method;
  }

  @Override
  public <T> RequestPatternBuilderImpl<ReqT> withHeader(
      @Nonnull Metadata.Key<T> headerKey,
      @Nonnull Predicate<T> predicate
  ) {
    headersMatcherBuilder.withHeader(headerKey, predicate);
    return this;
  }

  @Override
  public RequestPatternBuilderImpl<ReqT> withRequest(@Nonnull Predicate<ReqT> requestPredicate) {
    Objects.requireNonNull(requestPredicate);
    if (!method.getType().clientSendsOneMessage()) {
      throw new GrpcMockException("This builder step is only applicable to unary or server streaming methods");
    }
    clearRequestsPredicates();
    this.requestsPredicates.add(list -> list.size() == 1 && requestPredicate.test(list.get(0)));
    return this;
  }

  @Override
  public RequestPatternBuilderStep<ReqT> withRequests(@Nonnull Predicate<List<ReqT>> requestsPredicate) {
    Objects.requireNonNull(requestsPredicate);
    if (method.getType().clientSendsOneMessage()) {
      throw new GrpcMockException("This builder step is only applicable to client or bidi streaming methods");
    }
    this.requestsPredicates.add(requestsPredicate);
    return this;
  }

  public void clearRequestsPredicates() {
    this.requestsPredicates.clear();
  }

  @Override
  public RequestPattern<ReqT> build() {
    return new RequestPattern<>(
        method,
        headersMatcherBuilder.build(),
        requestsPredicates.stream()
            .reduce(Predicate::and)
            .map(PredicateRequestMatcher::new)
            .orElse(null)
    );
  }
}
