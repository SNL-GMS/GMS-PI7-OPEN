package gms.shared.frameworks.client;

import gms.shared.frameworks.common.ContentType;
import com.fasterxml.jackson.databind.JavaType;
import java.util.Objects;
import java.util.Optional;
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpRequest.BodyProcessor;
import jdk.incubator.http.HttpResponse;
import jdk.incubator.http.HttpResponse.BodyHandler;

/**
 * Implementation of the HTTP client abstraction using the built-in JDK HttpClient.
 */
public class ServiceClientJdkHttp implements ServiceClient {

  private final HttpClient httpClient;

  private ServiceClientJdkHttp(HttpClient client) {
    this.httpClient = Objects.requireNonNull(client);
  }

  /**
   * Create a client.
   *
   * @return a ServiceClientJdkHttp
   */
  public static ServiceClientJdkHttp create() {
    return new ServiceClientJdkHttp(HttpClient.newHttpClient());
  }

  /**
   * Create a client that uses the provided JDK HttpClient.
   *
   * @param client the JDK HttpClient to use for communications
   * @return a ServiceClientJdkHttp
   */
  public static ServiceClientJdkHttp create(HttpClient client) {
    return new ServiceClientJdkHttp(client);
  }

  /**
   * Send a request and gets a response
   *
   * @param request the request to send
   * @param <T> type param of the expected response
   * @return a ResponseType
   * @throws ConnectionFailed with wrapped exception when the connection fails (e.g. hostname not
   * found)
   * @throws InternalServerError with body of response from server if the response code is in [500,
   * 599].
   * @throws BadRequest with body of response from server if the response code is in [400, 499].
   * @throws IllegalArgumentException if the url in the request is invalid, the requestFormat or
   * responseFormat's in the request are unsupported, or request serialization fails.
   * @throws IllegalStateException if response deserialization fails
   */
  @Override
  public <T> T send(ServiceRequest request) {
    return sendImpl(request);
  }

  // this method is split out from send so it can have an extra type parameter
  // that is not part of the interface
  private <T, F> T sendImpl(ServiceRequest request) {
    Objects.requireNonNull(request, "Cannot send null request");
    final ResponseContentProtocol<F> responseProtocol
        = ContentProtocols.from(request.getResponseFormat());
    final HttpRequest httpRequest = createHttpRequest(request);
    final HttpResponse<F> httpResponse = throwIfErrorResponse(
        sendHttp(httpRequest, responseProtocol.bodyHandler()));
    return tryDeserialize(httpResponse.body(), responseProtocol, request.getResponseType());
  }

  private HttpRequest createHttpRequest(ServiceRequest request) {
    try {
      return HttpRequest.newBuilder().uri(request.getUrl().toURI())
          .timeout(request.getTimeout())
          .POST(bodyProcessor(request.getBody(), request.getRequestFormat()))
          .header("Content-Type", request.getRequestFormat().toString())
          .header("Accept", request.getResponseFormat().toString())
          .build();
    } catch (Exception ex) {
      throw new IllegalArgumentException("Could not prepare the request", ex);
    }
  }

  private static <F> BodyProcessor bodyProcessor(
      Object body, ContentType requestFormat) {
    final RequestContentProtocol<F> requestProtocol
        = ContentProtocols.from(requestFormat);
    return requestProtocol.bodyEncoder().apply(trySerialize(requestProtocol, body));
  }

  private static <F> F trySerialize(
      RequestContentProtocol<F> requestProtocol, Object data) {
    try {
      return requestProtocol.serialize(data);
    } catch (Exception ex) {
      throw new IllegalArgumentException("Could not serialize request body", ex);
    }
  }

  private <F> HttpResponse<F> sendHttp(HttpRequest request, BodyHandler<F> bodyHandler) {
    try {
      return this.httpClient.send(request, bodyHandler);
    } catch (Exception ex) {
      throw new ConnectionFailed("Could not connect to " + request.uri(), ex);
    }
  }

  private static <T, F> T tryDeserialize(
      F data,
      ResponseContentProtocol<F> responseProtocol,
      JavaType type) {
    try {
      return responseProtocol.deserialize(data, type);
    } catch (Exception ex) {
      throw new IllegalStateException("Could not deserialize response body", ex);
    }
  }

  private static <X> HttpResponse<X> throwIfErrorResponse(HttpResponse<X> response) {
    return throwIfInternalServerError(throwIfBadRequest(response));
  }

  private static <X> HttpResponse<X> throwIfBadRequest(
      HttpResponse<X> response) {
    if (isBadRequest(response.statusCode())) {
      throw new BadRequest(getErrorMessage(response));
    }
    return response;
  }

  private static boolean isBadRequest(int status) {
    return status >= 400 && status <= 499;
  }

  private static <X> HttpResponse<X> throwIfInternalServerError(HttpResponse<X> response) {
    if (isInternalServerError(response.statusCode())) {
      throw new InternalServerError(getErrorMessage(response));
    }
    return response;
  }

  private static boolean isInternalServerError(int status) {
    return status >= 500 && status <= 599;
  }

  private static String getErrorMessage(HttpResponse response) {
    return Optional.ofNullable(response.body()).map(Object::toString).orElse("");
  }
}
