package gms.shared.frameworks.control;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.service.HttpStatus.Code;
import gms.shared.frameworks.service.Request;
import gms.shared.frameworks.service.Response;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DelegatingRequestHandlerTests {

  private static final Type intType = Integer.class;

  @Test
  void testCreate() {
    final Type requestType = intType;
    final Function<Integer, Double> handlerOperation = i -> i / 2.0;
    final DelegatingRequestHandler delegatingRequestHandler = DelegatingRequestHandler
        .create(requestType, handlerOperation);

    assertAll(
        () -> assertNotNull(delegatingRequestHandler),
        () -> assertEquals(requestType, delegatingRequestHandler.getRequestType()),
        () -> assertEquals(handlerOperation, delegatingRequestHandler.getHandlerOperation())
    );
  }

  @Test
  void testHandle() {

    // Can't use a lambda since Mockito.spy doesn't work with final classes
    final Function<Integer, Integer> handlerOperation = new Function<>() {
      @Override
      public Integer apply(Integer integer) {
        return integer / 2;
      }
    };
    final Function<Integer, Integer> handlerOperationSpy = spy(handlerOperation);

    final DelegatingRequestHandler<Integer, Integer> handler = DelegatingRequestHandler
        .create(intType, handlerOperationSpy);

    final Request mockRequest = mock(Request.class);
    when(mockRequest.clientSentMsgpack()).thenReturn(false);
    when(mockRequest.getBody()).thenReturn("8");

    final Response<Integer> response = handler.handle(mockRequest, new ObjectMapper());

    // Verifies handler logic is delegated
    verify(handlerOperationSpy, times(1)).apply(Mockito.any());

    // Verify expected response
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(Code.OK, response.getHttpStatus()),
        () -> assertEquals(4, (int) response.getBody().orElse(Integer.MIN_VALUE))
    );
  }

  @Test
  void testHandleDeserializeFailsExpectClientError() {
    final DelegatingRequestHandler<Integer, Double> handler = DelegatingRequestHandler
        .create(intType, i -> i / 4.0);

    final Request mockRequest = mock(Request.class);
    when(mockRequest.clientSentMsgpack()).thenReturn(false);
    when(mockRequest.getBody()).thenReturn("ThisIsNotAnInteger");

    final Response<Double> response = handler.handle(mockRequest, new ObjectMapper());

    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(Code.BAD_REQUEST, response.getHttpStatus()),
        () -> assertTrue(response.getErrorMessage().orElse("").startsWith(
            "Could not deserialize request body into an instance of this route handler's request type")));
  }

  @Test
  void testHandleHandlerOperationFailsExpectServerError() {
    final DelegatingRequestHandler<Integer, Double> handler = DelegatingRequestHandler
        .create(intType, i -> {
          throw new IllegalArgumentException("");
        });

    final Request mockRequest = mock(Request.class);
    when(mockRequest.clientSentMsgpack()).thenReturn(false);
    when(mockRequest.getBody()).thenReturn("7");

    final Response<Double> response = handler.handle(mockRequest, new ObjectMapper());

    // Verify expected response
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(Code.INTERNAL_SERVER_ERROR, response.getHttpStatus()),
        () -> assertTrue(
            response.getErrorMessage().orElse("").contains("handlerOperation failed"))
    );
  }

  @Test
  void testHandleValidatesInputs() {
    final DelegatingRequestHandler<Integer, Double> handler = DelegatingRequestHandler
        .create(intType, i -> i / 4.0);

    verifyNullPointerException(() -> handler.handle(null, new ObjectMapper()),
        "Request can't be null");
    verifyNullPointerException(() -> handler.handle(mock(Request.class), null),
        "ObjectMapper can't be null");
  }

  private static void verifyNullPointerException(Executable executable, String message) {
    assertTrue(assertThrows(NullPointerException.class, executable).getMessage().contains(message));
  }
}
