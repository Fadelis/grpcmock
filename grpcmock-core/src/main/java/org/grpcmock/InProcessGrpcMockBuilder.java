package org.grpcmock;

import io.grpc.inprocess.InProcessServerBuilder;
import javax.annotation.Nonnull;

/**
 * @author akoylu
 */
public class InProcessGrpcMockBuilder extends GrpcMockBuilder {

  public InProcessGrpcMockBuilder(@Nonnull String name) {
    super(InProcessServerBuilder.forName(name));
  }
}
