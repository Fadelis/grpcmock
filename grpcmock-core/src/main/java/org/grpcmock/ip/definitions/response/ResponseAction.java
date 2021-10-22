package org.grpcmock.ip.definitions.response;

import io.grpc.stub.StreamObserver;
import javax.annotation.Nonnull;

/**
 * Interface defining a single action, which can interact with the {@link StreamObserver}.
 *
 * @author Fadelis
 */
public interface ResponseAction<RespT> {

  /**
   * <p>Execute a response action on the {@link StreamObserver}.
   * <p>It can be either sending a response via {@link StreamObserver#onNext}
   * or returning an error via {@link StreamObserver#onError}.
   */
  void execute(@Nonnull StreamObserver<RespT> responseObserver);

  /**
   * Determines if the {@link ResponseAction} is a terminating action. By default it is
   * <code>false</code> and will be true for exception actions, which terminate the gRPC call.
   */
  default boolean isTerminating() {
    return false;
  }
}
