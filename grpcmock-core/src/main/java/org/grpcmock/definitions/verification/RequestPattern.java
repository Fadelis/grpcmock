package org.grpcmock.definitions.verification;

import static java.util.Optional.ofNullable;

import io.grpc.MethodDescriptor;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.grpcmock.definitions.matcher.HeadersMatcher;
import org.grpcmock.definitions.matcher.RequestMatcher;

/**
 * Defines the conditions for {@link CapturedRequest} matching.
 *
 * @author Fadelis
 */
public class RequestPattern<ReqT> {

  private final MethodDescriptor<ReqT, ?> method;
  private final HeadersMatcher headersMatcher;
  private final RequestMatcher<List<ReqT>> requestMatchers;

  RequestPattern(
      @Nonnull MethodDescriptor<ReqT, ?> method,
      @Nonnull HeadersMatcher headersMatcher,
      @Nullable RequestMatcher<List<ReqT>> requestMatchers
  ) {
    Objects.requireNonNull(method);
    Objects.requireNonNull(headersMatcher);
    this.method = method;
    this.headersMatcher = headersMatcher;
    this.requestMatchers = ofNullable(requestMatchers).orElseGet(RequestMatcher::empty);
  }

  public String fullMethodName() {
    return this.method.getFullMethodName();
  }

  public boolean matches(CapturedRequest<ReqT> capturedRequest) {
    return capturedRequest.method().getFullMethodName().equals(method.getFullMethodName())
        && headersMatcher.matches(capturedRequest.headers())
        && requestMatchers.matches(normalizeRequests(capturedRequest.requests()));
  }

  private List<ReqT> normalizeRequests(List<ReqT> requests) {
    return requests.stream()
        .map(this::normalizeRequest)
        .collect(Collectors.toList());
  }

  private ReqT normalizeRequest(ReqT request) {
    if (request instanceof byte[]) {
      // when no stub is registered for a method, server cannot unmarshall the request
      // as it has no knowledge of the schema, so it is stored as byte array
      // and unmarshalled here when verifying
      return method.getRequestMarshaller().parse(new ByteArrayInputStream((byte[]) request));
    }
    return request;
  }
}
