package org.grpcmock.springboot;

import static java.nio.file.Paths.get;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import io.grpc.ServerInterceptor;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.grpcmock.GrpcMock;
import org.grpcmock.GrpcMockBuilder;
import org.grpcmock.exception.GrpcMockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @author Fadelis
 */
@Configuration
@EnableConfigurationProperties(GrpcMockProperties.class)
public class GrpcMockConfiguration implements SmartLifecycle {

  private static final Logger log = LoggerFactory.getLogger(GrpcMockConfiguration.class);
  private static final String GRPCMOCK_BEAN_NAME = "grpcMock";

  private final GrpcMockProperties properties;
  private final ApplicationContext context;
  private final DefaultListableBeanFactory beanFactory;

  private volatile boolean running;

  private GrpcMock server;

  @Autowired
  GrpcMockConfiguration(
      GrpcMockProperties properties,
      ApplicationContext context,
      DefaultListableBeanFactory beanFactory
  ) {
    this.properties = properties;
    this.context = context;
    this.beanFactory = beanFactory;
  }

  @PostConstruct
  public void init() {
    if (isRunning()) {
      resetAll();
      updateGlobalServer();
      return; // no need to reinitialise
    }
    // Create server builder with the configured port
    GrpcMockBuilder serverBuilder = GrpcMock.grpcMock(properties.getServer().getPort());
    // Register server interceptors
    ofNullable(properties.getServer().getInterceptors())
        .ifPresent(interceptors -> Stream.of(interceptors)
            .filter(Objects::nonNull)
            .map(this::getInterceptorInstance)
            .forEach(serverBuilder::interceptor));
    // Register executor if present
    Executor executor = ofNullable(properties.getServer().getExecutorBeanName())
        .filter(StringUtils::hasText)
        .map(name -> context.getBean(name, Executor.class))
        .orElseGet(() -> of(properties.getServer().getExecutorThreadCount())
            .filter(threads -> threads > 0)
            .map(Executors::newFixedThreadPool)
            .orElse(null));
    ofNullable(executor).ifPresent(serverBuilder::executor);
    // Register transport security certChain and privateKey if present
    String certChain = properties.getServer().getCertChainFile();
    String privateKey = properties.getServer().getPrivateKeyFile();
    if (StringUtils.hasText(certChain) && StringUtils.hasText(privateKey)) {
      serverBuilder.transportSecurity(get(certChain).toFile(), get(privateKey).toFile());
    } else if (!Objects.equals(certChain, privateKey)) {
      throw new GrpcMockException("Both certChain and privateKey have to be defined");
    }
    // build the gRPC Mock server
    log.debug(String.format(
        "Creating a new GrpcMock server at http port [%d]",
        properties.getServer().getPort()));
    server = serverBuilder.build();
  }

  @Override
  public void start() {
    this.server.start();
    updateGlobalServer();
  }

  @Override
  public void stop() {
    if (isRunning()) {
      this.server.stop();
      this.server = null;
      this.running = false;
      log.debug("Stopped GrpcMock instance");
    } else {
      log.debug("Server already stopped");
    }
  }

  @Override
  public boolean isRunning() {
    return this.running;
  }

  public void resetAll() {
    this.server.resetAll();
  }

  private void updateGlobalServer() {
    GrpcMock.configureFor(this.server);
    recreateBean();
    this.running = true;
    if (log.isDebugEnabled()) {
      log.debug(String.format("Started GrpcMock at port [%d]", properties.getServer().getPort()));
    }
  }

  private void recreateBean() {
    if (!this.beanFactory.containsBean(GRPCMOCK_BEAN_NAME)) {
      this.beanFactory.registerSingleton(GRPCMOCK_BEAN_NAME, this.server);
    } else {
      this.beanFactory.destroySingleton(GRPCMOCK_BEAN_NAME);
      this.beanFactory.registerSingleton(GRPCMOCK_BEAN_NAME, this.server);
    }
  }

  private ServerInterceptor getInterceptorInstance(Class<? extends ServerInterceptor> clazz) {
    try {
      return clazz.getConstructor().newInstance();
    } catch (Exception e) {
      throw new GrpcMockException("Default constructor is not accessible " +
          "or missing no-args constructor for interceptor: " + clazz, e);
    }
  }
}
