package org.grpcmock.examples.junit5.service;

import org.grpcmock.examples.v1.DownstreamServiceGrpc.DownstreamServiceBlockingStub;
import org.grpcmock.examples.v1.GetDownstreamMessageRequest;

/**
 * @author Fadelis
 */
public class DownstreamService {

  private final DownstreamServiceBlockingStub blockingStub;

  public DownstreamService(DownstreamServiceBlockingStub blockingStub) {
    this.blockingStub = blockingStub;
  }

  public String getDownstreamMessage(String requestMessage) {
    GetDownstreamMessageRequest request = GetDownstreamMessageRequest.newBuilder()
        .setMessage(requestMessage)
        .build();
    return blockingStub.getDownstreamMessage(request).getMessage();
  }
}
