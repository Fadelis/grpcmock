package org.grpcmock.definitions.matcher;

import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.matcher.steps.HeadersMatcherBuilder;

/**
 * @author Fadelis
 */
public class HeadersMatcherBuilderImpl implements HeadersMatcherBuilder {

  private final List<Predicate<Metadata>> headerPredicates = new ArrayList<>();

  HeadersMatcherBuilderImpl() {
  }

  @Override
  public <T> HeadersMatcherBuilder withHeader(
      @Nonnull Key<T> headerKey,
      @Nonnull Predicate<T> predicate
  ) {
    Objects.requireNonNull(headerKey);
    Objects.requireNonNull(predicate);
    this.headerPredicates.add(metadata -> predicate.test(metadata.get(headerKey)));
    return this;
  }

  @Override
  public HeadersMatcher build() {
    return headers -> headerPredicates.stream().allMatch(predicate -> predicate.test(headers));
  }
}
