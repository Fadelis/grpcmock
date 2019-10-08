package org.grpcmock.exception;

/**
 * Exception type thrown when verifying request call count condition.
 *
 * @author Fadelis
 */
public class GrpcMockVerificationError extends AssertionError {

  public GrpcMockVerificationError(String message) {
    super(message);
  }

  public GrpcMockVerificationError(String message, Throwable cause) {
    super(message, cause);
  }
}
