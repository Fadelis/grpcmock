package org.grpcmock.definitions.stub;

import static org.grpcmock.util.FunctionalHelpers.reverseStream;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCallHandler;
import io.grpc.ServerMethodDefinition;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.grpcmock.exception.GrpcMockException;
import org.grpcmock.exception.UnimplementedStatusException;
import org.grpcmock.interceptors.RequestCaptureInterceptor;

/**
 * @author Fadelis
 */
public class MethodStub<ReqT, RespT> {

  private final MethodDescriptor<ReqT, RespT> method;
  private final List<StubScenario<ReqT, RespT>> stubScenarios;

  MethodStub(
      @Nonnull MethodDescriptor<ReqT, RespT> method,
      @Nonnull List<StubScenario<ReqT, RespT>> stubScenarios
  ) {
    Objects.requireNonNull(method);
    Objects.requireNonNull(stubScenarios);
    Objects.requireNonNull(method.getServiceName());
    this.method = method;
    this.stubScenarios = new ArrayList<>(stubScenarios);
  }

  public String serviceName() {
    return this.method.getServiceName();
  }

  public String fullMethodName() {
    return this.method.getFullMethodName();
  }

  ServerMethodDefinition<ReqT, RespT> serverMethodDefinition() {
    return ServerMethodDefinition.create(method, serverCallHandler());
  }

  MethodStub registerScenarios(@Nonnull MethodStub<ReqT, RespT> methodStub) {
    Objects.requireNonNull(methodStub);
    if (!method.getFullMethodName().equals(methodStub.fullMethodName())) {
      throw new GrpcMockException("Cannot register stub scenarios for a different method");
    }
    this.stubScenarios.addAll(methodStub.stubScenarios);
    return this;
  }

  private ServerCallHandler<ReqT, RespT> serverCallHandler() {
    switch (method.getType()) {
      case UNARY:
        return ServerCalls.asyncUnaryCall(this::singleRequestCall);
      case SERVER_STREAMING:
        return ServerCalls.asyncServerStreamingCall(this::singleRequestCall);
      case CLIENT_STREAMING:
        return ServerCalls.asyncClientStreamingCall(responseObserver -> ServerCalls
            .asyncUnimplementedStreamingCall(method, responseObserver));
      case BIDI_STREAMING:
        return ServerCalls.asyncBidiStreamingCall(responseObserver -> ServerCalls
            .asyncUnimplementedStreamingCall(method, responseObserver));
      default:
        throw new GrpcMockException("Unsupported method type: " + method.getType());
    }

  }

  private void singleRequestCall(ReqT request, StreamObserver<RespT> streamObserver) {
    Metadata headers = RequestCaptureInterceptor.INTERCEPTED_HEADERS.get();
    Optional<StubScenario<ReqT, RespT>> maybeScenario = reverseStream(stubScenarios)
        .filter(scenario -> scenario.matches(headers))
        .filter(scenario -> scenario.matches(request))
        .findFirst();
    if (maybeScenario.isPresent()) {
      // pass the request to found scenario
      maybeScenario.get().call(request, streamObserver);
    } else {
      streamObserver.onError(new UnimplementedStatusException(
          "No matching stub scenario was found for this method: " + method.getFullMethodName()));
    }
  }
}
