package org.grpcmock.definitions.response;

import io.grpc.stub.StreamObserver;
import javax.annotation.Nonnull;

public class ExceptionResponse<ReqT, RespT> extends AbstractResponse<ReqT, RespT> {

  private final Throwable exception;

  public ExceptionResponse(@Nonnull Throwable exception) {
    this.exception = exception;
  }

  @Override
  public void execute(ReqT request, StreamObserver<RespT> streamObserver) {
    super.execute(request, streamObserver);
    streamObserver.onError(exception);
  }
}
