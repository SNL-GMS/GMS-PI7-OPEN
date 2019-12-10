package gms.core.signalenhancement.fk.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

public class FkCommonRouteHandlerTests {

  @Test
  public void testAlive() {
    final StandardResponse response = FkCommonRouteHandler.alive();

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
    assertEquals(ContentType.TEXT_PLAIN, response.getContentType());
    assertTrue(String.class.isInstance(response.getResponseBody()));
    assertTrue(((String)response.getResponseBody()).contains("Fk Control Service alive at "));
  }

}
