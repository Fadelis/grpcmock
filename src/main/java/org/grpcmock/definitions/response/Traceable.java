package org.grpcmock.definitions.response;

public interface Traceable {

  int timesCalled();

  default boolean wasCalled() {
    return timesCalled() > 0;
  }
}
