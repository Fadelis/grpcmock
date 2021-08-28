package org.grpcmock.definitions.matcher;

import java.util.List;

/**
 * @author Fadelis
 */
public interface RequestMatcher<ReqT> {

  boolean matches(List<ReqT> requests);

  static <ReqT> RequestMatcherBuilderImpl<ReqT> builder() {
    return new RequestMatcherBuilderImpl<>();
  }
}
