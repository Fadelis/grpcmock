package org.grpcmock.definitions.response;

import io.grpc.stub.StreamObserver;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.grpcmock.exception.UnimplementedStatusException;

public class ResponseActionImpl<RespT> implements ResponseAction<RespT> {

  private final RespT responseObject;
  private final Throwable exception;
  private final Delay delay;

  ResponseActionImpl(@Nonnull RespT responseObject, @Nullable Delay delay) {
    Objects.requireNonNull(responseObject);
    this.responseObject = responseObject;
    this.delay = delay;
    this.exception = null;
  }

  ResponseActionImpl(@Nonnull Throwable exception, @Nullable Delay delay) {
    Objects.requireNonNull(exception);
    this.exception = exception;
    this.delay = delay;
    this.responseObject = null;
  }

  @Override
  public void execute(@Nonnull StreamObserver<RespT> streamObserver) {
    Optional.ofNullable(delay).ifPresent(Delay::delayAction);
    if (Objects.nonNull(responseObject)) {
      streamObserver.onNext(responseObject);
      streamObserver.onCompleted();
    } else if (Objects.nonNull(exception)) {
      streamObserver.onError(exception);
    } else {
      streamObserver.onError(new UnimplementedStatusException("no response action was provided"));
    }
  }
}
