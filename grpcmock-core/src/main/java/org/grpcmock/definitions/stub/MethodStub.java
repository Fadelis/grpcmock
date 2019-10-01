package org.grpcmock.definitions.stub;

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
import org.grpcmock.interceptors.HeadersInterceptor;

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
    this.method = method;
    this.stubScenarios = new ArrayList<>(stubScenarios);
  }

  public MethodDescriptor<ReqT, RespT> method() {
    return this.method;
  }

  ServerMethodDefinition<ReqT, RespT> serverMethodDefinition() {
    return ServerMethodDefinition.create(method, serverCallHandler());
  }

  MethodStub registerScenarios(@Nonnull MethodStub<ReqT, RespT> methodStub) {
    Objects.requireNonNull(methodStub);
    if (!method.getFullMethodName().equals(methodStub.method().getFullMethodName())) {
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
    Optional<StubScenario<ReqT, RespT>> maybeScenario = stubScenarios.stream()
        .filter(scenario -> scenario.matches(HeadersInterceptor.INTERCEPTED_HEADERS.get()))
        .filter(scenario -> scenario.matches(request))
        .findFirst();
    if (maybeScenario.isPresent()) {
      maybeScenario.get().call(request, streamObserver);
    } else {
      streamObserver.onError(new UnimplementedStatusException(
          "No matching stub scenario was found for this method: " + method.getFullMethodName()));
    }
  }
}
