package org.grpcmock.definitions.stub.steps;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.BuilderStep;

/**
 * @author Fadelis
 */
public interface HeadersMatcherBuilderStep<BUILDER extends BuilderStep> extends BuilderStep {

  /**
   * <p>Adds a header matcher for the stub, which will trigger only if the given header
   * has value satisfying given {@link Predicate}.
   * <p>Subsequent matchers for the same header will replace the old one.
   */
  BUILDER withHeader(@Nonnull String headerName, @Nonnull Predicate<String> valuePredicate);

  /**
   * <p>Adds a header matcher for the stub, which will trigger only if the given header
   * has value equal to the provided one.
   * <p>Subsequent headers matchers for the same header will replace the old one.
   */
  default BUILDER withHeader(@Nonnull String header, @Nonnull String value) {
    Objects.requireNonNull(value);
    return withHeader(header, value::equals);
  }

  /**
   * <p>Adds a header matcher for the stub, which will trigger only if the given header
   * does not exist.
   * <p>Subsequent headers matchers for the same header will replace the old one.
   */
  default BUILDER withoutHeader(@Nonnull String header) {
    return withHeader(header, Objects::isNull);
  }
}
