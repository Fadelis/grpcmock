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

## Integrations

### Spring-Boot

gRPC Mock integrates with Spring-Boot via `grpcmock-spring-boot` module. You have to declare the `@AutoConfigureGrpcMock` for the test class to enable gRPC Mock.
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