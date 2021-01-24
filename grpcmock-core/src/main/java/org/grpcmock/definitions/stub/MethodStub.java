package org.grpcmock.definitions.stub;

import static org.grpcmock.util.FunctionalHelpers.reverseStream;

import io.grpc.MethodDescriptor;
import io.grpc.ServerCallHandler;
import io.grpc.ServerMethodDefinition;
import io.grpc.StatusRuntimeException;
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
public final class MethodStub<ReqT, RespT> {

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

  MethodStub<ReqT, RespT> registerScenarios(@Nonnull MethodStub<ReqT, RespT> methodStub) {
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
        return ServerCalls.asyncClientStreamingCall(this::streamRequestCall);
      case BIDI_STREAMING:
        return ServerCalls.asyncBidiStreamingCall(this::streamRequestCall);
      default:
        throw new GrpcMockException("Unsupported method type: " + method.getType());
    }

  }

  private void singleRequestCall(ReqT request, StreamObserver<RespT> responseObserver) {
    Optional<StubScenario<ReqT, RespT>> maybeScenario = findStub();
    if (maybeScenario.isPresent()) {
      maybeScenario.get().call(request, responseObserver);
    } else {
      responseObserver.onError(stubNotFoundException());
    }
  }

  private StreamObserver<ReqT> streamRequestCall(StreamObserver<RespT> responseObserver) {
    // client and bidi streaming methods are invoked without any request
    // so we return a wrapped request observer which will check for a matching stub on every incoming request
    // and then will proxy to it all future requests and all requests received until match was found.
    return new WrappedRequestStreamObserver(responseObserver);
  }

  private Optional<StubScenario<ReqT, RespT>> findStub() {
    return reverseStream(stubScenarios)
        .filter(scenario -> scenario.matches(RequestCaptureInterceptor.getCapturedRequest()))
        .findFirst();
  }

  private StatusRuntimeException stubNotFoundException() {
    return new UnimplementedStatusException("No matching stub scenario was found for this method: " + method.getFullMethodName());
  }

  private class WrappedRequestStreamObserver implements StreamObserver<ReqT> {

    private final StreamObserver<RespT> responseObserver;
    private StreamObserver<ReqT> delegate;

    private WrappedRequestStreamObserver(StreamObserver<RespT> responseObserver) {
      this.responseObserver = responseObserver;
    }

    @Override
    public void onNext(ReqT request) {
      if (Objects.nonNull(delegate)) {
        delegate.onNext(request);
      } else {
        findStub().ifPresent(stub -> {
          delegate = stub.call(responseObserver);
          // pass all previous requests to the found delegate
          RequestCaptureInterceptor.<ReqT>getCapturedRequest().requests()
              .forEach(delegate::onNext);
        });
      }
    }

    @Override
    public void onError(Throwable error) {
      if (Objects.nonNull(delegate)) {
        delegate.onError(error);
      }
    }

    @Override
    public void onCompleted() {
      if (Objects.nonNull(delegate)) {
        delegate.onCompleted();
      } else {
        responseObserver.onError(stubNotFoundException());
      }
    }
  }
}
