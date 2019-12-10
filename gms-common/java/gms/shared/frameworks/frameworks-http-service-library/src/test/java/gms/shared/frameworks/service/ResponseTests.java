package gms.shared.frameworks.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.frameworks.service.HttpStatus.Code;
import org.junit.jupiter.api.Test;

public class ResponseTests {

  private static final String ERROR_MSG = "error";

  @Test
  void testSuccess() {
    final Response r = Response.success("foo");
    assertNotNull(r);
    assertEquals(Code.OK, r.getHttpStatus());
    assertTrue(r.getBody().isPresent());
    assertFalse(r.getErrorMessage().isPresent());
    assertEquals("foo", r.getBody().get());
  }

  @Test
  void testSuccessNullBody() {
    assertThrows(NullPointerException.class, () -> Response.success(null));
  }

  @Test
  void testClientError() {
    final Response r = Response.clientError(ERROR_MSG);
    assertNotNull(r);
    assertEquals(Code.BAD_REQUEST, r.getHttpStatus());
    assertFalse(r.getBody().isPresent());
    assertTrue(r.getErrorMessage().isPresent());
    assertEquals(ERROR_MSG, r.getErrorMessage().get());
  }

  @Test
  void testServerError() {
    final Response r = Response.serverError(ERROR_MSG);
    assertNotNull(r);
    assertEquals(Code.INTERNAL_SERVER_ERROR, r.getHttpStatus());
    assertFalse(r.getBody().isPresent());
    assertTrue(r.getErrorMessage().isPresent());
    assertEquals(ERROR_MSG, r.getErrorMessage().get());
  }

  @Test
  void testError() {
    final Code c = Code.BAD_GATEWAY;
    final Response r = Response.error(c, ERROR_MSG);
    assertNotNull(r);
    assertEquals(c, r.getHttpStatus());
    assertFalse(r.getBody().isPresent());
    assertTrue(r.getErrorMessage().isPresent());
    assertEquals(ERROR_MSG, r.getErrorMessage().get());
  }
}
