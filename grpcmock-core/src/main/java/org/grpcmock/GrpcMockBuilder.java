package org.grpcmock;

import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.util.MutableHandlerRegistry;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

  public GrpcMock build() {
    return new GrpcMock(serverBuilder.build(), handlerRegistry);
  }
}
