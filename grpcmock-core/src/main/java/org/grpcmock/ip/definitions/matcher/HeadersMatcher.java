package org.grpcmock.ip.definitions.matcher;

import io.grpc.Metadata;

/**
 * Header matcher interface.
 *
 * @author Fadelis
 */
public interface HeadersMatcher {

  boolean matches(Metadata headers);

  static HeadersMatcherBuilderImpl builder() {
    return new HeadersMatcherBuilderImpl();
  }
}
