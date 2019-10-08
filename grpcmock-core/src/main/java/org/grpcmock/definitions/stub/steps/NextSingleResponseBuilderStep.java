package org.grpcmock.definitions.stub.steps;

import io.grpc.Status;
import javax.annotation.Nonnull;
import org.grpcmock.GrpcMock;
import org.grpcmock.definitions.BuilderStep;
import org.grpcmock.definitions.response.Delay;
import org.grpcmock.definitions.response.Response;
import org.grpcmock.definitions.response.steps.ExceptionResponseActionBuilder;
import org.grpcmock.definitions.response.steps.ObjectResponseActionBuilder;

/**
 * @author Fadelis
 */
public interface NextSingleResponseBuilderStep<ReqT, RespT> extends
    BuilderStep,
    MethodStubBuilder {

  /**
   * <p>Defines a single {@link Response} for subsequent request call to this stub.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last response defined will be returned for those requests.
   */
  NextSingleResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ObjectResponseActionBuilder<RespT> response);

  /**
   * <p>Defines a exception {@link Response} for subsequent request call to this stub.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last response defined will be returned for those requests.
   */
  NextSingleResponseBuilderStep<ReqT, RespT> nextWillReturn(
      @Nonnull ExceptionResponseActionBuilder response);

  /**
   * <p>Defines a single {@link Response} for subsequent request call to this stub.
   * <p>In order to configure a {@link Delay} for the response see {@link GrpcMock#response}
   * method.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last response defined will be returned for those requests.
   */
  default NextSingleResponseBuilderStep<ReqT, RespT> nextWillReturn(@Nonnull RespT response) {
    return nextWillReturn(GrpcMock.response(response));
  }

  /**
   * <p>Defines a exception {@link Response} for subsequent request call to this stub.
   * <p>In order to configure a {@link Delay} for the response see {@link GrpcMock#statusException}
   * method.
   * <p>If there are more requests coming in to this stub than responses defined,
   * the last response defined will be returned for those requests.
   */
  default NextSingleResponseBuilderStep<ReqT, RespT> nextWillReturn(@Nonnull Status status) {
    return nextWillReturn(GrpcMock.statusException(status));
  }
}
