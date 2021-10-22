package org.grpcmock.ip.definitions.stub.steps;

import io.grpc.stub.StreamObserver;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.grpcmock.ip.definitions.BuilderStep;
import org.grpcmock.ip.definitions.response.Response;

/**
 * @author Fadelis
 */
public interface StreamRequestProxyResponseBuilderStep<BUILDER extends BuilderStep, ReqT, RespT> extends
    BuilderStep,
    MethodStubBuilder<ReqT, RespT> {

  /**
   * <p>Defines a proxying response, which will proxy the request to given {@link Response}. The
   * user is responsible that the request is completed correctly.
   * <p>Should be used when there needs to be more logic in the response method than returning a
   * simple response.
   *
   * For example:<code><pre>
   * .willProxyTo((responseObserver) -> new StreamObserver<ReqT>() {
   *
   *    public void onNext(V value) {
   *      // handle incoming requests
   *    }
   *
   *    public void onError(Throwable throwable) {
   *      // handle error;
   *    }
   *
   *    public void onCompleted() {
   *      // send response on completion
   *      responseObserver.onNext(responseObject);
   *      responseObserver.onCompleted();
   *    }
   * });
   * </pre></code>
   */
  BUILDER willProxyTo(@Nonnull Function<StreamObserver<RespT>, StreamObserver<ReqT>> streamRequestResponseProxy);
}
