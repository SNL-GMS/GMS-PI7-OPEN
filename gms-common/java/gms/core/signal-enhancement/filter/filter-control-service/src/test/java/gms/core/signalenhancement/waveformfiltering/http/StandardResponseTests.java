package gms.core.signalenhancement.waveformfiltering.http;

import static org.junit.Assert.assertEquals;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StandardResponseTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testCreate() {
    final int expectedStatus = HttpStatus.ACCEPTED_202;
    final String expectedResponse = "{key:value}";
    final ContentType contentType = ContentType.APPLICATION_MSGPACK;
    StandardResponse response = StandardResponse
        .create(expectedStatus, expectedResponse, contentType);

    assertEquals(expectedStatus, response.getHttpStatus());
    assertEquals(expectedResponse, response.getResponseBody());
    assertEquals(contentType, response.getContentType());
  }

  @Test
  public void testCreateInvalidStatusExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("StandardResponse requires a valid httpStatus");
    StandardResponse.create(-1, "", ContentType.APPLICATION_JSON);
  }

  @Test
  public void testCreateNullResponseBodyExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("StandardResponse requires non-null responseBody");
    StandardResponse.create(HttpStatus.CONFLICT_409, null, ContentType.TEXT_PLAIN);
  }

  @Test
  public void testCreateNullContentTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("StandardResponse requires non-null contentType");
    StandardResponse.create(HttpStatus.CONFLICT_409, "", null);
  }
}
