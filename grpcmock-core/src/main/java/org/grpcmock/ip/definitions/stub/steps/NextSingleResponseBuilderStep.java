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
public interface NextSingleResponseBuilderStep<BUILDER extends NextSingleResponseBuilderStep<BUILDER, ReqT, RespT>, ReqT, RespT> extends
    BuilderStep,
    MethodStubBuilder<ReqT, RespT> {

  /**
   * <p>Defines a single {@link Response} for subsequent request call to this stub.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last response defined will be returned for those requests.
   */
  BUILDER nextWillReturn(@Nonnull ObjectResponseActionBuilder<RespT> response);

  /**
   * <p>Defines a exception {@link Response} for subsequent request call to this stub.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last response defined will be returned for those requests.
   */
  BUILDER nextWillReturn(@Nonnull ExceptionResponseActionBuilder response);

  /**
   * <p>Defines a single {@link Response} for subsequent request call to this stub.
   * <p>In order to configure a {@link Delay} for the response see {@link GrpcMock#response}
   * method.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last response defined will be returned for those requests.
   */
  default BUILDER nextWillReturn(@Nonnull RespT response) {
    return nextWillReturn(GrpcMock.response(response));
  }

  /**
   * <p>Defines a exception {@link Response} for subsequent request call to this stub.
   * <p>In order to configure a {@link Delay} for the response see {@link GrpcMock#statusException}
   * method.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last response defined will be returned for those requests.
   */
  default BUILDER nextWillReturn(@Nonnull Status status) {
    return nextWillReturn(GrpcMock.statusException(status));
  }
}
