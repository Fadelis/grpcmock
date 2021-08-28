package org.grpcmock.definitions.matcher.steps;

import io.grpc.Status;
import io.grpc.Status.Code;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.grpcmock.definitions.BuilderStep;

/**
 * @author Fadelis
 */
public interface StatusMatcherBuilderStep<BUILDER extends StatusMatcherBuilderStep<BUILDER>> extends
    BuilderStep {

  /**
   * <p>Adds a close {@link Status} matcher, which will check if the close status satisfies given predicate.
   * <p>The status inside the predicate might be <code>null</code> if the request has not finished during the match invocation.
   * <p>Subsequent status matchers will be added as additional conditions.
   */
  BUILDER withStatus(@Nonnull Predicate<Status> predicate);

  /**
   * <p>Adds a close {@link Status} matcher, which will check if the close status has the given {@link Code}.
   * <p>Subsequent status matchers will be added as additional conditions.
   */
  default BUILDER withStatusCode(@Nonnull Code code) {
    return withStatus(status -> Objects.nonNull(status) && status.getCode() == code);
  }

  /**
   * <p>Adds a close {@link Status} matcher, which will check if the close status closed with {@link Code#OK}.
   * <p>Subsequent status matchers will be added as additional conditions.
   */
  default BUILDER withStatusOk() {
    return withStatusCode(Code.OK);
  }

  /**
   * <p>Adds a close {@link Status} matcher, which will check if the close status description is equal to given one.
   * <p>Subsequent status matchers will be added as additional conditions.
   */
  default BUILDER withStatusMessage(@Nullable String message) {
    return withStatus(status -> Objects.nonNull(status) && Objects.equals(message, status.getDescription()));
  }
}
