package org.grpcmock.definitions.stub;

import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.grpcmock.interceptors.HeadersInterceptor;

public class ServiceStub<ReqT, RespT> {

  private final String serviceName;
  private final MethodDescriptor<ReqT, RespT> method;
  private final List<StubScenario<ReqT, RespT>> stubScenarios;

  ServiceStub(
      @Nonnull String serviceName,
      @Nonnull MethodDescriptor<ReqT, RespT> method,
      @Nonnull List<StubScenario<ReqT, RespT>> stubScenarios
  ) {
    this.serviceName = serviceName;
    this.method = method;
    this.stubScenarios = new ArrayList<>(stubScenarios);
  }

  public ServerServiceDefinition serverServiceDefinition() {
    return ServerServiceDefinition.builder(serviceName)
        .addMethod(method, ServerCalls.asyncUnaryCall(this::unaryCall))
        .build();
  }

  private void unaryCall(ReqT request, StreamObserver<RespT> streamObserver) {
    Optional<StubScenario<ReqT, RespT>> maybeScenario = stubScenarios.stream()
        .filter(scenario -> scenario.matches(HeadersInterceptor.INTERCEPTED_HEADERS.get()))
        .filter(scenario -> scenario.matches(request))
        .findFirst();
    if (maybeScenario.isPresent()) {
      maybeScenario.get().call(request, streamObserver);
    } else {
      streamObserver.onError(Status.UNIMPLEMENTED
          .withDescription("No matching stub scenario was found for this method: " + method
              .getFullMethodName())
          .asRuntimeException());
    }
  }
}
