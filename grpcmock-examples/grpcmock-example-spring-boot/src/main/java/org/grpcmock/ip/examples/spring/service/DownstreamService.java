package org.grpcmock.ip.examples.spring.service;

import org.grpcmock.ip.examples.v1.DownstreamServiceGrpc.DownstreamServiceBlockingStub;
import org.grpcmock.ip.examples.v1.GetDownstreamMessageRequest;
import org.springframework.stereotype.Service;

/**
 * @author Fadelis
 */
@Service
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
