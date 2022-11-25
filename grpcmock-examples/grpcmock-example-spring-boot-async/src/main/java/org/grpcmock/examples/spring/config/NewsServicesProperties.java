package org.grpcmock.examples.spring.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * @author Fadelis
 */
@ConfigurationProperties("news-services")
public class NewsServicesProperties {

  private final List<NewsServiceProperties> services;

  public NewsServicesProperties(List<NewsServiceProperties> services) {
    this.services = services;
  }

  public List<NewsServiceProperties> getServices() {
    return services;
  }

  public static class NewsServiceProperties {

    private final String host;
    public final String apikey;

    public NewsServiceProperties(String host, String apikey) {
      this.host = host;
      this.apikey = apikey;
    }

    public String getHost() {
      return this.host;
    }

    public String getApikey() {
      return this.apikey;
    }
  }
}
