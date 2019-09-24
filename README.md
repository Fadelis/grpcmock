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
        .willReturn(response(reponse1)) // first invocation will return this response
        .nextWillReturn(response(reponse2))); // subsequent invocations will return this response
```