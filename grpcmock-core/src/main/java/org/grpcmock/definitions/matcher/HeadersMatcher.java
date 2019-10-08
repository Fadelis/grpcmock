package org.grpcmock.definitions.matcher;

import io.grpc.Metadata;
import org.grpcmock.definitions.matcher.steps.HeadersMatcherBuilder;

/**
 * Header matcher interface. Currently only {@link String} type headers are supported.
 *
 * @author Fadelis
 */
public interface HeadersMatcher {

  boolean matches(Metadata headers);

  static HeadersMatcherBuilder builder() {
    return new HeadersMatcherBuilderImpl();
  }
}
