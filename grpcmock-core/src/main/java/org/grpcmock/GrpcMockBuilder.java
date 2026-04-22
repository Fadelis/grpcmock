package org.grpcmock;

import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.grpcmock.interceptors.RequestCaptureInterceptor;

/**
 * @author Fadelis
 */
public class GrpcMockBuilder {

  private final DelegateHandlerRegistry delegateHandlerRegistry = new DelegateHandlerRegistry();
  private final RequestCaptureInterceptor requestCaptureInterceptor = new RequestCaptureInterceptor();
  private final ServerBuilder serverBuilder;

  GrpcMockBuilder(@Nonnull ServerBuilder serverBuilder) {
    Objects.requireNonNull(serverBuilder);
    this.serverBuilder = serverBuilder
        .intercept(requestCaptureInterceptor)
        .fallbackHandlerRegistry(delegateHandlerRegistry);
  }

  GrpcMockBuilder(int port) {
    this(ServerBuilder.forPort(Math.max(0, port)));
  }

  public GrpcMockBuilder interceptor(@Nonnull ServerInterceptor interceptor) {
    Objects.requireNonNull(interceptor);
    serverBuilder.intercept(interceptor);
    return this;
  }

  public GrpcMockBuilder executor(@Nullable Executor executor) {
    serverBuilder.executor(executor);
    return this;
  }

  public GrpcMockBuilder transportSecurity(@Nonnull File certChain, @Nonnull File privateKey) {
    Objects.requireNonNull(certChain);
    Objects.requireNonNull(privateKey);
    serverBuilder.useTransportSecurity(certChain, privateKey);
    return this;
  }

  public GrpcMock build() {
    return new GrpcMock(serverBuilder.build(), delegateHandlerRegistry.getDelegate(), requestCaptureInterceptor);
  }
}
