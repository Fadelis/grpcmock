package org.grpcmock.definitions.matcher;

import java.util.List;

/**
 * @author Fadelis
 */
public interface RequestMatcher<ReqT> {

  boolean matches(List<ReqT> requests);

  static <ReqT> RequestMatcher<ReqT> empty() {
    return requests -> true;
  }
}
