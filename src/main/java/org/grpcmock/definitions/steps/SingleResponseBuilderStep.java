package org.grpcmock.definitions.steps;

import javax.annotation.Nonnull;
import org.grpcmock.definitions.Response;

public interface SingleResponseBuilderStep<ReqT, RespT> extends MappingStubBuilder {

  /**
   * Defines the single {@link Response} that will be returned for the request and complete it.
   */
  NextSingleResponseBuilderStep<ReqT, RespT> willReturn(@Nonnull Response<ReqT, RespT> response);
}
