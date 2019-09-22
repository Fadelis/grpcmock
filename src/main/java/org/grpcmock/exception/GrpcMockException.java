package org.grpcmock.exception;

/**
 * General exception type for gRPC Mock for when things go unexpected.
 */
public class GrpcMockException extends RuntimeException {

  public GrpcMockException(String message) {
    super(message);
  }

  public GrpcMockException(String message, Throwable cause) {
    super(message, cause);
  }
}
