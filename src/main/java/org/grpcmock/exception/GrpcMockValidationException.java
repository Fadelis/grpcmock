package org.grpcmock.exception;

/**
 * Exception type thrown on failed validation when configuring gRPC Mock or creating stubs.
 *
 * @author Fadelis
 */
public class GrpcMockValidationException extends GrpcMockException {

  public GrpcMockValidationException(String message) {
    super(message);
  }

  public GrpcMockValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
