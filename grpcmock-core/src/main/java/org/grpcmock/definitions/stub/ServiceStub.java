package org.grpcmock.definitions.stub;

import static java.util.Optional.ofNullable;

import io.grpc.ServerServiceDefinition;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.grpcmock.exception.GrpcMockException;

/**
 * @author Fadelis
 */
public class ServiceStub {

  private final String serviceName;
  private final Map<String, MethodStub> methodStubs = new ConcurrentHashMap<>();

  ServiceStub(
      @Nonnull String serviceName,
      @Nonnull MethodStub<?, ?> methodStub
  ) {
    Objects.requireNonNull(serviceName);
    Objects.requireNonNull(methodStub);
    this.serviceName = serviceName;
    this.methodStubs.put(methodStub.method().getFullMethodName(), methodStub);
  }

  public String serviceName() {
    return this.serviceName;
  }

  public ServiceStub mergeServiceStub(@Nonnull ServiceStub serviceStub) {
    Objects.requireNonNull(serviceStub);
    serviceStub.methodStubs.values().forEach(this::registerMethod);
    return this;
  }

  public <ReqT, RespT> ServiceStub registerMethod(@Nonnull MethodStub<ReqT, RespT> methodStub) {
    Objects.requireNonNull(methodStub);
    if (!serviceName.equals(methodStub.method().getServiceName())) {
      throw new GrpcMockException("Method is not part of the actual service descriptor");
    }
    this.methodStubs.compute(
        methodStub.method().getFullMethodName(),
        (key, oldValue) -> ofNullable(oldValue)
            .map(methodStub::registerScenarios)
            .orElse(methodStub));

    return this;
  }

  public ServerServiceDefinition serverServiceDefinition() {
    ServerServiceDefinition.Builder builder = ServerServiceDefinition.builder(serviceName);
    this.methodStubs.values().stream()
        .map(MethodStub::serverMethodDefinition)
        .forEach(builder::addMethod);

    return builder.build();
  }
}
