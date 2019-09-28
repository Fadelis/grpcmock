package org.grpcmock;

import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.util.MutableHandlerRegistry;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Objects;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.grpcmock.exception.GrpcMockException;
import org.grpcmock.interceptors.HeadersInterceptor;

/**
 * @author Fadelis
 */
public class GrpcMockBuilder {

  private final MutableHandlerRegistry handlerRegistry = new MutableHandlerRegistry();
  private ServerBuilder serverBuilder;

  GrpcMockBuilder(@Nonnull ServerBuilder serverBuilder) {
    Objects.requireNonNull(serverBuilder);
    this.serverBuilder = serverBuilder
        .intercept(new HeadersInterceptor())
        .fallbackHandlerRegistry(handlerRegistry);
  }

  GrpcMockBuilder(int port) {
    this(ServerBuilder.forPort(port <= 0 ? findFreePort() : port));
  }

  public GrpcMockBuilder interceptor(@Nonnull ServerInterceptor interceptor) {
    Objects.requireNonNull(interceptor);
    serverBuilder = serverBuilder.intercept(interceptor);
    return this;
  }

  public GrpcMockBuilder executor(@Nullable Executor executor) {
    serverBuilder = serverBuilder.executor(executor);
    return this;
  }

  public GrpcMockBuilder transportSecurity(@Nonnull File certChain, @Nonnull File privateKey) {
    Objects.requireNonNull(certChain);
    Objects.requireNonNull(privateKey);
    serverBuilder = serverBuilder.useTransportSecurity(certChain, privateKey);
    return this;
  }

  private static int findFreePort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      socket.setReuseAddress(true);
      return socket.getLocalPort();
    } catch (IOException e) {
      throw new GrpcMockException("Failed finding a free port", e);
    }
  }

  public GrpcMock build() {
    return new GrpcMock(serverBuilder.build(), handlerRegistry);
  }
}
