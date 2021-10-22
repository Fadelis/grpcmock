package org.grpcmock.ip.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import javax.annotation.Nonnull;

/**
 * Exception wrapper for {@link StatusRuntimeException} with {@link Status#UNIMPLEMENTED}.
 *
 * @author Fadelis
 */
public class UnimplementedStatusException extends StatusRuntimeException {

  /**
   * Create a {@link StatusRuntimeException} with {@link Status#UNIMPLEMENTED} status and given
   * message.
   */
  public UnimplementedStatusException(@Nonnull String message) {
    super(Status.UNIMPLEMENTED.withDescription(message));
  }
}
