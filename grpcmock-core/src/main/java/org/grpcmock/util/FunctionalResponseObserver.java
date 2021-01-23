package org.grpcmock.util;

import io.grpc.stub.StreamObserver;
import java.util.Objects;
import java.util.function.Consumer;


public class FunctionalResponseObserver<V> implements StreamObserver<V> {

  private final Consumer<V> onNext;
  private final Consumer<Throwable> onError;
  private final Runnable onCompleted;

  FunctionalResponseObserver(
      Consumer<V> onNext,
      Consumer<Throwable> onError,
      Runnable onCompleted
  ) {
    this.onNext = onNext;
    this.onError = onError;
    this.onCompleted = onCompleted;
  }

  public static <V> Builder<V> builder() {
    return new Builder<>();
  }

  @Override
  public void onNext(V value) {
    onNext.accept(value);
  }

  @Override
  public void onError(Throwable throwable) {
    onError.accept(throwable);
  }

  @Override
  public void onCompleted() {
    onCompleted.run();
  }

  public static class Builder<V> {

    private Consumer<V> onNext = value -> {
    };
    private Consumer<Throwable> onError = throwable -> {
    };
    private Runnable onCompleted = () -> {
    };

    public Builder<V> onNext(Consumer<V> onNext) {
      Objects.requireNonNull(onNext);
      this.onNext = onNext;
      return this;
    }

    public Builder<V> onError(Consumer<Throwable> onError) {
      Objects.requireNonNull(onError);
      this.onError = onError;
      return this;
    }

    public Builder<V> onCompleted(Runnable onCompleted) {
      Objects.requireNonNull(onCompleted);
      this.onCompleted = onCompleted;
      return this;
    }

    public FunctionalResponseObserver<V> build() {
      return new FunctionalResponseObserver<>(onNext, onError, onCompleted);
    }
  }
}
