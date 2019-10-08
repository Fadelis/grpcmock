package org.grpcmock.interceptors;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

/**
 * <p>gRPC server interceptor, which forwards received headers from {@link Metadata}
 * to request stack through {@link Context}.
 *
 * @author Fadelis
 */
public class HeadersInterceptor implements ServerInterceptor {

  public static final Context.Key<Metadata> INTERCEPTED_HEADERS = Context.key("headers");

  @Override
  public <ReqT, RespT> Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call,
      Metadata metadata,
      ServerCallHandler<ReqT, RespT> next
  ) {
    Metadata capturedHeaders = new Metadata();
    capturedHeaders.merge(metadata);
    Context ctx = Context.current().withValue(INTERCEPTED_HEADERS, capturedHeaders);

    return Contexts.interceptCall(ctx, call, metadata, next);
  }
}
