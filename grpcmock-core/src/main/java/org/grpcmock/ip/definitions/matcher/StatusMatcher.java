package org.grpcmock.ip.definitions.matcher;

import io.grpc.Status;

/**
 * @author Fadelis
 */
public interface StatusMatcher {

  boolean matches(Status status);

  static StatusMatcherBuilderImpl builder() {
    return new StatusMatcherBuilderImpl();
  }
}
