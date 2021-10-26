package org.grpcmock;

import io.grpc.inprocess.InProcessServerBuilder;

import javax.annotation.Nonnull;

/**
 * @author akoylu
 */
public class InProcessGrpcMockBuilder extends GrpcMockBuilder {

  InProcessGrpcMockBuilder(@Nonnull InProcessServerBuilder serverBuilder) {
    super(serverBuilder);
  }

  public InProcessGrpcMockBuilder(String name) {
    this(InProcessServerBuilder.forName(name));
  }
}
