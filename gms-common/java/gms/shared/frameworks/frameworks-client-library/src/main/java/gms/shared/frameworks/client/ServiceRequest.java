package gms.shared.frameworks.client;

import com.fasterxml.jackson.databind.JavaType;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.common.ContentType;
import java.net.URL;
import java.time.Duration;
import org.apache.commons.lang3.Validate;

/**
 * Class representing a service request.
 */
@AutoValue
public abstract class ServiceRequest {

  private static final ContentType defaultFormat = ContentType.defaultContentType();

  public abstract URL getUrl();

  public abstract Object getBody();

  public abstract Duration getTimeout();

  public abstract JavaType getResponseType();

  public abstract ContentType getRequestFormat();

  public abstract ContentType getResponseFormat();

  /**
   * Create a service request from all arguments
   * @param url the url of the request
   * @param body the body of the request
   * @param timeout the timeout of the request, not negative
   * @param responseType the expected type of the response from the server
   * @param requestFormat the format to send the request in
   * @param responseFormat the format to get the response in
   * @return a {@link ServiceRequest}
   * @throws IllegalArgumentException if timeout is negative
   */
  public static ServiceRequest from(
      URL url, Object body, Duration timeout,
      JavaType responseType, ContentType requestFormat, ContentType responseFormat) {
    return builder(url, body, timeout, responseType)
        .setRequestFormat(requestFormat)
        .setResponseFormat(responseFormat)
        .build();
  }

  /**
   * Create a service request builder
   * @param url the url of the request
   * @param body the body of the request
   * @param timeout the timeout of the request, not negative
   * @param responseType the expected type of the response from the server
   * @return a {@link ServiceRequest}
   * @throws IllegalArgumentException if timeout is negative
   */
  public static Builder builder(URL url, Object body,
      Duration timeout, JavaType responseType) {
    return new AutoValue_ServiceRequest.Builder()
        .setUrl(url)
        .setBody(body)
        .setTimeout(timeout)
        .setResponseType(responseType)
        .setRequestFormat(defaultFormat)
        .setResponseFormat(defaultFormat);
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setUrl(URL url);
    public abstract Builder setBody(Object body);
    public abstract Builder setResponseType(JavaType responseType);
    public abstract Builder setTimeout(Duration timeout);
    public abstract Builder setRequestFormat(ContentType format);
    public abstract Builder setResponseFormat(ContentType format);
    abstract ServiceRequest autoBuild();

    public ServiceRequest build() {
      final ServiceRequest req = autoBuild();
      Validate.isTrue(!req.getTimeout().isNegative(),
          "Timeout cannot be negative");
      return req;
    }
  }
}
