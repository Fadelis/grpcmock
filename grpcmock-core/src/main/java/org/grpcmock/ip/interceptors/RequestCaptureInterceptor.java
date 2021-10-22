package org.grpcmock.ip.interceptors;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;
import org.grpcmock.ip.GrpcMock;
import org.grpcmock.ip.definitions.verification.RequestPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Fadelis
 */
public final class RequestCaptureInterceptor implements ServerInterceptor {

  private static final Logger log = LoggerFactory.getLogger(GrpcMock.class);
  private static final String SEPARATOR = "----------------------------------------";
  private static final Context.Key<CapturedRequest> CAPTURED_REQUEST = Context.key("capture_request");

  private final Queue<CapturedRequest> capturedRequests = new ConcurrentLinkedQueue<>();

  public int callCountFor(@Nonnull RequestPattern<?> requestPattern) {
    Objects.requireNonNull(requestPattern);
    return Math.toIntExact(capturedRequests.stream()
        .filter(requestPattern::matches)
        .count());
  }

  public void clear() {
    capturedRequests.clear();
  }

  @Override
  public <ReqT, RespT> Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call,
      Metadata metadata,
      ServerCallHandler<ReqT, RespT> next
  ) {
    MethodDescriptor<ReqT, RespT> method = call.getMethodDescriptor();
    Metadata headers = getCapturedMetadata(metadata);
    List<ReqT> requests = new CopyOnWriteArrayList<>();
    CapturedRequest<ReqT> capturedRequest = captureRequest(method, headers, requests);

    ServerCall<ReqT, RespT> forwardingCall = new SimpleForwardingServerCall<ReqT, RespT>(call) {
      @Override
      public void close(Status status, Metadata trailers) {
        capturedRequest.setCloseStatus(status);
        super.close(status, trailers);
      }
    };
    Context ctx = Context.current().withValue(CAPTURED_REQUEST, capturedRequest);
    Listener<ReqT> interceptedListener = Contexts.interceptCall(ctx, forwardingCall, metadata, next);

    return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(interceptedListener) {
      @Override
      public void onMessage(ReqT message) {
        requests.add(message);
        log.info("\n{}\nReceived request:\n{}\n{}", SEPARATOR, capturedRequest, SEPARATOR);
        super.onMessage(message);
      }
    };
  }

  private <ReqT> CapturedRequest<ReqT> captureRequest(MethodDescriptor<ReqT, ?> method, Metadata headers, List<ReqT> requests) {
    CapturedRequest<ReqT> capturedRequest = new CapturedRequest<>(method, headers, requests);
    if (!capturedRequests.offer(capturedRequest)) {
      log.warn("Failed to capture request in the queue");
    }
    return capturedRequest;
  }

  private Metadata getCapturedMetadata(Metadata incomingMetadata) {
    Metadata capturedHeaders = new Metadata();
    capturedHeaders.merge(incomingMetadata);
    return capturedHeaders;
  }

  public static <ReqT> CapturedRequest<ReqT> getCapturedRequest() {
    return CAPTURED_REQUEST.get();
  }
}
