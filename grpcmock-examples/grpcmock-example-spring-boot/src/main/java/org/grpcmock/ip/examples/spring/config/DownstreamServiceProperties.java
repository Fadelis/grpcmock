package org.grpcmock.ip.examples.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * @author Fadelis
 */
@ConstructorBinding
@ConfigurationProperties("downstream-service")
public class DownstreamServiceProperties {

  private final String host;

  public DownstreamServiceProperties(String host) {
    this.host = host;
  }

  public String getHost() {
    return this.host;
  }
}
