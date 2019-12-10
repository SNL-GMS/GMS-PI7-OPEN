# An opinionated library for making HTTP service requests with some behavior conventions and defaults.

## High-level goals

- **Reduce the amount of HTTP knowledge required to make GMS service calls**

Relieve developers of the burden of knowing about HTTP headers and status codes.

- **Standardize and improve how GMS Java HTTP clients behave**

Ensure status codes are always reacted to by throwing exceptions on error codes.  Automatically handle content type negotiation and provide default for content types.  Make serialization of request body content and deserialization of response body content transparent and robust.

## Technical decisions and 'opinions':
- **Only allow supported HTTP verbs** 

For GMS, that's just `POST`, thus the verb is provided to the HTTP client or it's calls.

- **Automatically handle content type negotiation**

The `ServiceRequest` object has `ContentType` enums to determine what format data is sent in and what format it's received in from the server.  These have defaults (JSON) but can be overridden as well.  The library uses the `ContentType`s to set the appropriate HTTP request headers (`Content-Type` and `Accept`), and then serialize the request body and deserialize the response body accordingly.

- **Throw exceptions on HTTP connection failures and error codes**

This provides a standardized interface to clients while ensuring the client cannnot completely ignore the status code by reading the body as some object when the code was e.g. `500 INTERNAL SERVER ERROR`.

# Developer usage guide

## Defining a request with `ServiceRequest<T>`

`ServiceRequest` is an immutable data object that defines a HTTP request/response operation.  The type parameter indicates the type the response body is expected to be deserialized as. `ServiceRequest` has defaults for `ContentType requestformat` and `ContentType responseFormat` (both JSON) and all other arguments are required.  `ServiceRequest` also has a builder.

Example using static creation operation:
```java
ServiceRequest<Integer> req = ServiceRequest.from(
        new URL("http://localhost:8080/foo"), 
        "a body as a string", 
        Duration.ofMillis(100), 
        Integer.class,
        ContentType.MSGPACK, ContentType.JSON);
```
This creates a request to `http://localhost:8080/foo` that will encode `a body as a string` as msgpack, timeout of 100 milliseconds, read the server response a JSON integer.

Example using the builder:
```java
ServiceRequest<Integer> req = ServiceRequest.builder(
        new URL("http://localhost:8080/foo"), 
        "a body as a string", 
        Duration.ofMillis(100), Integer.class)
        .build();
```
This creates a request like before but with the `ContentType` arguments left as defaults.

## Sending a request using the `ServiceClient` interface

Once you have a `ServiceRequest<T>`, you can use an implementation of `ServiceClient` (such as `ServiceClientJdkHttp`) to send the request and get the response as a `T`.  Example:
```java
ServiceClient client = ServiceClientJdkHttp.create();
YourSpecialDto response = client.send(ServiceRequest.builder(
        URI.create("http://localhost:8080/foo").toURL(), 
        "a body as a string", 
        Duration.ofMillis(100), YourSpecialDto.class)
        .build());
```
Operations on `ServiceClient` throw unchecked exceptions on HTTP status codes other than `200 OK`:
  - Codes in [400, 499] throw `ServiceClient.BadRequest`
  - Codes in [500, 599] throw `ServiceClient.InternalServerError`

Connection failures throw `ServiceClient.ConnectionFailed`.  Failures to serialize request body or deserialize response body throw `IllegalStateException`.
