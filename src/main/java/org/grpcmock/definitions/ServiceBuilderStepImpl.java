package org.grpcmock.definitions;

import io.grpc.MethodDescriptor;
import io.grpc.ServiceDescriptor;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.steps.ServiceBuilderStep;
import org.grpcmock.exception.GrpcMockException;

public class ServiceBuilderStepImpl implements ServiceBuilderStep {

  private final String serviceName;
  private ServiceDescriptor serviceDescriptor;

  public ServiceBuilderStepImpl(@Nonnull ServiceDescriptor serviceDescriptor) {
    this.serviceName = serviceDescriptor.getName();
    this.serviceDescriptor = serviceDescriptor;
  }

  public ServiceBuilderStepImpl(@Nonnull String serviceName) {
    this.serviceName = serviceName;
  }

  @Override
  public <ReqT, RespT> SingleResponseBuilderStepImpl<ReqT, RespT> forMethod(
      @Nonnull MethodDescriptor<ReqT, RespT> method
  ) {
    Objects.requireNonNull(method);
    if (Objects.nonNull(serviceDescriptor) && !serviceDescriptor.getMethods().contains(method)) {
      throw new GrpcMockException("Method is not part of the actual service descriptor");
    }
    if (!method.getType().clientSendsOneMessage()) {
      throw new GrpcMockException("This builder accepts only Unary and Server streaming methods");
    }
    return new SingleResponseBuilderStepImpl<>(serviceName, method);
  }
}
