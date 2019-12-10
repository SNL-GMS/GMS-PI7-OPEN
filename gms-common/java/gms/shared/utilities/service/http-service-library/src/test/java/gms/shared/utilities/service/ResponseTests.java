package gms.shared.utilities.service;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import gms.shared.utilities.service.HttpStatus.Code;
import junit.framework.TestCase;
import org.junit.Test;

public class ResponseTests {

  private static final String ERROR_MSG = "error";

  @Test
  public void testSuccess() {
    final Response r = Response.success("foo");
    assertNotNull(r);
    TestCase.assertEquals(Code.OK, r.getHttpStatus());
    assertTrue(r.getBody().isPresent());
    assertFalse(r.getErrorMessage().isPresent());
    assertEquals("foo", r.getBody().get());
  }

  @Test(expected = NullPointerException.class)
  public void testSuccessNullBody() {
    Response.success(null);
  }

  @Test
  public void testClientError() {
    final Response r = Response.clientError(ERROR_MSG);
    assertNotNull(r);
    assertEquals(Code.BAD_REQUEST, r.getHttpStatus());
    assertFalse(r.getBody().isPresent());
    assertTrue(r.getErrorMessage().isPresent());
    assertEquals(ERROR_MSG, r.getErrorMessage().get());
  }

  @Test
  public void testServerError() {
    final Response r = Response.serverError(ERROR_MSG);
    assertNotNull(r);
    assertEquals(Code.INTERNAL_SERVER_ERROR, r.getHttpStatus());
    assertFalse(r.getBody().isPresent());
    assertTrue(r.getErrorMessage().isPresent());
    assertEquals(ERROR_MSG, r.getErrorMessage().get());
  }

  @Test
  public void testError() {
    final Code c = Code.BAD_GATEWAY;
    final Response r = Response.error(c, ERROR_MSG);
    assertNotNull(r);
    assertEquals(c, r.getHttpStatus());
    assertFalse(r.getBody().isPresent());
    assertTrue(r.getErrorMessage().isPresent());
    assertEquals(ERROR_MSG, r.getErrorMessage().get());
  }
}
