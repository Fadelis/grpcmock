package org.grpcmock.ip.util;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Utility class for functional programming helper methods
 *
 * @author Fadelis
 */
public final class FunctionalHelpers {

  private FunctionalHelpers() {
  }

  /**
   * Returns a reversed order stream from the provided list.
   */
  public static <T> Stream<T> reverseStream(List<T> list) {
    return IntStream.iterate(list.size() - 1, i -> i - 1)
        .limit(list.size())
        .mapToObj(list::get);
  }
}
