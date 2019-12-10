package gms.shared.frameworks.service;

import com.google.auto.value.AutoValue;
import gms.shared.frameworks.service.HttpStatus.Code;
import java.util.Optional;
import org.apache.commons.lang3.Validate;

/**
 * Wraps HTTP response code and body
 */
@AutoValue
public abstract class Response<T> {

  /**
   * Gets the status code of the response.
   *
   * @return the status code
   */
  public abstract HttpStatus.Code getHttpStatus();

  /**
   * Gets the body of the response.
   * This should be present when the error message is empty,
   * and this should be empty when the error message is present.
   *
   * @return the body
   */
  public abstract Optional<T> getBody();

  /**
   * Gets the error message of this response.
   * This should be empty when the body is present, and
   * this should be present when the body is empty.
   *
   * @return the error message
   */
  public abstract Optional<String> getErrorMessage();

  /**
   * Creates a 'success' response (200 OK) with the provided body and no error message.
   *
   * @param body, not null
   * @return a Response
   */
  public static <T> Response<T> success(T body) {
    Validate.notNull(body, "Body cannot be null on success Response");
    return new AutoValue_Response<>(
        Code.OK, Optional.of(body), Optional.empty());
  }

  /**
   * Creates a 'client error' response (400 BAD_REQUEST) with the
   * provided error message and no body.  This can be used by route handler
   * functions to conveniently return an error from a bad client request.
   * @param msg the error message
   * @return a Response
   */
  public static <T> Response<T> clientError(String msg) {
    return error(Code.BAD_REQUEST, msg);
  }

  /**
   * Creates a 'server error' response (500 INTERNAL_SERVER_ERROR) with the
   * provided error message and no body.  This is generally used
   * by this library when a route handler throws an exception.
   * @param msg the error message
   * @return a Response
   */
  public static <T> Response<T> serverError(String msg) {
    return error(Code.INTERNAL_SERVER_ERROR, msg);
  }

  /**
   * Creates a generic 'error' response with the
   * provided error message, no body, and the provided status code
   * @param statusCode the status code to set
   * @param msg the error message
   * @return a Response
   */
  public static <T> Response<T> error(HttpStatus.Code statusCode, String msg) {
    Validate.notNull(statusCode, "status code cannot be null on error Response");
    Validate.notBlank(msg, "Error message cannot be null or blank on error Response");
    final int sc = statusCode.getStatusCode();
    Validate.isTrue(sc >= 400 && sc <= 599,
        "For error, expected status code in range [400, 599] but was " + sc);
    return new AutoValue_Response<>(
        statusCode, Optional.empty(), Optional.of(msg));
  }
}
