package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.StorageUnavailableException;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

/**
 * Exception handlers for the service.  Exceptions get routed to these handlers to return
 * appropriate HTTP responses.
 */
public final class ExceptionHandlers {

  private static Logger logger = LoggerFactory.getLogger(ExceptionHandlers.class);

  // Prevent class from being instantiated, since it is a "static" class.
  private ExceptionHandlers() {
  }

  /**
   * Catch-all exception handler.
   *
   * @param e the exception that was caught.
   * @param request the request that caused the exception
   * @param response the response that was being built when the exception was thrown
   */
  public static void ExceptionHandler(Exception e, Request request, Response response) {
    logger.error("Exception caught by exception handler: ", e);
    // check for client error
    if (containsAnyCause(e,
        IllegalArgumentException.class, NullPointerException.class,
        DateTimeParseException.class, JsonProcessingException.class)) {
      response.status(HttpStatus.BAD_REQUEST_400);
    }
    else if (containsCause(e, StorageUnavailableException.class)) {
      response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
    }
    else {
      response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }
    response.type("application/text");
    response.body(e.getMessage() == null ? "" : e.getMessage());
  }

  private static boolean containsCause(Exception e, Class<?> clazz) {
    return ExceptionUtils.indexOfThrowable(e, clazz) >= 0;
  }

  private static boolean containsAnyCause(Exception e, Class<?>... clazzes) {
    return Arrays.stream(clazzes).anyMatch(c -> containsCause(e, c));
  }
}

