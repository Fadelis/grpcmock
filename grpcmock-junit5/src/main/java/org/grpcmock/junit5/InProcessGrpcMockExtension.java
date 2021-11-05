package org.grpcmock.junit5;

import org.grpcmock.GrpcMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In-process gRPC Mock extension for JUnit5. All stub mappings are reset after each test method. After all tests in the test
 * class are done the server will be shutdown.
 *
 * @author Fadelis
 */
public class InProcessGrpcMockExtension extends GrpcMockExtension {

  private static final Logger log = LoggerFactory.getLogger(InProcessGrpcMockExtension.class);

  public InProcessGrpcMockExtension() {
    super(GrpcMock.inProcessGrpcMock().build());
  }

  @Override
  protected void init() {
    this.server.start();
    GrpcMock.configureFor(this.server);
    log.debug("Started in-process gRPC Mock server with name: {}", getInProcessName());
  }

  public String getInProcessName() {
    return this.server.getInProcessName();
  }
}
