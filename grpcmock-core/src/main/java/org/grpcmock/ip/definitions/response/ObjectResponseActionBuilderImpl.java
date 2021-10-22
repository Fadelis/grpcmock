package org.grpcmock.ip.definitions.response;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.grpcmock.ip.definitions.response.steps.ObjectResponseActionBuilder;

/**
 * @author Fadelis
 */
public class ObjectResponseActionBuilderImpl<RespT> implements
    ObjectResponseActionBuilder<RespT> {

  private final RespT responseObject;
  private Delay delay;

  public ObjectResponseActionBuilderImpl(@Nonnull RespT responseObject) {
    Objects.requireNonNull(responseObject);
    this.responseObject = responseObject;
  }

  @Override
  public ObjectResponseActionBuilderImpl<RespT> withDelay(@Nonnull Delay delay) {
    this.delay = delay;
    return this;
  }

  @Override
  public ResponseAction<RespT> build() {
    return responseObserver -> {
      Optional.ofNullable(delay).ifPresent(Delay::delayAction);
      responseObserver.onNext(responseObject);
    };
  }
}
