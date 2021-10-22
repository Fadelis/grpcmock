package org.grpcmock.ip.definitions.matcher;

import io.grpc.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.grpcmock.ip.definitions.matcher.steps.StatusMatcherBuilderStep;

/**
 * @author Fadelis
 */
public class StatusMatcherBuilderImpl implements StatusMatcherBuilderStep<StatusMatcherBuilderImpl> {

  private final List<Predicate<Status>> statusPredicates = new ArrayList<>();

  StatusMatcherBuilderImpl() {
  }

  @Override
  public StatusMatcherBuilderImpl withStatus(@Nonnull Predicate<Status> predicate) {
    Objects.requireNonNull(predicate);
    statusPredicates.add(predicate);
    return null;
  }

  public StatusMatcher build() {
    return status -> statusPredicates.stream().allMatch(predicate -> predicate.test(status));
  }
}
