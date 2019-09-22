package org.grpcmock.definitions.response;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.Objects;
import javax.annotation.Nullable;

public class ObjectResponse<ReqT, RespT> extends AbstractResponse<ReqT, RespT> {

  private final RespT responseObject;

  public ObjectResponse(@Nullable RespT responseObject) {
    this.responseObject = responseObject;
  }

  @Override
  public void execute(ReqT request, StreamObserver<RespT> streamObserver) {
    super.execute(request, streamObserver);
    if (Objects.nonNull(responseObject)) {
      streamObserver.onNext(responseObject);
      streamObserver.onCompleted();
    } else {
      streamObserver.onError(Status.UNIMPLEMENTED
          .withDescription("No response for the stub was found")
          .asRuntimeException());
    }
  }
}
