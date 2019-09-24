package org.grpcmock.definitions.response.steps;

import javax.annotation.Nonnull;
import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.Delay;

public interface DelayBuilderStep<BUILDER extends BuilderStep> extends BuilderStep {

  BUILDER withDelay(@Nonnull Delay delay);

  /**
   * Configures a fixed {@link Delay} in milliseconds for the response action.
   */
  default BUILDER withFixedDelay(long milliseconds) {
    return withDelay(Delay.fixedDelay(milliseconds));
  }

  /**
   * Configures a random {@link Delay} between given {@param minMilliseconds} and {@param
   * maxMilliseconds} bounds in milliseconds for the response action.
   */
  default BUILDER withRandomDelay(long minMilliseconds, long maxMilliseconds) {
    return withDelay(Delay.randomDelay(minMilliseconds, maxMilliseconds));
  }

  /**
   * Configures a random {@link Delay} between given 0 and {@param maxMilliseconds} in milliseconds
   * for the response action.
   */
  default BUILDER withRandomDelay(long maxMilliseconds) {
    return withDelay(Delay.randomDelay(maxMilliseconds));
  }
}
