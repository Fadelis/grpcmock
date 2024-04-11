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
        .withStatusOk()
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
  <version>0.12.0</version>
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
Once a random port is selected it can be accessed via `${grpcmock.server.port}` property and used in gRPC `Channel` creation.

It's also possible to use InProcess server with Spring-Boot

```java
@SpringJUnitConfig
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.NONE)
@AutoConfigureGrpcMock(useInProcessServer = true)
class TestClass {

   @Value("${grpcmock.server.name}")
   private String inProcessName;

   private ManagedChannel channel;

   @BeforeEach
   void setupChannel() {
      channel = InProcessChannelBuilder.forName(inProcessName).build();
   }

   @AfterEach
   void shutdownChannel() {
      Optional.ofNullable(channel).ifPresent(ManagedChannel::shutdownNow);
   }
}
```

If you don't set `name()` in `@AutoConfigureGrpcMock` then it will be generated randomly.
Beware that it's not possible to create two InProcess servers at once so in case of fixed names
you will have to configure a unique name for each spring context. If a random name is used
it can be accessed via `${grpcmock.server.name}` property and used in gRPC `Channel` creation.

To remove logging of incoming requests, logging level for GrpcMock should be changed in `application-test.yml`:

```yaml
logging.level.org.grpcmock.GrpcMock: WARN
```

### JUnit5

gRPC Mock integrates with JUnit5 via `grpcmock-junit5` module.

```xml
<dependency>
  <groupId>org.grpcmock</groupId>
  <artifactId>grpcmock-junit5</artifactId>
  <version>0.12.0</version>
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

For quicker unit test execution you can integrate gRPC Mock using an in-process server via `InProcessGrpcMockExtension` extension:

```java
@ExtendWith(InProcessGrpcMockExtension.class)
class TestClass {

  private ManagedChannel channel;

  @BeforeEach
  void setupChannel() {
    channel = InProcessChannelBuilder.forName(GrpcMock.getGlobalInProcessName())
        .usePlaintext()
        .build();
  }

  @AfterEach
  void shutdownChannel() {
    Optional.ofNullable(channel).ifPresent(ManagedChannel::shutdownNow);
  }
}
```

Alternatively, you can configure gRPC Mock programmatically using `@RegisterExtension` annotation:

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

In both variants the port for the gRPC Mock server can be retrieved via `GrpcMock.getGlobalPort()`. Mapping stubs will be cleared
after each test run and after all tests in the test class are done the server will be shutdown.

To remove logging of incoming requests, logging level for GrpcMock should be changed depending on the logging backend used. E.g.
when using `slf4j-simple` backend a file `simplelogger.properties` needs to be created in test `resouces` with content:

```yaml
org.slf4j.simpleLogger.log.org.grpcmock.GrpcMock=warn
```

### Working in multithreaded test setup

When working in multithreaded test setup, where the test class is initiated on one thread and test methods are called on a
different one, you will encounter `UNIMPLEMENTED` errors when trying to use the static configuration methods. GrpcMock uses a 
`ThreadLocal` instance of `GrpcMock` when using all the static methods, so it will be a different instance on different threads.

To work with `GrpcMock` in such cases you'll need to have access to the `grpcMock` instance object in your test class.

In Spring-Boot setup:
```java
@SpringJUnitConfig
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.NONE)
@AutoConfigureGrpcMock
class TestClass {
  
  @Autowire
  private GrpcMock grpcMock;
}
```

In other setups:
```java
class TestClass {
  
  private GrpcMock grpcMock = GrpcMock.grpcMock().build().start();
}
```

Then when configuring your stubs you can either call the non-static methods directly on the instance in your test methods:
```java
@Test
void should_test_something() {
  grpcMock.register(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response));
  
  ... test code
        
  grpcMock.verifyThat(calledMethod(SimpleServiceGrpc.getUnaryRpcMethod()).build(), CountMatcher.once());
}
```

Alternatively you could call `GrpcMock.configureFor` method with the grpcMock instance in the test class at the beginning 
of each test:
```java
@Test
void should_test_something() {
  configureFor(grpMock);
  stubFor(unaryMethod(SimpleServiceGrpc.getUnaryRpcMethod())
        .willReturn(response));
  
  ... test code
        
  verifyThat(calledMethod(SimpleServiceGrpc.getUnaryRpcMethod()));
}
```
Or depending whether `BeforeEach` method is called on the same thread as the test method that could be done in `BeforeEach` method:
```java
@BeforeEach
void setup() {
  configureFor(grpcMock);
}
```
