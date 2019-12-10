package gms.shared.utilities.service;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RouteTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private static final RequestHandler handler = (req, deser) -> Response.success("hi");

  @Test
  public void testCreateBlankPath() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Path cannot be blank or null");
    Route.create("", handler);
  }

  @Test
  public void testCreateNullPath() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Path cannot be blank or null");
    Route.create(null, handler);
  }

  @Test
  public void testCreateInvalidUrlPath() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid path for route");
    Route.create("a bad url", handler);
  }

  @Test
  public void testCreate() {
    final String expectedPath = "/foo";
    Route r = Route.create(expectedPath, handler);
    assertNotNull(r);
    assertEquals(expectedPath, r.getPath());
    assertEquals(handler, r.getHandler());
    // provide path without leading slash; check that expected path was still created
    r = Route.create("foo", handler);
    assertNotNull(r);
    assertEquals(expectedPath, r.getPath());
    assertEquals(handler, r.getHandler());
  }

}
