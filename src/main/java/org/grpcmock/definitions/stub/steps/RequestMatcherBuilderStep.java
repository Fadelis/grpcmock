package org.grpcmock.definitions.stub.steps;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.definitions.BuilderStep;

public interface RequestMatcherBuilderStep<BUILDER extends BuilderStep, ReqT> extends BuilderStep {

  BUILDER withRequest(@Nonnull Predicate<ReqT> requestPredicate);

  default BUILDER withRequest(@Nonnull ReqT request) {
    Objects.requireNonNull(request);
    return withRequest(request::equals);
  }
}
