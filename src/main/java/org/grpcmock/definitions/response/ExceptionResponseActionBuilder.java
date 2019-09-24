package org.grpcmock.definitions.response;

import io.grpc.Status;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.response.steps.ExceptionResponseActionBuilderStep;

/**
 * @author Fadelis
 */
public class ExceptionResponseActionBuilder implements ExceptionResponseActionBuilderStep {

  private final Throwable exception;
  private Delay delay;

  public ExceptionResponseActionBuilder(@Nonnull Throwable exception) {
    Objects.requireNonNull(exception);
    this.exception = exception;
  }

  public ExceptionResponseActionBuilder(@Nonnull Status status) {
    Objects.requireNonNull(status);
    this.exception = status.asRuntimeException();
  }

  @Override
  public ExceptionResponseActionBuilderStep withDelay(@Nonnull Delay delay) {
    this.delay = delay;
    return this;
  }

  @Override
  public <RespT> ResponseAction<RespT> build() {
    return new ResponseActionImpl<>(exception, delay);
  }
}
