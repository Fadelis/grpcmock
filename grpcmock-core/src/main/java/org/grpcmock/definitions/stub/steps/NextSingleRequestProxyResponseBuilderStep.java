package org.grpcmock.definitions.stub.steps;

import io.grpc.stub.StreamObserver;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.Response;

/**
 * @author Fadelis
 */
public interface NextSingleRequestProxyResponseBuilderStep<BUILDER extends NextSingleRequestProxyResponseBuilderStep<BUILDER, ReqT, RespT>, ReqT, RespT> extends
    BuilderStep,
    MethodStubBuilder<ReqT, RespT> {

  /**
   * <p>Defines a proxying response, which will proxy the request to given {@link Response} for
   * subsequent request call to this stub. The user is responsible that the request is completed
   * correctly.
   * <p>Should be used when there needs to be more logic in the response method than returning a
   * simple response.
   *
   * For example:<code><pre>
   * .nextWillProxyTo((request, responseObserver) -> {
   *   responseObserver.onNext(responseObject);
   *   responseObserver.onCompleted();
   * });
   * </pre></code>
   */
  BUILDER nextWillProxyTo(@Nonnull BiConsumer<ReqT, StreamObserver<RespT>> responseProxy);
}
