package org.grpcmock.definitions.matcher;

/**
 * @author Fadelis
 */
public interface RequestMatcher<ReqT> {

  boolean matches(ReqT request);

  static <ReqT> RequestMatcher<ReqT> empty() {
    return request -> true;
  }
}
