package org.grpcmock.ip.examples.spring.interceptor;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.Metadata.AsciiMarshaller;
import io.grpc.MethodDescriptor;

/**
 * @author Fadelis
 */
public class ApikeyInterceptor implements ClientInterceptor {

  private static final AsciiMarshaller<String> MARSHALLER = Metadata.ASCII_STRING_MARSHALLER;
  public static final Metadata.Key<String> APIKEY_HEADER = Metadata.Key.of("Authorization", MARSHALLER);
  public static final String AUTHORIZATION_VALUE_PREFIX = "Berear ";

  private final String apikey;

  public ApikeyInterceptor(String apikey) {
    this.apikey = apikey;
  }

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
      MethodDescriptor<ReqT, RespT> method,
      CallOptions callOptions,
      Channel next
  ) {
    return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        headers.put(APIKEY_HEADER, AUTHORIZATION_VALUE_PREFIX + apikey);
        super.start(responseListener, headers);
      }
    };
  }
}
