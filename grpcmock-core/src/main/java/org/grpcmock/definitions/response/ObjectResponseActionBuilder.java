package org.grpcmock.definitions.response;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.response.steps.ObjectResponseActionBuilderStep;

/**
 * @author Fadelis
 */
public class ObjectResponseActionBuilder<RespT> implements ObjectResponseActionBuilderStep<RespT> {

  private final RespT responseObject;
  private Delay delay;

  public ObjectResponseActionBuilder(@Nonnull RespT responseObject) {
    Objects.requireNonNull(responseObject);
    this.responseObject = responseObject;
  }

  @Override
  public ObjectResponseActionBuilder<RespT> withDelay(@Nonnull Delay delay) {
    this.delay = delay;
    return this;
  }

  @Override
  public ResponseAction<RespT> build() {
    return new ResponseActionImpl<>(responseObject, delay);
  }
}
