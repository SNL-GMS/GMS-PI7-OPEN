package gms.shared.frameworks.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.service.Request;
import gms.shared.frameworks.service.RequestHandler;
import gms.shared.frameworks.service.RequestParsingUtils;
import gms.shared.frameworks.service.Response;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link RequestHandler} which extracts an input of type T, delegates logic to a Function which
 * computes a response U from the input, and returns a {@link Response} with this result.
 *
 * @param <T> type of the request body
 * @param <U> type of the response body
 */
@AutoValue
public abstract class DelegatingRequestHandler<T, U> implements RequestHandler<U> {

  private static final Logger logger = LoggerFactory.getLogger(
      DelegatingRequestHandler.class);

  /**
   * Obtain the type of the request body processed by this DelegatingRequestHandler
   *
   * @return an instance of {@link Type}, not null
   */
  public abstract Type getRequestType();

  /**
   * Obtains the handler operation containing the business logic invoked by this
   * DelegatingRequestHandler
   *
   * @return {@link Function} computing response body of type U from request body of type T, not
   *     null
   */
  public abstract Function<T, U> getHandlerOperation();

  /**
   * Obtains a new {@link DelegatingRequestHandler} which accepts requests with bodies of the
   * provided requestType and delegates processing to the provided handlerOperation.
   *
   * @param requestType {@link Type} of the request body type processed in the
   *     DelegatingRequestHandler, not null
   * @param handlerOperation {@link Function} containing the route handler logic, not null
   * @param <T> type of the request body
   * @param <U> type of the response body
   * @return {@link DelegatingRequestHandler}, not null
   * @throws NullPointerException if requestType or handlerOperation are null
   */
  public static <T, U> DelegatingRequestHandler<T, U> create(
      Type requestType, Function<T, U> handlerOperation) {
    return new AutoValue_DelegatingRequestHandler<>(requestType, handlerOperation);
  }

  /**
   * Handles a {@link Request} but deserializing the body, delegating processing to {@link
   * DelegatingRequestHandler#getHandlerOperation()}, and creating a {@link Response}. The response
   * is a {@link Response#success(Object)} when the handler operation completes successfully. The
   * response is a {@link Response#error(Code, String)} if the request body cannot be deserialized
   * to an instance of T. The response is a {@link Response#serverError(String)} if the handler
   * operation does not complete successfully.
   *
   * @param request {@link Request}, must have body containing a serialized T, not null
   * @param objectMapper {@link ObjectMapper} used to deserialize the body to a T
   * @return {@link Response}, not null
   */
  @Override
  public Response<U> handle(Request request, ObjectMapper objectMapper) {
    Objects.requireNonNull(request, "Request can't be null");
    Objects.requireNonNull(objectMapper, "ObjectMapper can't be null");

    logger.info("DelegatingRequestHandler received request");
    logger.trace("Request contents are {}", request);

    // Deserialize the input; return a client error if the request body can't be deserialized
    final T input;
    try {
      input = RequestParsingUtils.extractRequest(request, objectMapper,
          objectMapper.constructType(getRequestType()));
      logger.trace("Deserialized input to {} which is of type {}", input, input.getClass());
    } catch (IOException e) {
      final String msg
          = "Could not deserialize request body into an instance of this route handler's request type "
          + getRequestType();
      return Response.clientError(logAndReturnErrorMsg(msg, e));
    }

    // Invoke the handlerOperation; return a server error if the processing operation fails
    try {
      final U result = getHandlerOperation().apply(input);
      logger.trace("Executed handler operation which computed result {}", result);
      return Response.success(result);
    } catch (Exception e) {
      return Response.serverError(logAndReturnErrorMsg(
          "invoking handlerOperation failed", e));
    }
  }

  private static String logAndReturnErrorMsg(String context, Throwable e) {
    final String fullMsg = context + "\n" + ExceptionUtils.getStackTrace(e);
    logger.error(fullMsg, e);
    return fullMsg;
  }
}
