package org.grpcmock.definitions.response;

import io.grpc.stub.StreamObserver;
import javax.annotation.Nonnull;

/**
 * Interface defining a single action, which can interact with the {@link StreamObserver}.
 */
public interface ResponseAction<RespT> {

  /**
   * <p>Execute a response action on the {@link StreamObserver}.
   * <p>It can be either sending a response via {@link StreamObserver#onNext}
   * or returning an error via {@link StreamObserver#onError}.
   */
  void execute(@Nonnull StreamObserver<RespT> streamObserver);
}
