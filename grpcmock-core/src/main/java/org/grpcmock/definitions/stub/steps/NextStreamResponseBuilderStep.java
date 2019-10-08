package org.grpcmock.definitions.stub.steps;

import io.grpc.Status;
import java.util.List;
import javax.annotation.Nonnull;
import org.grpcmock.GrpcMock;
import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.Delay;
import org.grpcmock.definitions.response.Response;
import org.grpcmock.definitions.response.ResponseAction;
import org.grpcmock.definitions.response.steps.ExceptionResponseActionBuilder;
import org.grpcmock.definitions.response.steps.ObjectResponseActionBuilder;
import org.grpcmock.definitions.response.steps.StreamResponseBuilder;

/**
 * @author Fadelis
 */
public interface NextStreamResponseBuilderStep<ReqT, RespT> extends
    BuilderStep,
    MethodStubBuilder {

  /**
   * <p>Defines a stream {@link Response} for subsequent request call that will execute multiple
   * {@link ResponseAction}.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last stream response defined will be returned for those requests.
   */
  NextStreamResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull StreamResponseBuilder<RespT> response);

  /**
   * <p>Defines single action stream {@link Response} for subsequent request call to this stub.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last stream response defined will be returned for those requests.
   */
  NextStreamResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ObjectResponseActionBuilder<RespT> response);

  /**
   * <p>Defines exception {@link Response} for subsequent request call to this stub.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last stream response defined will be returned for those requests.
   */
  NextStreamResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ExceptionResponseActionBuilder response);

  /**
   * <p>Defines a stream {@link Response} for subsequent request call that will return multiple
   * response objects.
   * <p>In order to configure a {@link Delay} for the actions see {@link GrpcMock#response} method.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last stream response defined will be returned for those requests.
   *
   * @param responses single response objects for the stream response. Will be returned in provided
   * list order.
   */
  default NextStreamResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull List<RespT> responses
  ) {
    return nextWillReturn(GrpcMock.stream(responses));
  }

  /**
   * <p>Defines a stream {@link Response} for subsequent request call that will return multiple
   * response objects.
   * <p>In order to configure a {@link Delay} for the actions see {@link GrpcMock#response} method.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last stream response defined will be returned for those requests.
   *
   * @param responses single response objects for the stream response. Will be returned in provided
   * array order.
   * @see GrpcMock#response
   */
  default NextStreamResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull RespT... responses
  ) {
    return nextWillReturn(GrpcMock.stream(responses));
  }

  /**
   * <p>Defines status exception {@link Response} for subsequent request call to this stub.
   * p>In order to configure a {@link Delay} for the response see {@link GrpcMock#statusException}
   * method.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last stream response defined will be returned for those requests.
   *
   * @see GrpcMock#statusException
   */
  default NextStreamResponseBuilderStep<ReqT, RespT> nextWillReturn(@Nonnull Status status) {
    return nextWillReturn(GrpcMock.statusException(status));
  }
}
