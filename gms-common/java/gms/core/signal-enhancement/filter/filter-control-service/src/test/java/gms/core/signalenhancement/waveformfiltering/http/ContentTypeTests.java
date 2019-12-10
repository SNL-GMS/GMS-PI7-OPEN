package gms.core.signalenhancement.waveformfiltering.http;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ContentTypeTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testParseContentType() {
    assertEquals(ContentType.APPLICATION_JSON, ContentType.parse("application/json"));
    assertEquals(ContentType.APPLICATION_JSON, ContentType.parse("application/jSOn"));
    assertEquals(ContentType.APPLICATION_MSGPACK, ContentType.parse("application/msgpack"));
    assertEquals(ContentType.APPLICATION_MSGPACK, ContentType.parse("apPLicATioN/msgpack"));
    assertEquals(ContentType.TEXT_PLAIN, ContentType.parse("text/plain"));
    assertEquals(ContentType.UNKNOWN, ContentType.parse("unknown/type"));
  }

  @Test
  public void testParseContentTypeNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("parse requires non-null contentType");
    ContentType.parse(null);
  }
}
