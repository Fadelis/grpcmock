package org.grpcmock.definitions.response;

import io.grpc.stub.StreamObserver;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SingleActionResponse<ReqT, RespT> extends AbstractResponse<ReqT, RespT> {

  private final ResponseAction<RespT> responseAction;

  public SingleActionResponse(@Nonnull ResponseAction<RespT> responseAction) {
    Objects.requireNonNull(responseAction);
    this.responseAction = responseAction;
  }

  @Override
  public void execute(ReqT request, StreamObserver<RespT> streamObserver) {
    super.execute(request, streamObserver);
    responseAction.execute(streamObserver);
  }
}
