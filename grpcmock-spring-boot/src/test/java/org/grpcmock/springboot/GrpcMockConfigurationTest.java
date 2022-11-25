package org.grpcmock.springboot;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.assertj.core.api.Assertions;
import org.grpcmock.exception.GrpcMockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * @author Fadelis
 */
class GrpcMockConfigurationTest {

  private final GrpcMockProperties properties = mock(
      GrpcMockProperties.class, Mockito.RETURNS_DEEP_STUBS);
  private final ApplicationContext context = mock(ApplicationContext.class);
  // Use a real implementation of DefaultListableBeanFactory until issues with Mockito and JDK17 are sorted
  // https://github.com/mockito/mockito/issues/2589
  private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
  private final GrpcMockConfiguration configuration = new GrpcMockConfiguration(
      properties, context, beanFactory);

  @BeforeEach
  void setup() {
    when(properties.getServer().getPort()).thenReturn(8888);
  }

  @Test
  void should_throw_error_when_interceptor_does_not_have_no_args_constructor() {
    when(properties.getServer().getInterceptors())
        .thenReturn(new Class[]{MyServerInterceptor.class});

    Assertions.assertThatThrownBy(configuration::init)
        .isInstanceOf(GrpcMockException.class)
        .hasMessageContaining("missing no-args constructor");
  }

  @Test
  void should_throw_error_when_cert_chain_is_present_but_private_key_is_missing() {
    when(properties.getServer().getCertChainFile()).thenReturn("my-cert-chain");

    Assertions.assertThatThrownBy(configuration::init)
        .isInstanceOf(GrpcMockException.class)
        .hasMessage("Both certChain and privateKey have to be defined");
  }

  @Test
  void should_throw_error_when_private_key_is_present_but_cert_chain_is_missing() {
    when(properties.getServer().getPrivateKeyFile()).thenReturn("my-cert-chain");

    Assertions.assertThatThrownBy(configuration::init)
        .isInstanceOf(GrpcMockException.class)
        .hasMessage("Both certChain and privateKey have to be defined");
  }

  public static class MyServerInterceptor implements ServerInterceptor {

    public MyServerInterceptor(String arg) {
    }

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call,
        Metadata headers,
        ServerCallHandler<ReqT, RespT> next
    ) {
      return next.startCall(call, headers);
    }
  }
}