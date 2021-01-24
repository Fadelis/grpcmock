package org.grpcmock.definitions.stub;

import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.response.Response;
import org.grpcmock.definitions.verification.CapturedRequest;
import org.grpcmock.definitions.verification.RequestPattern;
import org.grpcmock.exception.GrpcMockValidationException;

/**
 * @author Fadelis
 */
public final class StubScenario<ReqT, RespT> {

  private final RequestPattern<ReqT> requestPattern;
  private final List<Response<ReqT, RespT>> responses;

  public StubScenario(
      @Nonnull RequestPattern<ReqT> requestPattern,
      @Nonnull List<Response<ReqT, RespT>> responses
  ) {
    Objects.requireNonNull(responses);
    Objects.requireNonNull(requestPattern);
    if (responses.isEmpty()) {
      throw new GrpcMockValidationException("Stub scenario should contain at least one response");
    }
    this.requestPattern = requestPattern;
    this.responses = new ArrayList<>(responses);
  }

  public boolean matches(CapturedRequest<ReqT> capturedRequest) {
    return requestPattern.matches(capturedRequest);
  }

  public void call(ReqT request, StreamObserver<RespT> streamObserver) {
    nextResponse().execute(request, streamObserver);
  }

  public StreamObserver<ReqT> call(StreamObserver<RespT> streamObserver) {
    return nextResponse().execute(streamObserver);
  }

  private Response<ReqT, RespT> nextResponse() {
    return responses.stream()
        .filter(response -> !response.wasCalled())
        .findFirst()
        .orElseGet(() -> responses.get(responses.size() - 1));
  }
}
