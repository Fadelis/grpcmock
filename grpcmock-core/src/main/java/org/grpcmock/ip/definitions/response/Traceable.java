package org.grpcmock.ip.definitions.response;

import org.grpcmock.ip.definitions.stub.StubScenario;

/**
 * Interface used in {@link Response} and {@link StubScenario} to track the number of invocations.
 *
 * @author Fadelis
 */
public interface Traceable {

  int timesCalled();

  default boolean wasCalled() {
    return timesCalled() > 0;
  }
}
