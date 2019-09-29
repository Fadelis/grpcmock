# gRPC Mock
A testing utility tool to easily mock endpoints of gRPC services. 
The tool follows a similar DSL type of structure to HTTP mocking service [WireMock](https://github.com/tomakehurst/wiremock).

## Features

 - gRPC method stub configurable through a fluent Java API
 - Headers and request body matchers to determine the correct stub
 - Configurable delay for individual responses
 - [SOON] Verifiable method invocations for specific method
 - Supported gRPC method types:
    - Unary methods
    - Server streaming methods
    - [SOON] Client streaming methods
    - [SOON] Bidi stream methods with single response at the end (the same as Client streaming methods)
    
## Quick usage

### Unary methods

```
stubFor(service(HealthGrpc.SERVICE_NAME)
        .forMethod(HealthGrpc.getCheckMethod())
        .withHeader("header-1", "value-1")
        .withHeader("header-2", value -> value.startsWith("value"))
        .withRequest(expectedRequest)
        .willReturn(response(reponse1)
            .withFixedDelay(200)) // first invocation will return this response after 200 ms
        .nextWillReturn(response(reponse2))); // subsequent invocations will return this response
```

### Server streaming methods

```
stubFor(service(SimpleServiceGrpc.SERVICE_NAME)
        .forServerStreamingMethod(SimpleServiceGrpc.getServerStreamingRpcMethod())
        .withHeader("header-1", "value-1")
        .withRequest(req -> req.getRequestMessage().endsWith("1"))
        .willReturn(stream(response(responses1).withFixedDelay(200))
            .and(response(responses2).withFixedDelay(100))
            .and(response(responses3).withFixedDelay(200)))
        .nextWillReturn(statusException(Status.NOT_FOUND)));
```

## Integrations

### Spring-Boot

gRPC Mock integrates with Spring-Boot via `grpcmock-spring-boot` module. 

```
<dependency>
  <groupId>org.grpcmock</groupId>
  <artifactId>grpcmock-spring-boot</artifactId>
  <version>1.0.0</version>
</dependency>
```

You have to declare the `@AutoConfigureGrpcMock` for the test class to enable gRPC Mock.

Spring-Boot test class should look something like this:
```
@SpringJUnitConfig
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.NONE)
@AutoConfigureGrpcMock(port = 0)
class TestClass {
    ...
}
```

If the gRPC Mock port is set to 0, then a random port will be selected for the server. It is the recommended approach to improve test run times. Once a random port is selected it can be access via `${grpcmock.server.port}` property and used in gRPC `Channel` creation.

Mapping stubs will be cleared after each test run and after each test class run. If test class was run with a fixed port, the test context will be marked as dirty to reinitialise a new one.

### JUnit5

gRPC Mock integrates with JUnit5 via `grpcmock-junit5` module.

```
<dependency>
  <groupId>org.grpcmock</groupId>
  <artifactId>grpcmock-junit5</artifactId>
  <version>1.0.0</version>
</dependency>
```

You can integrate gRPC Mock with default configuration for a JUnit5 test via `@ExtendWith` annotation:

```
@ExtendWith(GrpcMockExtension.class)
class TestClass {

}
```

Or alternatively, you can configure gRPC Mock programmatically using `@RegisterExtension` annotation:

```
class TestClass {

  @RegisterExtension
  static GrpcMockExtension grpcMockExtension = GrpcMockExtension.builder()
      .withPort(0)
      .withInterceptor(new MyServerInterceptor())
      .build();

}
```

In both variants the port for the gRPC Mock server can be retrieved via `GrpcMock.getGlobalPort()`. 
Mapping stubs will be cleared after each test run and 
after all tests in the test class are done the server will be shutdown.