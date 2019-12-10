package gms.core.signalenhancement.beam.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

/**
 * Error handler utilities creating {@link StandardResponse} for common error scenarios.
 */
public class ErrorHandler {

  private static Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

  // Prevent class from being instantiated, since it is a "static" class.
  private ErrorHandler() {
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
        DateTimeParseException.class, JsonProcessingException.class,
        MismatchedInputException.class)) {
      response.status(HttpStatus.BAD_REQUEST_400);
    } else if (containsCause(e, DataExistsException.class)) {
      response.status(HttpStatus.CONFLICT_409);
    } else {
      response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }
    response.type("application/text");
    response.body(e.getMessage() == null ? e.toString() : e.getMessage());
  }

  private static boolean containsCause(Exception e, Class<?> clazz) {
    return ExceptionUtils.indexOfThrowable(e, clazz) >= 0;
  }

  private static boolean containsAnyCause(Exception e, Class<?>... clazzes) {
    return Arrays.stream(clazzes).anyMatch(c -> containsCause(e, c));
  }
}
