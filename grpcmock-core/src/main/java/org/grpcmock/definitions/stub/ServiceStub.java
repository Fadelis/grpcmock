package org.grpcmock.definitions.stub;

import static java.util.Optional.ofNullable;

import io.grpc.ServerServiceDefinition;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.verification.RequestPattern;
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
    this.methodStubs.put(methodStub.fullMethodName(), methodStub);
  }

  public String serviceName() {
    return this.serviceName;
  }

  public ServerServiceDefinition serverServiceDefinition() {
    ServerServiceDefinition.Builder builder = ServerServiceDefinition.builder(serviceName);
    this.methodStubs.values().stream()
        .map(MethodStub::serverMethodDefinition)
        .forEach(builder::addMethod);

    return builder.build();
  }

  public <ReqT> int callCountFor(@Nonnull RequestPattern<ReqT> requestPattern) {
    Objects.requireNonNull(requestPattern);
    if (!serviceName.equals(requestPattern.serviceName())) {
      throw new GrpcMockException("Request method is not part of this service");
    }
    return ofNullable(methodStubs.get(requestPattern.fullMethodName()))
        .map(methodStub -> methodStub.callCountFor(requestPattern))
        .orElseThrow(() -> new GrpcMockException(
            "No stub found for the method: " + requestPattern.fullMethodName()));
  }

  public ServiceStub mergeServiceStub(@Nonnull ServiceStub serviceStub) {
    Objects.requireNonNull(serviceStub);
    if (!serviceName.equals(serviceStub.serviceName())) {
      throw new GrpcMockException("Method is not part of the actual service descriptor");
    }
    serviceStub.methodStubs.values().forEach(this::registerMethod);
    return this;
  }

  private <ReqT, RespT> ServiceStub registerMethod(@Nonnull MethodStub<ReqT, RespT> methodStub) {
    Objects.requireNonNull(methodStub);

    this.methodStubs.compute(
        methodStub.fullMethodName(),
        (key, oldValue) -> ofNullable(oldValue)
            .map(previous -> previous.registerScenarios(methodStub))
            .orElse(methodStub));

    return this;
  }
}
