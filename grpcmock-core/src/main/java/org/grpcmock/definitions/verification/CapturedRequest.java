package org.grpcmock.definitions.verification;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * @author Fadelis
 */
public class CapturedRequest<ReqT> {

  private final MethodDescriptor<ReqT, ?> method;
  private final Metadata headers;
  private final List<ReqT> requests;

  public CapturedRequest(
      @Nonnull MethodDescriptor<ReqT, ?> method,
      @Nonnull Metadata headers,
      @Nonnull List<ReqT> requests
  ) {
    Objects.requireNonNull(method);
    Objects.requireNonNull(headers);
    Objects.requireNonNull(requests);
    this.method = method;
    this.headers = new Metadata();
    this.headers.merge(headers);
    this.requests = new ArrayList<>(requests);
  }

  public CapturedRequest(
      @Nonnull MethodDescriptor<ReqT, ?> method,
      @Nonnull Metadata headers,
      @Nonnull ReqT request
  ) {
    this(method, headers, Collections.singletonList(request));
  }

  public MethodDescriptor<ReqT, ?> method() {
    return method;
  }

  public Metadata headers() {
    return headers;
  }

  public List<ReqT> requests() {
    return requests;
  }

  @Override
  public String toString() {
    return String.format("Request to %s method%nwith headers:%n%s%nwith request:%n%s",
        method.getFullMethodName(),
        headers.toString(),
        requests.size() == 1 ? requests.get(0) : requests
    );
  }
}
