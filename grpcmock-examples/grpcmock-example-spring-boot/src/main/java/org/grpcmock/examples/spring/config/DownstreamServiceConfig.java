package org.grpcmock.examples.spring.config;

import io.grpc.ManagedChannelBuilder;
import org.grpcmock.examples.v1.DownstreamServiceGrpc;
import org.grpcmock.examples.v1.DownstreamServiceGrpc.DownstreamServiceBlockingStub;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Fadelis
 */
@Configuration
public class DownstreamServiceConfig {

  @Bean
  public DownstreamServiceBlockingStub downstreamServiceBlockingStub(DownstreamServiceProperties properties) {
    return DownstreamServiceGrpc.newBlockingStub(ManagedChannelBuilder.forTarget(properties.getHost())
        .usePlaintext()
        .build());
  }
}
