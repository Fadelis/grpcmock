package org.grpcmock.junit5;

import static java.util.Optional.ofNullable;

import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.grpcmock.GrpcMock;
import org.grpcmock.GrpcMockBuilder;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.ModifierSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC Mock extension for JUnit5. All stub mappings are reset after each test method. After all tests in the test class are done
 * the server will be shutdown.
 *
 * @author Fadelis
 */
public class GrpcMockExtension implements BeforeAllCallback, AfterAllCallback, AfterEachCallback {

  private static final Logger log = LoggerFactory.getLogger(GrpcMockExtension.class);

  protected final GrpcMock server;

  public GrpcMockExtension() {
    this(GrpcMock.grpcMock().build());
  }

  public GrpcMockExtension(@Nonnull GrpcMock server) {
    Objects.requireNonNull(server);
    this.server = server;
    init();
  }

  public static Builder builder() {
    return new Builder();
  }

  private void init() {
    this.server.start();
    GrpcMock.configureFor(this.server);
    logServerStarted();
  }

  private void logServerStarted() {
    log.debug("Started gRPC Mock server at port: {}", this.server.getPort());
  }

  public int getPort() {
    return this.server.getPort();
  }

  public GrpcMock getInstance() {
    return this.server;
  }

  @Override
  public void beforeAll(ExtensionContext extensionContext) {
    server.resetAll();
    log.debug("Resetting all stub mappings before the tests");
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    if (extensionContext.getTestClass().map(this::isNestedClass).orElse(Boolean.FALSE)) {
      return;
    }
    server.resetAll();
    server.stop();
    log.debug("Stopping gRPC Mock server.");
  }

  @Override
  public void afterEach(ExtensionContext extensionContext) {
    server.resetAll();
    log.debug("Resetting all stub mappings after a test");
  }

  private boolean isNestedClass(Class<?> currentClass) {
    return !ModifierSupport.isStatic(currentClass) && currentClass.isMemberClass();
  }

  public static class Builder {

    private int port = 0;
    private Executor executor;
    private final List<ServerInterceptor> interceptors = new ArrayList<>();
    private File certChain;
    private File privateKey;

    Builder() {
    }

    /**
     * Defines the port value for the gRPC Mock server. If set to <code>0</code> a random free port will be picked.
     */
    public Builder withPort(int port) {
      this.port = port;
      return this;
    }

    /**
     * <p>Defines {@link Executor} to be used for the gRPC Mock server.
     * <p>If not defined a default {@link Executor} from {@link ServerBuilder} will be used.
     */
    public Builder withExecutor(@Nullable Executor executor) {
      this.executor = executor;
      return this;
    }

    /**
     * Defines {@link ServerInterceptor} for the gRPC Mock server.
     */
    public Builder withInterceptor(@Nonnull ServerInterceptor interceptor) {
      Objects.requireNonNull(interceptor);
      this.interceptors.add(interceptor);
      return this;
    }

    /**
     * Defines certChain and privateKey files in order to configure server security via
     * {@link ServerBuilder#useTransportSecurity(File, File)}.
     */
    public Builder withTransportSecurity(@Nonnull File certChain, @Nonnull File privateKey) {
      Objects.requireNonNull(certChain);
      Objects.requireNonNull(privateKey);
      this.certChain = certChain;
      this.privateKey = privateKey;
      return this;
    }

    /**
     * Defines certChain and privateKey files in order to configure server security via
     * {@link ServerBuilder#useTransportSecurity(File, File)}.
     */
    public Builder withTransportSecurity(@Nonnull String certChain, @Nonnull String privateKey) {
      Objects.requireNonNull(certChain);
      Objects.requireNonNull(privateKey);
      this.certChain = Paths.get(certChain).toFile();
      this.privateKey = Paths.get(privateKey).toFile();
      return this;
    }

    public GrpcMockExtension build() {
      GrpcMockBuilder builder = GrpcMock.grpcMock(port);
      ofNullable(executor).ifPresent(builder::executor);
      interceptors.forEach(builder::interceptor);
      if (Objects.nonNull(certChain) && Objects.nonNull(privateKey)) {
        builder.transportSecurity(certChain, privateKey);
      }
      return new GrpcMockExtension(builder.build());
    }
  }
}
