package org.grpcmock.ip.examples.spring.config;

import io.grpc.ManagedChannelBuilder;
import java.util.List;
import java.util.stream.Collectors;
import org.grpcmock.ip.examples.spring.interceptor.ApikeyInterceptor;
import org.grpcmock.ip.examples.spring.service.DownstreamNewsService;
import org.grpcmock.ip.examples.v1.NewsServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Fadelis
 */
@Configuration
public class NewsServicesConfig {

  @Bean
  public List<DownstreamNewsService> downstreamServices(NewsServicesProperties properties) {
    return properties.getServices().stream()
        .map(serviceProperties -> NewsServiceGrpc.newFutureStub(ManagedChannelBuilder
            .forTarget(serviceProperties.getHost())
            .intercept(new ApikeyInterceptor(serviceProperties.getApikey()))
            .usePlaintext()
            .build()))
        .map(DownstreamNewsService::new)
        .collect(Collectors.toList());
  }
}
