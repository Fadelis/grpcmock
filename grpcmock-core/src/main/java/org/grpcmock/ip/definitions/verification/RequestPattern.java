package org.grpcmock.ip.definitions.verification;

import io.grpc.MethodDescriptor;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.grpcmock.ip.definitions.matcher.HeadersMatcher;
import org.grpcmock.ip.definitions.matcher.RequestMatcher;
import org.grpcmock.ip.definitions.matcher.StatusMatcher;
import org.grpcmock.ip.interceptors.CapturedRequest;

/**
 * Defines the conditions for {@link CapturedRequest} matching.
 *
 * @author Fadelis
 */
public class RequestPattern<ReqT> {

  private final MethodDescriptor<ReqT, ?> method;
  private final StatusMatcher statusMatcher;
  private final HeadersMatcher headersMatcher;
  private final RequestMatcher<ReqT> requestsMatcher;

  RequestPattern(
      @Nonnull MethodDescriptor<ReqT, ?> method,
      @Nonnull StatusMatcher statusMatcher,
      @Nonnull HeadersMatcher headersMatcher,
      @Nonnull RequestMatcher<ReqT> requestsMatcher
  ) {
    Objects.requireNonNull(method);
    Objects.requireNonNull(statusMatcher);
    Objects.requireNonNull(headersMatcher);
    Objects.requireNonNull(requestsMatcher);
    this.method = method;
    this.statusMatcher = statusMatcher;
    this.headersMatcher = headersMatcher;
    this.requestsMatcher = requestsMatcher;
  }

  public String fullMethodName() {
    return this.method.getFullMethodName();
  }

  public boolean matches(CapturedRequest<ReqT> capturedRequest) {
    return capturedRequest.method().getFullMethodName().equals(method.getFullMethodName())
        && statusMatcher.matches(capturedRequest.closeStatus())
        && headersMatcher.matches(capturedRequest.headers())
        && requestsMatcher.matches(normalizeRequests(capturedRequest.requests()));
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
