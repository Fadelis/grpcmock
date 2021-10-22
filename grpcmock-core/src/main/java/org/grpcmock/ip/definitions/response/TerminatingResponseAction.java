package org.grpcmock.ip.definitions.response;

/**
 * @author Fadelis
 */
public interface TerminatingResponseAction<RespT> extends ResponseAction<RespT> {

  @Override
  default boolean isTerminating() {
    return true;
  }
}
