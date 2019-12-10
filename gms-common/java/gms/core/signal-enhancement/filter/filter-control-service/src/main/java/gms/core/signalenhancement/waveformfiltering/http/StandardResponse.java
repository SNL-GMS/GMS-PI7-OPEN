package gms.core.signalenhancement.waveformfiltering.http;

import java.util.Objects;
import org.eclipse.jetty.http.HttpStatus;

/**
 * Wraps HTTP response code and body
 */
public class StandardResponse {

  private final int httpStatus;
  private final Object responseBody;
  private final ContentType contentType;

  private StandardResponse(int httpStatus, Object responseBody, ContentType contentType) {
    this.httpStatus = httpStatus;
    this.responseBody = responseBody;
    this.contentType = contentType;
  }

  /**
   * Obtains an instance of {@link StandardResponse} with the provided HTTP Status code and response
   * body.
   *
   * @param httpStatus HTTP status code as an integer, must be a valid code number
   * @param responseBody response body, not null
   * @param contentType response body content type, not null
   * @return a StandardResponse, not null
   * @throws NullPointerException if responseBody is null
   * @throws IllegalArgumentException if httpStatus is not a valud HTTP status code
   * @see <a href="http://www.iana.org/assignments/http-status-codes/">IANA HTTP Status Code
   * Registry</a>
   */
  public static StandardResponse create(int httpStatus, Object responseBody,
      ContentType contentType) {
    Objects.requireNonNull(responseBody, "StandardResponse requires non-null responseBody");
    Objects.requireNonNull(contentType, "StandardResponse requires non-null contentType");

    if (!isValidHttpStatusCode(httpStatus)) {
      throw new IllegalArgumentException("StandardResponse requires a valid httpStatus");
    }

    return new StandardResponse(httpStatus, responseBody, contentType);
  }

  /**
   * Determines if the statusCode is a valid HTTP status code per the IANA registry
   *
   * @param statusCode integer status code
   * @return whether statusCode is a valid HTTP status code
   * @see <a href="http://www.iana.org/assignments/http-status-codes/">IANA HTTP Status Code
   * Registry</a>
   */
  private static boolean isValidHttpStatusCode(int statusCode) {
    boolean isValid = true;

    try {
      if (null == HttpStatus.getCode(statusCode)) {
        isValid = false;
      }
    } catch (Exception e) {
      isValid = false;
    }

    return isValid;
  }

  /**
   * Obtains an HTTP Status code, guaranteed valid per the IANA registry
   *
   * @return integer status code
   * @see <a href="http://www.iana.org/assignments/http-status-codes/">IANA HTTP Status Code
   * Registry</a>
   */
  public int getHttpStatus() {
    return httpStatus;
  }

  /**
   * Obtains the response body content
   *
   * @return response body contents, not null
   */
  public Object getResponseBody() {
    return responseBody;
  }

  /**
   * Obtains the response body {@link ContentType}
   *
   * @return {@link ContentType}, not null
   */
  public ContentType getContentType() {
    return contentType;
  }
}
