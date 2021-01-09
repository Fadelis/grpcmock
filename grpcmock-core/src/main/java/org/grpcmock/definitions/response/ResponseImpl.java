package org.grpcmock.definitions.response;

import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import org.grpcmock.exception.GrpcMockValidationException;

/**
 * @author Fadelis
 */
public class ResponseImpl<ReqT, RespT> implements Response<ReqT, RespT> {

  private final AtomicInteger counter = new AtomicInteger();
  private final List<ResponseAction<RespT>> responseActions;

  public ResponseImpl(@Nonnull List<ResponseAction<RespT>> responseActions) {
    Objects.requireNonNull(responseActions);
    if (responseActions.isEmpty()) {
      throw new GrpcMockValidationException("Should contain at least one action");
    }
    if (responseActions.stream().anyMatch(Objects::isNull)) {
      throw new GrpcMockValidationException("Response action cannot be null");
    }
    if (responseActions.stream().filter(ResponseAction::isTerminating).count() > 1) {
      throw new GrpcMockValidationException("Should not contain more that one terminating action");
    }
    if (responseActions.stream()
        .limit(responseActions.size() - 1L)
        .anyMatch(ResponseAction::isTerminating)) {
      throw new GrpcMockValidationException("Terminating action should be the last one");
    }
    this.responseActions = new ArrayList<>(responseActions);
  }

  public ResponseImpl(@Nonnull ResponseAction<RespT> responseAction) {
    this(Collections.singletonList(responseAction));
  }

  @Override
  public void execute(ReqT request, StreamObserver<RespT> responseObserver) {
    counter.incrementAndGet();
    responseActions.forEach(action -> action.execute(responseObserver));
    if (!responseActions.get(responseActions.size() - 1).isTerminating()) {
      responseObserver.onCompleted();
    }
  }

  @Override
  public int timesCalled() {
    return counter.get();
  }
}
