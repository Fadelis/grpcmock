package org.grpcmock.definitions.matcher;

import java.util.Map;

/**
 * Header matcher interface. Currently only {@link String} type headers are supported.
 *
 * @author Fadelis
 */
public interface HeadersMatcher {

  boolean matches(Map<String, String> headers);

  static HeadersMatcher empty() {
    return header -> true;
  }
}
