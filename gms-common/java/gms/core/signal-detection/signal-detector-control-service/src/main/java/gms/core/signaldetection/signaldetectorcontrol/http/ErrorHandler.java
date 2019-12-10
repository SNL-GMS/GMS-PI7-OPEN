package gms.core.signaldetection.signaldetectorcontrol.http;

import java.util.Objects;
import org.eclipse.jetty.http.HttpStatus;

/**
 * Error handler utilities creating {@link StandardResponse} for common error scenarios.
 */
public class ErrorHandler {
  /**
   * Creates a {@link StandardResponse} with a 404 status and json body listing the missing resource
   *
   * @param url String containing url to the missing resource, not null
   * @return StandardResponse, not null
   * @throws NullPointerException if url is null
   */
  public static StandardResponse handle404(String url) {
    Objects.requireNonNull(url, "ErrorHandler.handle404 requires non-null resource URL");

    String errorMessage = ("Error 404 - Not Found (" + url + ")");
    return StandardResponse.create(HttpStatus.NOT_FOUND_404, errorMessage, ContentType.TEXT_PLAIN);
  }

  /**
   * Creates a {@link StandardResponse} with a 500 status and json body indicating the 500 and
   * containing the errorMessage (when present)
   *
   * @param errorMessage optional error message string, not null
   * @return StandardResponse, not null
   * @throws NullPointerException if errorMessage is null
   */
  public static StandardResponse handle500(String errorMessage) {
    Objects.requireNonNull(errorMessage, "ErrorHandler.handle500 requires non-null errorMessage");

    StringBuilder messageBuilder = new StringBuilder();
    messageBuilder.append("Error 500 - Internal Server Error");

    if (!errorMessage.trim().isEmpty()) {
      messageBuilder.append(": ");
      messageBuilder.append(errorMessage.trim());
    }

    return StandardResponse.create(HttpStatus.INTERNAL_SERVER_ERROR_500, messageBuilder.toString(),
        ContentType.TEXT_PLAIN);
  }
}