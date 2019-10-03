package org.grpcmock.exception;

/**
 * Exception type thrown when verifying request call count condition.
 *
 * @author Fadelis
 */
public class GrpcMockVerificationException extends GrpcMockException {

  public GrpcMockVerificationException(String message) {
    super(message);
  }

  public GrpcMockVerificationException(String message, Throwable cause) {
    super(message, cause);
  }
}
