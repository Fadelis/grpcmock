package org.grpcmock.definitions.verification;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.matcher.HeadersMatcher;
import org.grpcmock.definitions.matcher.HeadersMatcherBuilderImpl;
import org.grpcmock.definitions.matcher.RequestMatcher;
import org.grpcmock.definitions.matcher.RequestMatcherBuilderImpl;
import org.grpcmock.definitions.matcher.StatusMatcher;
import org.grpcmock.definitions.matcher.StatusMatcherBuilderImpl;
import org.grpcmock.definitions.verification.steps.RequestPatternBuilderStep;
import org.grpcmock.exception.GrpcMockException;
import org.grpcmock.interceptors.CapturedRequest;

/**
 * Builder for {@link RequestPattern} to define conditions for {@link CapturedRequest} matching.
 *
 * @author Fadelis
 */
public class RequestPatternBuilderImpl<ReqT> implements RequestPatternBuilderStep<ReqT> {

  private final MethodDescriptor<ReqT, ?> method;
  private final StatusMatcherBuilderImpl statusMatcherBuilder = StatusMatcher.builder();
  private final HeadersMatcherBuilderImpl headersMatcherBuilder = HeadersMatcher.builder();
  private final RequestMatcherBuilderImpl<ReqT> requestMatcherBuilder = RequestMatcher.builder();

  public RequestPatternBuilderImpl(@Nonnull MethodDescriptor<ReqT, ?> method) {
    Objects.requireNonNull(method);
    this.method = method;
  }

  @Override
  public RequestPatternBuilderStep<ReqT> withStatus(@Nonnull Predicate<Status> predicate) {
    statusMatcherBuilder.withStatus(predicate);
    return this;
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
    requestMatcherBuilder.withRequest(requestPredicate);
    return this;
  }

  @Override
  public RequestPatternBuilderStep<ReqT> withRequests(@Nonnull Predicate<List<ReqT>> requestsPredicate) {
    Objects.requireNonNull(requestsPredicate);
    if (method.getType().clientSendsOneMessage()) {
      throw new GrpcMockException("This builder step is only applicable to client or bidi streaming methods");
    }
    requestMatcherBuilder.withRequests(requestsPredicate);
    return this;
  }

  public void clearRequestsPredicates() {
    this.requestMatcherBuilder.clearRequestsPredicates();
  }

  @Override
  public RequestPattern<ReqT> build() {
    return new RequestPattern<>(
        method,
        statusMatcherBuilder.build(),
        headersMatcherBuilder.build(),
        requestMatcherBuilder.build()
    );
  }
}
