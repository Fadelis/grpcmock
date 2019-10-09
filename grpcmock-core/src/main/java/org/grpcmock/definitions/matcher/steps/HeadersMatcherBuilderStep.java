package org.grpcmock.definitions.matcher.steps;

import static io.grpc.Metadata.Key.of;

import io.grpc.Metadata;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.BuilderStep;

/**
 * @author Fadelis
 */
public interface HeadersMatcherBuilderStep<BUILDER extends HeadersMatcherBuilderStep<BUILDER>> extends
    BuilderStep {

  /**
   * <p>Adds a header matcher for the stub, which will trigger only if the given header
   * has value satisfying given {@link Predicate}.
   * <p>Subsequent matchers for the same header will replace the old one.
   */
  <T> BUILDER withHeader(
      @Nonnull Metadata.Key<T> headerKey,
      @Nonnull Predicate<T> predicate);

  /**
   * <p>Adds a binary header matcher for the stub, which will trigger only if the given header
   * has binary value {@link Arrays#equals(byte[], byte[])} to the provided one.
   * <p>Subsequent headers matchers for the same header will replace the old one.
   */
  default BUILDER withHeader(@Nonnull Metadata.Key<byte[]> header, @Nonnull byte[] value) {
    Objects.requireNonNull(value);
    return withHeader(header, actual -> Arrays.equals(actual, value));
  }

  /**
   * <p>Adds a header matcher for the stub, which will trigger only if the given header
   * has value equal to the provided one.
   * <p>Subsequent headers matchers for the same header will replace the old one.
   */
  default BUILDER withHeader(@Nonnull Metadata.Key<String> header, @Nonnull String value) {
    Objects.requireNonNull(value);
    return withHeader(header, value::equals);
  }

  /**
   * <p>Adds a header matcher for the stub, which will trigger only if the given header
   * has value satisfying given {@link Predicate}.
   * <p>Subsequent matchers for the same header will replace the old one.
   */
  default BUILDER withHeader(
      @Nonnull String headerName,
      @Nonnull Predicate<String> predicate
  ) {
    Objects.requireNonNull(headerName);
    return withHeader(of(headerName, Metadata.ASCII_STRING_MARSHALLER), predicate);
  }

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
  default <T> BUILDER withoutHeader(@Nonnull Metadata.Key<T> headerKey) {
    return withHeader(headerKey, Objects::isNull);
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
