package org.grpcmock.definitions.response.steps;

import javax.annotation.Nonnull;
import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.Delay;
import org.grpcmock.definitions.response.ResponseAction;

/**
 * @author Fadelis
 */
public interface DelayBuilderStep<BUILDER extends BuilderStep> extends BuilderStep {

  /**
   * <p>Configure a {@link Delay} for the response action.
   * <p>Delay for {@link ResponseAction} is additive, meaning that it will trigger after previous
   * {@link ResponseAction} has finished.
   */
  BUILDER withDelay(@Nonnull Delay delay);

  /**
   * Configures a fixed {@link Delay} in milliseconds for the response action.
   * <p>Delay for {@link ResponseAction} is additive, meaning that it will trigger after previous
   * {@link ResponseAction} has finished.
   */
  default BUILDER withFixedDelay(long milliseconds) {
    return withDelay(Delay.fixedDelay(milliseconds));
  }

  /**
   * Configures a random {@link Delay} between given minMilliseconds and maxMilliseconds bounds in
   * milliseconds for the response action.
   * <p>Delay for {@link ResponseAction} is additive, meaning that it will trigger after previous
   * {@link ResponseAction} has finished.
   */
  default BUILDER withRandomDelay(long minMilliseconds, long maxMilliseconds) {
    return withDelay(Delay.randomDelay(minMilliseconds, maxMilliseconds));
  }

  /**
   * Configures a random {@link Delay} between given 0 and maxMilliseconds in milliseconds for the
   * response action.
   * <p>Delay for {@link ResponseAction} is additive, meaning that it will trigger after previous
   * {@link ResponseAction} has finished.
   */
  default BUILDER withRandomDelay(long maxMilliseconds) {
    return withDelay(Delay.randomDelay(maxMilliseconds));
  }
}
