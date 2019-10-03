package org.grpcmock.definitions.verification;

import io.grpc.MethodDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * @author Fadelis
 */
public class CapturedRequest<ReqT> {

  private final MethodDescriptor<ReqT, ?> method;
  private final Map<String, String> headers;
  private final List<ReqT> requests;

  public CapturedRequest(
      @Nonnull MethodDescriptor<ReqT, ?> method,
      @Nonnull Map<String, String> headers,
      @Nonnull List<ReqT> requests
  ) {
    Objects.requireNonNull(method);
    Objects.requireNonNull(headers);
    Objects.requireNonNull(requests);
    this.method = method;
    this.headers = new HashMap<>(headers);
    this.requests = new ArrayList<>(requests);
  }

  public CapturedRequest(
      @Nonnull MethodDescriptor<ReqT, ?> method,
      @Nonnull Map<String, String> headers,
      @Nonnull ReqT request
  ) {
    this(method, headers, Collections.singletonList(request));
  }

  public MethodDescriptor<ReqT, ?> method() {
    return method;
  }

  public Map<String, String> headers() {
    return headers;
  }

  public List<ReqT> requests() {
    return requests;
  }

  @Override
  public String toString() {
    return String.format("Request to %s method%nwith headers:%n%s%nwith request:%n%s",
        method.getFullMethodName(),
        headers.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(", ", "{", "}")),
        requests.size() == 1 ? requests.get(0) : requests
    );
  }
}
