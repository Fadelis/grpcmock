package org.grpcmock.springboot;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.assertj.core.api.Assertions;
import org.grpcmock.exception.GrpcMockException;
import org.grpcmock.springboot.GrpcMockProperties.Server;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * @author Fadelis
 */
class GrpcMockConfigurationTest {

  private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
  private GrpcMockConfiguration configuration;

  @Test
  void should_throw_error_when_interceptor_does_not_have_no_args_constructor() {
    GrpcMockProperties properties = new GrpcMockProperties();
    Server server = new Server();
    server.setPort(8888);
    server.setInterceptors(new Class[]{MyServerInterceptor.class});
    properties.setServer(server);
    configuration = new GrpcMockConfiguration(properties, beanFactory);

    Assertions.assertThatThrownBy(configuration::afterPropertiesSet)
        .isInstanceOf(GrpcMockException.class)
        .hasMessageContaining("missing no-args constructor");
  }

  @Test
  void should_throw_error_when_cert_chain_is_present_but_private_key_is_missing() {
    GrpcMockProperties properties = new GrpcMockProperties();
    Server server = new Server();
    server.setPort(8888);
    server.setCertChainFile("my-cert-chain");
    properties.setServer(server);
    configuration = new GrpcMockConfiguration(properties, beanFactory);

    Assertions.assertThatThrownBy(configuration::afterPropertiesSet)
        .isInstanceOf(GrpcMockException.class)
        .hasMessage("Both certChain and privateKey have to be defined");
  }

  @Test
  void should_throw_error_when_private_key_is_present_but_cert_chain_is_missing() {
    GrpcMockProperties properties = new GrpcMockProperties();
    Server server = new Server();
    server.setPort(8888);
    server.setPrivateKeyFile("my-cert-chain");
    properties.setServer(server);
    configuration = new GrpcMockConfiguration(properties, beanFactory);

    Assertions.assertThatThrownBy(configuration::afterPropertiesSet)
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
