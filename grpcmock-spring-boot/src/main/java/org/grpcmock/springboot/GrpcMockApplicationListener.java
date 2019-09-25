package org.grpcmock.springboot;

import static java.util.Optional.ofNullable;

import java.util.HashMap;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.SocketUtils;

/**
 * Prepares environment for gRPC Mock and finds a free port if needed.
 *
 * @author Fadelis
 */
public class GrpcMockApplicationListener implements ApplicationListener<ApplicationPreparedEvent> {

  @Override
  public void onApplicationEvent(ApplicationPreparedEvent event) {
    registerPort(event.getApplicationContext().getEnvironment());
  }

  private void registerPort(ConfigurableEnvironment environment) {
    Integer httpPort = environment.getProperty("grpcmock.server.port", Integer.class);
    // If httpPort is not found it means the AutoConfigureGrpcMock hasn't been initialised.
    if (httpPort == null) {
      return;
    }

    if (httpPort.equals(0)) {
      int availablePort = SocketUtils.findAvailableTcpPort();
      MapPropertySource properties = ofNullable(environment.getPropertySources().remove("grpcmock"))
          .map(MapPropertySource.class::cast)
          .orElseGet(() -> new MapPropertySource("grpcmock", new HashMap<>()));
      environment.getPropertySources().addFirst(properties);
      properties.getSource().put("grpcmock.server.port", availablePort);
      properties.getSource().put("grpcmock.server.port-dynamic", true);
    }
  }
}
