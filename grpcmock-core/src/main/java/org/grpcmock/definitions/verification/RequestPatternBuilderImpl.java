package org.grpcmock.definitions.verification;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.matcher.HeadersMatcher;
import org.grpcmock.definitions.matcher.PredicateRequestMatcher;
import org.grpcmock.definitions.matcher.steps.HeadersMatcherBuilder;
import org.grpcmock.definitions.verification.steps.RequestPatternBuilderStep;

/**
 * Builder for {@link RequestPattern} to define conditions for {@link CapturedRequest} matching.
 *
 * @author Fadelis
 */
public class RequestPatternBuilderImpl<ReqT> implements RequestPatternBuilderStep<ReqT> {

  private final MethodDescriptor<ReqT, ?> method;
  private final HeadersMatcherBuilder headersMatcherBuilder = HeadersMatcher.builder();
  private Predicate<List<ReqT>> requestPredicate;

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
    this.requestPredicate = list -> list.stream().allMatch(requestPredicate);
    return this;
  }

  @Override
  public RequestPattern<ReqT> build() {
    return new RequestPattern<>(
        method,
        headersMatcherBuilder.build(),
        Optional.ofNullable(requestPredicate).map(PredicateRequestMatcher::new).orElse(null)
    );
  }
}
