package org.grpcmock.interceptors;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>gRPC server interceptor, which forwards received headers from {@link Metadata}
 * to request stack through {@link Context}.
 *
 * @author Fadelis
 */
public class HeadersInterceptor implements ServerInterceptor {

  public static final Context.Key<Map<String, String>> INTERCEPTED_HEADERS = Context.key("headers");

  @Override
  public <ReqT, RespT> Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call,
      Metadata metadata,
      ServerCallHandler<ReqT, RespT> next
  ) {
    Map<String, String> headers = metadata.keys().stream()
        .map(key -> Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))
        .collect(Collectors.toMap(Metadata.Key::name, metadata::get));
    Context ctx = Context.current().withValue(INTERCEPTED_HEADERS, headers);

    return Contexts.interceptCall(ctx, call, metadata, next);
  }
}
