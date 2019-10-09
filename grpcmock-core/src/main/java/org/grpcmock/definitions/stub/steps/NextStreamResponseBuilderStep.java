package org.grpcmock.definitions.stub.steps;

import java.util.List;
import javax.annotation.Nonnull;
import org.grpcmock.GrpcMock;
import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.Delay;
import org.grpcmock.definitions.response.Response;
import org.grpcmock.definitions.response.ResponseAction;
import org.grpcmock.definitions.response.steps.StreamResponseBuilder;

/**
 * @author Fadelis
 */
public interface NextStreamResponseBuilderStep<BUILDER extends NextStreamResponseBuilderStep<BUILDER, ReqT, RespT>, ReqT, RespT> extends
    BuilderStep,
    MethodStubBuilder<ReqT, RespT>,
    NextSingleResponseBuilderStep<BUILDER, ReqT, RespT> {

  /**
   * <p>Defines a stream {@link Response} for subsequent request call that will execute multiple
   * {@link ResponseAction}.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last stream response defined will be returned for those requests.
   */
  BUILDER nextWillReturn(@Nonnull StreamResponseBuilder<RespT> response);

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
  default BUILDER nextWillReturn(@Nonnull List<RespT> responses) {
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
  default BUILDER nextWillReturn(@Nonnull RespT... responses) {
    return nextWillReturn(GrpcMock.stream(responses));
  }
}
