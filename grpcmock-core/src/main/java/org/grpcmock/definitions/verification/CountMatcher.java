package org.grpcmock.definitions.verification;

import java.util.Objects;
import java.util.function.IntPredicate;
import javax.annotation.Nonnull;

/**
 * @author Fadelis
 */
public class CountMatcher implements IntPredicate {

  private final String description;
  private final IntPredicate condition;

  public CountMatcher(@Nonnull String description, @Nonnull IntPredicate condition) {
    Objects.requireNonNull(description);
    Objects.requireNonNull(condition);
    this.description = description;
    this.condition = condition;
  }

  @Override
  public boolean test(int actual) {
    return condition.test(actual);
  }

  @Override
  public String toString() {
    return description;
  }

  /**
   * Called exactly the specified number of times.
   */
  public static CountMatcher times(int count) {
    return new CountMatcher(String.format("exactly %d times", count), actual -> actual == count);
  }

  /**
   * Called exactly one time.
   */
  public static CountMatcher once() {
    return times(1);
  }

  /**
   * Called exactly two times.
   */
  public static CountMatcher twice() {
    return times(2);
  }

  /**
   * Never called.
   */
  public static CountMatcher never() {
    return new CountMatcher("never", actual -> actual <= 0);
  }

  /**
   * Called the specified number of times or more.
   */
  public static CountMatcher atLeast(int count) {
    return new CountMatcher(String.format("at least %d times", count), actual -> actual >= count);
  }

  /**
   * Called the specified number of times or less.
   */
  public static CountMatcher atMost(int count) {
    return new CountMatcher(String.format("at most %d times", count), actual -> actual <= count);
  }
}
