package org.grpcmock;

import io.grpc.inprocess.InProcessServerBuilder;

import javax.annotation.Nonnull;

/**
 * @author akoylu
 */
public class InProcessGrpcMockBuilder extends GrpcMockBuilder {

  private String name;

  public String getName() {
    return name;
  }

  InProcessGrpcMockBuilder(@Nonnull InProcessServerBuilder serverBuilder) {
    super(serverBuilder);
  }

  public InProcessGrpcMockBuilder(@Nonnull String name) {
    this(InProcessServerBuilder.forName(name));

    this.name = name;
  }

  public InProcessGrpcMockBuilder() {
    this(InProcessServerBuilder.generateName());
  }
}
