# gRPC Mock ![Build pipeline](https://github.com/Fadelis/grpcmock/workflows/Build%20pipeline/badge.svg)

A gRPC Java testing tool to easily mock endpoints of gRPC services for IT or Unit testing.
The tool follows a similar DSL type of structure to HTTP mocking service [WireMock](https://github.com/tomakehurst/wiremock).

## Features

 - gRPC method stubbing configurable through a fluent Java API
 - Headers and request body matchers to determine the correct stub
 - Configurable delay for individual responses
 - Verifiable method invocations for specific method
 - Supported gRPC method types:
    - Unary methods
    - Server streaming methods
    - Client streaming methods
    - Bidi stream methods

## Quick usage

### Unary methods

```java
stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
    .willReturn(response1));

stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
    .withHeader("header-1", "value-1")
    .withHeader("header-2", value -> value.startsWith("value"))
    .withRequest(expectedRequest)
    .willReturn(response(response1)
        .withFixedDelay(200)) // first invocation will return this response after 200 ms
    .nextWillReturn(response(response2))); // subsequent invocations will return this response
```

See more [examples](grpcmock-core/src/test/java/org/grpcmock/GrpcMockUnaryMethodTest.java)

### Server streaming methods

```java
stubFor(serverStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
    .willReturn(responses1, responses2, responses3)); // return one by one with no delay

stubFor(serverStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
    .withHeader("header-1", "value-1")
    .withRequest(req -> req.getRequestMessage().endsWith("1"))
    .willReturn(stream(response(responses1).withFixedDelay(200))
        .and(response(responses2).withFixedDelay(100))
        .and(response(responses3).withFixedDelay(200)))
    .nextWillReturn(statusException(Status.NOT_FOUND))); // subsequent invocations will return status exception
```

See more [examples](grpcmock-core/src/test/java/org/grpcmock/GrpcMockServerStreamingMethodTest.java)

### Client streaming methods

Stubs for client streaming method calls are selected on receiving first stream request message.

```java
stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
    .willReturn(responses1)); // return a response on completed client streaming requests

stubFor(clientStreamingMethod(SimpleServiceGrpc.getClientStreamingRpcMethod())
    .withHeader("header-1", "value-1")
    .withFirstRequest(req -> req.getRequestMessage().endsWith("1"))
    .willReturn(response(responses1).withFixedDelay(200))
    .nextWillReturn(statusException(Status.NOT_FOUND))); // subsequent invocations will return status exception
```

See more [examples](grpcmock-core/src/test/java/org/grpcmock/GrpcMockClientStreamingMethodTest.java)

### Bidi streaming methods

Stubs for bidi streaming method calls are selected on receiving first stream request message.

```java
stubFor(bidiStreamingMethod(SimpleServiceGrpc.getBidiStreamingRpcMethod())
    .withHeader("header-1", "value-1")
    .withFirstRequest(req -> req.getRequestMessage().endsWith("1"))
    .willProxyTo(responseObserver -> new StreamObserver<SimpleRequest>() {
        @Override
        public void onNext(SimpleRequest request) {
          SimpleResponse response = SimpleResponse.newBuilder()
            .setResponseMessage(request.getRequestMessage())
            .build();
          responseObserver.onNext(response);
        }
        
        @Override
        public void onError(Throwable error) {
          // handle error
        }
        
        @Override
        public void onCompleted() {
          responseObserver.onCompleted();
        }
    }));
```

See more [examples](grpcmock-core/src/test/java/org/grpcmock/GrpcMockBidiStreamingMethodTest.java)

### Verifying invocation count

```java
verifyThat(
    calledMethod(getUnaryRpcMethod())
        .withHeader("header-1", "value-1")
        .withRequest(request),
    times(3));

verifyThat(getUnaryRpcMethod(), never());

verifyThat(
    calledMethod(getClientStreamingRpcMethod())
        .withNumberOfRequests(2)
        .withFirstRequest(request)
        .withRequestAtIndex(1, request2));
```

See more [examples](grpcmock-core/src/test/java/org/grpcmock/GrpcMockVerifyTest.java)

## Integrations

See example [projects](grpcmock-examples)

### Spring-Boot

gRPC Mock integrates with Spring-Boot via `grpcmock-spring-boot` module.

```xml
<dependency>
  <groupId>org.grpcmock</groupId>
  <artifactId>grpcmock-spring-boot</artifactId>
  <version>0.5.0</version>
</dependency>
```

You have to declare the `@AutoConfigureGrpcMock` for the test class to enable gRPC Mock:

```java
@SpringJUnitConfig
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.NONE)
@AutoConfigureGrpcMock
class TestClass {

  @Value("${grpcmock.server.port}")
  private int grpcMockPort;

  private ManagedChannel channel;

  @BeforeEach
  void setupChannel() {
    channel = ManagedChannelBuilder.forAddress("localhost", grpcMockPort)
        .usePlaintext()
        .build();
  }
  
  @AfterEach
  void shutdownChannel() {
    Optional.ofNullable(channel).ifPresent(ManagedChannel::shutdownNow);
  }
}
```

If the gRPC Mock port is set to 0, then a random port will be selected for the server.
It is the recommended approach to improve test run times.
Once a random port is selected it can be access via `${grpcmock.server.port}` property and used in gRPC `Channel` creation.

Mapping stubs will be cleared after each test run and after each test class run.
If test class was run with a fixed port, the test context will be marked as dirty to reinitialise a new one.

### JUnit5

gRPC Mock integrates with JUnit5 via `grpcmock-junit5` module.

```xml
<dependency>
  <groupId>org.grpcmock</groupId>
  <artifactId>grpcmock-junit5</artifactId>
  <version>0.5.0</version>
</dependency>
```

You can integrate gRPC Mock with default configuration for a JUnit5 test via `@ExtendWith` annotation:

```java
@ExtendWith(GrpcMockExtension.class)
class TestClass {

  private ManagedChannel channel;

  @BeforeEach
  void setupChannel() {
    channel = ManagedChannelBuilder.forAddress("localhost", GrpcMock.getGlobalPort())
        .usePlaintext()
        .build();
  }
  
  @AfterEach
  void shutdownChannel() {
    Optional.ofNullable(channel).ifPresent(ManagedChannel::shutdownNow);
  }
}
```

Or alternatively, you can configure gRPC Mock programmatically using `@RegisterExtension` annotation:

```java
class TestClass {

  @RegisterExtension
  static GrpcMockExtension grpcMockExtension = GrpcMockExtension.builder()
      .withPort(0)
      .withInterceptor(new MyServerInterceptor())
      .build();
  private ManagedChannel channel;

  @BeforeEach
  void setupChannel() {
    channel = ManagedChannelBuilder.forAddress("localhost", GrpcMock.getGlobalPort())
        .usePlaintext()
        .build();
  }

  @AfterEach
  void shutdownChannel() {
    Optional.ofNullable(channel).ifPresent(ManagedChannel::shutdownNow);
  }
}
```

In both variants the port for the gRPC Mock server can be retrieved via `GrpcMock.getGlobalPort()`.
Mapping stubs will be cleared after each test run and
after all tests in the test class are done the server will be shutdown.