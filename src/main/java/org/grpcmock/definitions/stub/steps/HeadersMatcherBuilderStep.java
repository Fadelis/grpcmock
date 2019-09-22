package org.grpcmock.definitions.stub.steps;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

public interface HeadersMatcherBuilderStep<BUILDER extends BuilderStep> extends BuilderStep {

  BUILDER withHeader(@Nonnull String headerName,
      @Nonnull Predicate<String> valuePredicate);

  default BUILDER withHeader(@Nonnull String header, @Nonnull String value) {
    Objects.requireNonNull(value);
    return withHeader(header, value::equals);
  }

  default BUILDER withoutHeader(@Nonnull String header) {
    return withHeader(header, Objects::isNull);
  }
}
