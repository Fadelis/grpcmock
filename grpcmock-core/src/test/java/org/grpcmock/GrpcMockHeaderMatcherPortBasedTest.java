package org.grpcmock;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.Metadata;
import io.grpc.Metadata.AsciiMarshaller;
import io.grpc.testing.protobuf.SimpleRequest;
import org.grpcmock.definitions.matcher.HeadersMatcher;
import org.junit.jupiter.api.Test;

/**
 * @author Fadelis
 */
class GrpcMockHeaderMatcherPortBasedTest extends PortBasedTestBase {

  private static final AsciiMarshaller<String> MARSHALLER = Metadata.ASCII_STRING_MARSHALLER;
  private static final Metadata.Key<String> STRING_KEY = Metadata.Key.of("key-1", MARSHALLER);
  private static final Metadata.Key<byte[]> BINARY_KEY = Metadata.Key
      .of("key-2" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

  private final SimpleRequest object = SimpleRequest.newBuilder()
      .setRequestMessage("message-1")
      .build();


  @Test
  void should_match_exact_string_type_header() {
    Metadata headers = new Metadata();
    headers.put(STRING_KEY, "value-1");

    HeadersMatcher matcher = HeadersMatcher.builder()
        .withHeader(STRING_KEY, "value-1")
        .build();

    assertThat(matcher.matches(headers)).isTrue();
  }

  @Test
  void should_match_exact_string_type_header_with_string_key() {
    Metadata headers = new Metadata();
    headers.put(STRING_KEY, "value-1");

    HeadersMatcher matcher = HeadersMatcher.builder()
        .withHeader(STRING_KEY.name(), "value-1")
        .build();

    assertThat(matcher.matches(headers)).isTrue();
  }

  @Test
  void should_match_predicate_for_string_type_header() {
    Metadata headers = new Metadata();
    headers.put(STRING_KEY, "longer_value_1");

    HeadersMatcher matcher = HeadersMatcher.builder()
        .withHeader(STRING_KEY, value -> value.startsWith("longer"))
        .build();

    assertThat(matcher.matches(headers)).isTrue();
  }

  @Test
  void should_match_predicate_for_string_type_header_with_string_key() {
    Metadata headers = new Metadata();
    headers.put(STRING_KEY, "longer_value_1");

    HeadersMatcher matcher = HeadersMatcher.builder()
        .withHeader(STRING_KEY.name(), value -> value.startsWith("longer"))
        .build();

    assertThat(matcher.matches(headers)).isTrue();
  }

  @Test
  void should_match_if_header_does_not_exist() {
    Metadata headers = new Metadata();

    HeadersMatcher matcher = HeadersMatcher.builder()
        .withoutHeader(STRING_KEY)
        .build();

    assertThat(matcher.matches(headers)).isTrue();
  }

  @Test
  void should_match_if_header_does_not_exist_with_string_key() {
    Metadata headers = new Metadata();

    HeadersMatcher matcher = HeadersMatcher.builder()
        .withoutHeader(STRING_KEY.name())
        .build();

    assertThat(matcher.matches(headers)).isTrue();
  }

  @Test
  void should_match_binary_header() {
    byte[] objectBytes = object.toByteArray();
    Metadata headers = new Metadata();
    headers.put(BINARY_KEY, objectBytes);

    HeadersMatcher matcher = HeadersMatcher.builder()
        .withHeader(BINARY_KEY, objectBytes)
        .build();

    assertThat(matcher.matches(headers)).isTrue();
  }

  @Test
  void should_match_predicate_for_binary_header() {
    byte[] objectBytes = object.toByteArray();
    Metadata headers = new Metadata();
    headers.put(BINARY_KEY, objectBytes);

    HeadersMatcher matcher = HeadersMatcher.builder()
        .withHeader(BINARY_KEY, value -> value.length == objectBytes.length)
        .build();

    assertThat(matcher.matches(headers)).isTrue();
  }
}
