package org.grpcmock.definitions.matcher;

import java.util.Map;

public interface HeadersMatcher {

  boolean matches(Map<String, String> headers);

  static HeadersMatcher empty() {
    return header -> true;
  }
}
