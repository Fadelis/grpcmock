package org.grpcmock.ip.examples.spring.controller;

import org.grpcmock.ip.examples.spring.service.DownstreamService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Fadelis
 */
@RestController
public class MessageController {

  private final DownstreamService downstreamService;

  public MessageController(DownstreamService downstreamService) {
    this.downstreamService = downstreamService;
  }

  @GetMapping("send/{message}")
  public String getDownstreamMessage(@PathVariable String message) {
    return downstreamService.getDownstreamMessage(message);
  }
}
