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
public interface StreamResponseBuilderStep<BUILDER extends BuilderStep, ReqT, RespT> extends
    BuilderStep,
    SingleResponseBuilderStep<BUILDER, RespT> {

  /**
   * Defines a stream {@link Response} that will execute multiple {@link ResponseAction}.
   */
  BUILDER willReturn(@Nonnull StreamResponseBuilder<RespT> response);

  /**
   * <p>Defines a stream {@link Response}, which can respond with multiple {@link ResponseAction}.
   * <p>In order to configure a {@link Delay} for the actions see {@link GrpcMock#response} method.
   *
   * @param responses single response objects for the stream response. Will be returned in provided
   * array order.
   */
  default BUILDER willReturn(@Nonnull List<RespT> responses) {
    return willReturn(GrpcMock.stream(responses));
  }

  /**
   * <p>Defines a stream {@link Response}, which can respond with multiple {@link ResponseAction}.
   * <p>In order to configure a {@link Delay} for the actions see {@link GrpcMock#response} method.
   *
   * @param responses single response objects for the stream response. Will be returned in provided
   * array order.
   */
  default BUILDER willReturn(@Nonnull RespT... responses) {
    return willReturn(GrpcMock.stream(responses));
  }
}
