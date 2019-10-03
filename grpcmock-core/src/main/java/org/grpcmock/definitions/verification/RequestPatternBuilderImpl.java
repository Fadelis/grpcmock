package org.grpcmock.definitions.verification;

import io.grpc.MethodDescriptor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.matcher.PredicateHeadersMatcher;
import org.grpcmock.definitions.matcher.PredicateRequestMatcher;
import org.grpcmock.definitions.verification.steps.RequestPatternBuilderStep;

/**
 * Builder for {@link RequestPattern} to define conditions for {@link CapturedRequest} matching.
 *
 * @author Fadelis
 */
public class RequestPatternBuilderImpl<ReqT> implements RequestPatternBuilderStep<ReqT> {

  private final MethodDescriptor<ReqT, ?> method;
  private final Map<String, Predicate<String>> headerPredicates = new HashMap<>();
  private Predicate<List<ReqT>> requestPredicate;

  public RequestPatternBuilderImpl(@Nonnull MethodDescriptor<ReqT, ?> method) {
    Objects.requireNonNull(method);
    this.method = method;
  }

  @Override
  public RequestPatternBuilderImpl<ReqT> withHeader(@Nonnull String headerName,
      @Nonnull Predicate<String> valuePredicate) {
    Objects.requireNonNull(headerName);
    Objects.requireNonNull(valuePredicate);
    this.headerPredicates.put(headerName, valuePredicate);
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
        new PredicateHeadersMatcher(headerPredicates),
        Optional.ofNullable(requestPredicate).map(PredicateRequestMatcher::new).orElse(null)
    );
  }
}
