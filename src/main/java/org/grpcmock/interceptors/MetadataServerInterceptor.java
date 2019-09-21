package org.grpcmock.interceptors;

import io.grpc.Context;
import io.grpc.Context.Key;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>gRPC server interceptor, which forwards received headers from {@link Metadata}
 * to request stack through {@link Context}.
 *
 * @author Fadelis
 */
public class MetadataServerInterceptor implements ServerInterceptor {

  public static final Map<String, Key<String>> INTERCEPTED_HEADERS = new ConcurrentHashMap<>();

  @Override
  public <ReqT, RespT> Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call,
      Metadata headers,
      ServerCallHandler<ReqT, RespT> next
  ) {
    Context ctx = headers.keys().stream()
        .map(key -> Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))
        .reduce(
            Context.current(),
            (context, key) -> context.withValue(
                INTERCEPTED_HEADERS.computeIfAbsent(key.name(), Context::key),
                headers.get(key)),
            (a, b) -> a);

    return Contexts.interceptCall(ctx, call, headers, next);
  }
}
