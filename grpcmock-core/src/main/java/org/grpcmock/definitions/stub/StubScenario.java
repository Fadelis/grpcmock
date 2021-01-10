package org.grpcmock.definitions.stub;

import static java.util.Optional.ofNullable;

import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.grpcmock.definitions.matcher.HeadersMatcher;
import org.grpcmock.definitions.matcher.RequestMatcher;
import org.grpcmock.definitions.response.Response;
import org.grpcmock.exception.GrpcMockValidationException;

/**
 * @author Fadelis
 */
public class StubScenario<ReqT, RespT> implements HeadersMatcher, RequestMatcher<ReqT> {

  private final HeadersMatcher headersMatcher;
  private final RequestMatcher<ReqT> requestMatcher;
  private final List<Response<ReqT, RespT>> responses;

  public StubScenario(
      @Nonnull HeadersMatcher headersMatcher,
      @Nullable RequestMatcher<ReqT> requestMatcher,
      @Nonnull List<Response<ReqT, RespT>> responses
  ) {
    Objects.requireNonNull(responses);
    Objects.requireNonNull(headersMatcher);
    if (responses.isEmpty()) {
      throw new GrpcMockValidationException("Stub scenario should contain at least one response");
    }
    this.headersMatcher = headersMatcher;
    this.requestMatcher = ofNullable(requestMatcher).orElseGet(RequestMatcher::empty);
    this.responses = new ArrayList<>(responses);
  }

  @Override
  public boolean matches(Metadata headers) {
    return headersMatcher.matches(headers);
  }

  @Override
  public boolean matches(ReqT request) {
    return requestMatcher.matches(request);
  }

  public void call(ReqT request, StreamObserver<RespT> streamObserver) {
    responses.stream()
        .filter(response -> !response.wasCalled())
        .findFirst()
        .orElseGet(() -> responses.get(responses.size() - 1))
        .execute(request, streamObserver);
  }
}
