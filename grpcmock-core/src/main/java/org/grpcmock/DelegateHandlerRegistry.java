package org.grpcmock;

import io.grpc.HandlerRegistry;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.ServerCallHandler;
import io.grpc.ServerMethodDefinition;
import io.grpc.Status;
import io.grpc.services.BinaryLogProvider;
import io.grpc.stub.ServerCalls;
import io.grpc.util.MutableHandlerRegistry;
import javax.annotation.Nullable;

/**
 * @author Fadelis
 */
public final class DelegateHandlerRegistry extends HandlerRegistry {

  private final MutableHandlerRegistry delegate = new MutableHandlerRegistry();

  public MutableHandlerRegistry getDelegate() {
    return this.delegate;
  }

  @Override
  public ServerMethodDefinition<?, ?> lookupMethod(String methodName, @Nullable String authority) {
    ServerMethodDefinition<?, ?> delegateDefinition = delegate.lookupMethod(methodName, authority);
    return delegateDefinition != null ? delegateDefinition : notFoundServerMethod(methodName);
  }

  private static ServerMethodDefinition<byte[], byte[]> notFoundServerMethod(String fullMethodName) {
    return ServerMethodDefinition.create(noopMethod(fullMethodName), notFoundUnaryCall(fullMethodName));
  }

  private static ServerCallHandler<byte[], byte[]> notFoundUnaryCall(String fullMethodName) {
    return ServerCalls.asyncUnaryCall((request, responseObserver) -> responseObserver.onError(Status.UNIMPLEMENTED
        .withDescription(String.format("Method not found: %s", fullMethodName))
        .asRuntimeException()));
  }

  private static MethodDescriptor<byte[], byte[]> noopMethod(String fullMethodName) {
    return MethodDescriptor.newBuilder(BinaryLogProvider.BYTEARRAY_MARSHALLER, BinaryLogProvider.BYTEARRAY_MARSHALLER)
        .setType(MethodType.UNARY)
        .setFullMethodName(fullMethodName)
        .build();
  }
}
