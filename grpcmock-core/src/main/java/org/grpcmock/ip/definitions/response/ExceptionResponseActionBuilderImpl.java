package org.grpcmock.ip.definitions.response;

import io.grpc.Status;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.grpcmock.ip.definitions.response.steps.ExceptionResponseActionBuilder;
import org.grpcmock.ip.exception.GrpcMockValidationException;

/**
 * @author Fadelis
 */
public class ExceptionResponseActionBuilderImpl implements ExceptionResponseActionBuilder {

  private final Throwable exception;
  private Delay delay;

  public ExceptionResponseActionBuilderImpl(@Nonnull Throwable exception) {
    Objects.requireNonNull(exception);
    this.exception = exception;
  }

  public ExceptionResponseActionBuilderImpl(@Nonnull Status status) {
    Objects.requireNonNull(status);
    if (status.isOk()) {
      throw new GrpcMockValidationException("OK is not a valid exception status");
    }
    this.exception = status.asRuntimeException();
  }

  @Override
  public ExceptionResponseActionBuilder withDelay(@Nonnull Delay delay) {
    this.delay = delay;
    return this;
  }

  @Override
  public <RespT> TerminatingResponseAction<RespT> build() {
    return responseObserver -> {
      Optional.ofNullable(delay).ifPresent(Delay::delayAction);
      responseObserver.onError(exception);
    };
  }
}
