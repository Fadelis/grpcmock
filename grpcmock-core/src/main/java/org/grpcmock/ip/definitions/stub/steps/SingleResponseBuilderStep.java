package org.grpcmock.ip.definitions.stub.steps;

import io.grpc.Status;
import javax.annotation.Nonnull;
import org.grpcmock.ip.GrpcMock;
import org.grpcmock.ip.definitions.BuilderStep;
import org.grpcmock.ip.definitions.response.Delay;
import org.grpcmock.ip.definitions.response.Response;
import org.grpcmock.ip.definitions.response.steps.ExceptionResponseActionBuilder;
import org.grpcmock.ip.definitions.response.steps.ObjectResponseActionBuilder;

/**
 * @author Fadelis
 */
public interface SingleResponseBuilderStep<BUILDER extends BuilderStep, RespT> extends BuilderStep {

  /**
   * Defines a single {@link Response} that will be returned for the request and complete it.
   */
  BUILDER willReturn(@Nonnull ObjectResponseActionBuilder<RespT> response);

  /**
   * Defines a exception {@link Response} that will terminate the request.
   */
  BUILDER willReturn(@Nonnull ExceptionResponseActionBuilder response);

  /**
   * <p>Defines a single {@link Response} that will be returned for the request and complete it.
   * <p>In order to configure a {@link Delay} for the response see {@link GrpcMock#response}
   * method.
   */
  default BUILDER willReturn(@Nonnull RespT response) {
    return willReturn(GrpcMock.response(response));
  }

  /**
   * <p>Defines a exception {@link Response} that will terminate the request.
   * <p>In order to configure a {@link Delay} for the response see {@link GrpcMock#statusException}
   * method.
   */
  default BUILDER willReturn(@Nonnull Status status) {
    return willReturn(GrpcMock.statusException(status));
  }
}
