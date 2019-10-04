package org.grpcmock.definitions.response;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Functional interface for a delay in milliseconds used for {@link ResponseAction}.
 *
 * @author Fadelis
 */
public interface Delay {

  /**
   * Defines delay value in milliseconds.
   */
  long inMilliseconds();

  /**
   * Fixed {@link Delay} in milliseconds.
   */
  static Delay fixedDelay(long milliseconds) {
    return () -> milliseconds;
  }

  /**
   * Random delay between given minMilliseconds and maxMilliseconds bounds.
   */
  static Delay randomDelay(long minMilliseconds, long maxMilliseconds) {
    return () -> ThreadLocalRandom.current().nextLong(minMilliseconds, maxMilliseconds);
  }

  /**
   * Random delay between 0 and maxMilliseconds.
   */
  static Delay randomDelay(long maxMilliseconds) {
    return randomDelay(0, maxMilliseconds);
  }

  /**
   * Trigger configured delay for the response action.
   */
  default void delayAction() {
    try {
      TimeUnit.MILLISECONDS.sleep(inMilliseconds());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
