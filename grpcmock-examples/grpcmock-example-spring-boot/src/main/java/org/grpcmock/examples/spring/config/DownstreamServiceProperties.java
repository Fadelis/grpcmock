package org.grpcmock.examples.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Fadelis
 */
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
