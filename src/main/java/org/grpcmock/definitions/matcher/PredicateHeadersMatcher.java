package org.grpcmock.definitions.matcher;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

public class PredicateHeadersMatcher implements HeadersMatcher {

  private final Map<String, Predicate<String>> headerPredicates;

  public PredicateHeadersMatcher(@Nonnull Map<String, Predicate<String>> headerPredicates) {
    Objects.requireNonNull(headerPredicates);
    this.headerPredicates = headerPredicates;
  }

  @Override
  public boolean matches(Map<String, String> headers) {
    return headerPredicates.entrySet().stream()
        .allMatch(entry -> entry.getValue().test(headers.get(entry.getKey())));
  }
}
