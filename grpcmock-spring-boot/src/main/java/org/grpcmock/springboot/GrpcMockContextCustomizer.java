package org.grpcmock.springboot;

import static java.nio.file.Paths.get;
import static java.util.Optional.ofNullable;

import io.grpc.ServerInterceptor;
import io.grpc.inprocess.InProcessServerBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.grpcmock.GrpcMock;
import org.grpcmock.GrpcMockBuilder;
import org.grpcmock.exception.GrpcMockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.util.StringUtils;

/**
 * Builds and starts the gRPC Mock server during context customization, before any beans
 * are created. This ensures the actual port is available in the Spring environment for
 * {@code @Value("${grpcmock.server.port}")} injection, and eliminates the TOCTOU race
 * condition of pre-allocating a port.
 *
 * <p>Owns the full server lifecycle: creation, start, stop on context close.
 *
 * @author Fadelis
 */
class GrpcMockContextCustomizer implements ContextCustomizer {

  static final String GRPCMOCK_BEAN_NAME = "grpcMock";
  private static final Logger log = LoggerFactory.getLogger(GrpcMockContextCustomizer.class);
  private final AutoConfigureGrpcMock properties;

  GrpcMockContextCustomizer(AutoConfigureGrpcMock annotation) {
    this.properties = annotation;
  }

  @Override
  public void customizeContext(
      ConfigurableApplicationContext context,
      MergedContextConfiguration mergedConfig
  ) {
    // Start server with atomically assigned port by the OS
    GrpcMock server = configureServerBuilder(context).build();
    server.start();

    // Register properties and server as a singleton bean and set global instance
    GrpcMock.configureFor(server);
    registerProperties(context, server);
    context.getBeanFactory().registerSingleton(GRPCMOCK_BEAN_NAME, server);

    // Stop server when context closes
    context.addApplicationListener((ContextClosedEvent event) -> {
      server.stop();
      log.debug("Stopped GrpcMock instance");
    });

    if (properties.useInProcessServer()) {
      log.debug("Creating a new GrpcMock in-process server with name [{}]", server.getInProcessName());
    } else {
      log.debug("Creating a new GrpcMock server at http port [{}]", server.getPort());
    }
  }

  private GrpcMockBuilder configureServerBuilder(ConfigurableApplicationContext context) {
    GrpcMockBuilder serverBuilder;
    if (properties.useInProcessServer()) {
      String serverName = StringUtils.hasText(properties.name())
          ? properties.name()
          : InProcessServerBuilder.generateName();
      serverBuilder = GrpcMock.inProcessGrpcMock(serverName);
    } else {
      serverBuilder = GrpcMock.grpcMock(properties.port());
    }
    // Register server interceptors
    ofNullable(properties.interceptors())
        .ifPresent(interceptors -> Stream.of(interceptors)
            .filter(Objects::nonNull)
            .map(this::getInterceptorInstance)
            .forEach(serverBuilder::interceptor));
    // Register executor if present
    Executor executor = ofNullable(properties.executorBeanName())
        .filter(StringUtils::hasText)
        .map(name -> getLazyExecutor(context, name))
        .orElseGet(() -> Optional.of(properties.executorThreadCount())
            .filter(threads -> threads > 0)
            .map(Executors::newFixedThreadPool)
            .orElse(null));
    ofNullable(executor).ifPresent(serverBuilder::executor);
    // Register transport security certChain and privateKey if present
    String certChain = properties.certChainFile();
    String privateKey = properties.privateKeyFile();
    if (StringUtils.hasText(certChain) && StringUtils.hasText(privateKey)) {
      serverBuilder.transportSecurity(get(certChain).toFile(), get(privateKey).toFile());
    } else if (!Objects.equals(certChain, privateKey)) {
      throw new GrpcMockException("Both certChain and privateKey have to be defined");
    }
    return serverBuilder;
  }

  private void registerProperties(ConfigurableApplicationContext context, GrpcMock server) {
    Map<String, Object> source = new HashMap<>();
    source.put("grpcmock.server.port", server.getPort());
    source.put("grpcmock.server.name", properties.useInProcessServer() ? server.getInProcessName() : "");
    source.put("grpcmock.server.port-dynamic", properties.port() == 0 && !StringUtils.hasText(properties.name()));

    context.getEnvironment().getPropertySources()
        .addFirst(new MapPropertySource("grpcmock", source));
  }

  private ServerInterceptor getInterceptorInstance(Class<? extends ServerInterceptor> clazz) {
    try {
      return clazz.getConstructor().newInstance();
    } catch (Exception e) {
      throw new GrpcMockException("Default constructor is not accessible " +
          "or missing no-args constructor for interceptor: " + clazz, e);
    }
  }

  private Executor getLazyExecutor(ConfigurableApplicationContext context, String executorBeanName) {
    return command -> context.getBean(executorBeanName, Executor.class).execute(command);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GrpcMockContextCustomizer)) {
      return false;
    }
    GrpcMockContextCustomizer that = (GrpcMockContextCustomizer) o;
    return Objects.equals(this.properties, that.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(properties);
  }
}
