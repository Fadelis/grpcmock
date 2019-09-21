package org.grpcmock.definitions;

public interface Traceable {

  int timesCalled();

  default boolean wasCalled() {
    return timesCalled() > 0;
  }
}
