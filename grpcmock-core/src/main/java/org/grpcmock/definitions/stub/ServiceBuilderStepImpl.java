package org.grpcmock.definitions.stub;

import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.ServiceDescriptor;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.stub.steps.BidiStreamingMethodStubBuilderStep;
import org.grpcmock.definitions.stub.steps.ClientStreamingMethodStubBuilderStep;
import org.grpcmock.definitions.stub.steps.ServerStreamingMethodStubBuilderStep;
import org.grpcmock.definitions.stub.steps.ServiceBuilderStep;
import org.grpcmock.definitions.stub.steps.UnaryMethodStubBuilderStep;
import org.grpcmock.exception.GrpcMockException;

/**
 * @author Fadelis
 */
public class ServiceBuilderStepImpl implements ServiceBuilderStep {

  private final String serviceName;

  public ServiceBuilderStepImpl(@Nonnull ServiceDescriptor serviceDescriptor) {
    Objects.requireNonNull(serviceDescriptor);
    this.serviceName = serviceDescriptor.getName();
  }

  public ServiceBuilderStepImpl(@Nonnull String serviceName) {
    Objects.requireNonNull(serviceName);
    this.serviceName = serviceName;
  }

  @Override
  public <ReqT, RespT> UnaryMethodStubBuilderStep<ReqT, RespT> forMethod(
      @Nonnull MethodDescriptor<ReqT, RespT> method
  ) {
    Objects.requireNonNull(method);
    if (!serviceName.equals(method.getServiceName())) {
      throw new GrpcMockException("Method is not part of the actual service descriptor");
    }
    if (!method.getType().clientSendsOneMessage()) {
      throw new GrpcMockException("This builder accepts only Unary and Server streaming methods");
    }
    return new UnaryMethodStubBuilderImpl<>(serviceName, method);
  }

  @Override
  public <ReqT, RespT> ServerStreamingMethodStubBuilderStep<ReqT, RespT> forServerStreamingMethod(
      @Nonnull MethodDescriptor<ReqT, RespT> method) {
    Objects.requireNonNull(method);
    if (!serviceName.equals(method.getServiceName())) {
      throw new GrpcMockException("Method is not part of the actual service descriptor");
    }
    if (method.getType() != MethodType.SERVER_STREAMING) {
      throw new GrpcMockException("This builder accepts only Server streaming methods");
    }
    return new ServerStreamingMethodStubBuilderImpl<>(serviceName, method);
  }

  @Override
  public <ReqT, RespT> ClientStreamingMethodStubBuilderStep<ReqT, RespT> forClientStreamingMethod(
      @Nonnull MethodDescriptor<ReqT, RespT> method) {
    throw new GrpcMockException("Not yet implemented");
  }

  @Override
  public <ReqT, RespT> BidiStreamingMethodStubBuilderStep<ReqT, RespT> forBidiStreamingMethod(
      @Nonnull MethodDescriptor<ReqT, RespT> method) {
    throw new GrpcMockException("Not yet implemented");
  }
}
