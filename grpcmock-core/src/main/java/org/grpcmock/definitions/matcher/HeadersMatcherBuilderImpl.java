package org.grpcmock.definitions.matcher;

import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.matcher.steps.HeadersMatcherBuilderStep;

/**
 * @author Fadelis
 */
public class HeadersMatcherBuilderImpl implements HeadersMatcherBuilderStep<HeadersMatcherBuilderImpl> {

  private final List<Predicate<Metadata>> headerPredicates = new ArrayList<>();

  HeadersMatcherBuilderImpl() {
  }

  @Override
  public <T> HeadersMatcherBuilderImpl withHeader(
      @Nonnull Key<T> headerKey,
      @Nonnull Predicate<T> predicate
  ) {
    Objects.requireNonNull(headerKey);
    Objects.requireNonNull(predicate);
    this.headerPredicates.add(metadata -> predicate.test(metadata.get(headerKey)));
    return this;
  }

  public HeadersMatcher build() {
    return headers -> headerPredicates.stream().allMatch(predicate -> predicate.test(headers));
  }
}
