package org.grpcmock.definitions.response.steps;

import java.time.Duration;
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
   * Configures a fixed {@link Delay} with provided {@link Duration} for the response action.
   * <p>Delay for {@link ResponseAction} is additive, meaning that it will trigger after previous
   * {@link ResponseAction} has finished.
   */
  default BUILDER withFixedDelay(Duration duration) {
    return withDelay(Delay.fixedDelay(duration.toMillis()));
  }

  /**
   * Configures a random {@link Delay} between given minMilliseconds and maxMilliseconds bounds in milliseconds for the response
   * action.
   * <p>Delay for {@link ResponseAction} is additive, meaning that it will trigger after previous
   * {@link ResponseAction} has finished.
   */
  default BUILDER withRandomDelay(long minMilliseconds, long maxMilliseconds) {
    return withDelay(Delay.randomDelay(minMilliseconds, maxMilliseconds));
  }

  /**
   * Configures a random {@link Delay} between given min {@link Duration} and max {@link Duration} bounds in milliseconds for the
   * response action.
   * <p>Delay for {@link ResponseAction} is additive, meaning that it will trigger after previous
   * {@link ResponseAction} has finished.
   */
  default BUILDER withRandomDelay(Duration minDuration, Duration maxDuration) {
    return withDelay(Delay.randomDelay(minDuration.toMillis(), maxDuration.toMillis()));
  }

  /**
   * Configures a random {@link Delay} between 0 and given maxMilliseconds for the response action.
   * <p>Delay for {@link ResponseAction} is additive, meaning that it will trigger after previous
   * {@link ResponseAction} has finished.
   */
  default BUILDER withRandomDelay(long maxMilliseconds) {
    return withRandomDelay(0, maxMilliseconds);
  }

  /**
   * Configures a random {@link Delay} between 0 and given max {@link Duration} for the response action.
   * <p>Delay for {@link ResponseAction} is additive, meaning that it will trigger after previous
   * {@link ResponseAction} has finished.
   */
  default BUILDER withRandomDelay(Duration maxDuration) {
    return withRandomDelay(Duration.ZERO, maxDuration);
  }
}
