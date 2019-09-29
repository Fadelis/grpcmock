package org.grpcmock.definitions.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.response.steps.ExceptionResponseActionBuilder;
import org.grpcmock.definitions.response.steps.ExceptionStreamResponseBuildersStep;
import org.grpcmock.definitions.response.steps.ObjectResponseActionBuilder;
import org.grpcmock.definitions.response.steps.ObjectStreamResponseBuilderStep;

/**
 * @author Fadelis
 */
public class StreamResponseBuilderImpl<RespT> implements
    ObjectStreamResponseBuilderStep<RespT>,
    ExceptionStreamResponseBuildersStep<RespT> {

  private final List<ResponseAction<RespT>> responseActions = new ArrayList<>();

  public StreamResponseBuilderImpl(@Nonnull ResponseAction<RespT> responseAction) {
    Objects.requireNonNull(responseAction);
    this.responseActions.add(responseAction);
  }

  @Override
  public ObjectStreamResponseBuilderStep<RespT> and(
      @Nonnull ObjectResponseActionBuilder<RespT> responseAction) {
    Objects.requireNonNull(responseAction);
    this.responseActions.add(responseAction.build());
    return this;
  }

  @Override
  public ExceptionStreamResponseBuildersStep<RespT> and(
      @Nonnull ExceptionResponseActionBuilder responseAction) {
    Objects.requireNonNull(responseAction);
    this.responseActions.add(responseAction.build());
    return this;
  }

  @Override
  public <ReqT> Response<ReqT, RespT> build() {
    return new ResponseImpl<>(responseActions);
  }
}
