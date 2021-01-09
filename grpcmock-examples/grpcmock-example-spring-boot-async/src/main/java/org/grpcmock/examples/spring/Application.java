package org.grpcmock.examples.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * @author Fadelis
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
