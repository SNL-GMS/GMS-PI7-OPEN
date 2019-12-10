package gms.core.signaldetection.signaldetectorcontrol.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ErrorHandlerTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testHandle404NotFound() {
    final String url = "http://path/to/not/found";
    final String expectedBodyString = "Error 404 - Not Found (" + url + ")";

    StandardResponse response = ErrorHandler.handle404(url);
    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND_404, response.getHttpStatus());
    assertEquals(ContentType.TEXT_PLAIN, response.getContentType());
    assertEquals(expectedBodyString, response.getResponseBody());
  }

  @Test
  public void testHandle404NullUrlExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ErrorHandler.handle404 requires non-null resource URL");
    ErrorHandler.handle404(null);
  }

  @Test
  public void testHandle500InternalServerError() {
    final String errorMessage = "Custom error message causing the 500";
    final String expectedBodyString = "Error 500 - Internal Server Error: " + errorMessage;

    StandardResponse response = ErrorHandler.handle500(errorMessage);
    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getHttpStatus());
    assertEquals(ContentType.TEXT_PLAIN, response.getContentType());
    assertEquals(expectedBodyString, response.getResponseBody());
  }

  @Test
  public void testHandle500ErrorMessageEmpty() {
    final String expectedBodyString = "Error 500 - Internal Server Error";
    StandardResponse response = ErrorHandler.handle500(" ");
    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getHttpStatus());
    assertEquals(ContentType.TEXT_PLAIN, response.getContentType());
    assertEquals(expectedBodyString, response.getResponseBody());
  }

  @Test
  public void testHandle500NullMessageExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ErrorHandler.handle500 requires non-null errorMessage");
    ErrorHandler.handle500(null);
  }
}
