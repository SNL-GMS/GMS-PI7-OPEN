package gms.shared.utilities.service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implements the Request interface using the Spark HTTP library.
 */
class SparkRequest implements Request {

  private final spark.Request request;

  SparkRequest(spark.Request request) {
    this.request = Objects.requireNonNull(request,
        "Cannot create SparkRequest with null spark.Request");
  }

  @Override
  public Optional<String> getPathParam(String name) {
    return Optional.ofNullable(this.request.params(name));
  }

  @Override
  public String getBody() {
    return this.request.body();
  }

  @Override
  public Optional<String> getHeader(String name) {
    return Optional.ofNullable(this.request.headers(name));
  }

  @Override
  public Map<String, String> getHeaders() {
    Set<String> headerNames = this.request.headers();
    return headerNames.stream()
        .collect(Collectors.toMap(
            Function.identity(), // key is the header name
            h -> this.request.headers(h))); // value is the header value
  }

  @Override
  public byte[] getRawBody() {
    return this.request.bodyAsBytes();
  }
}
