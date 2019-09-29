package org.grpcmock.definitions.response;

/**
 * @author Fadelis
 */
public interface TerminatingResponseAction<RespT> extends ResponseAction<RespT> {

  @Override
  default boolean isTerminating() {
    return true;
  }
}
