package gms.shared.frameworks.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RouteTests {

  private static final RequestHandler handler = (req, deser) -> Response.success("hi");

  @Test
  void testCreateBlankPath() {
    assertEquals("Path cannot be blank or null",
        assertThrows(IllegalArgumentException.class,
            () -> Route.create("", handler)).getMessage());
  }

  @Test
  void testCreateNullPath() {
    assertEquals("Path cannot be blank or null",
        assertThrows(NullPointerException.class,
            () -> Route.create(null, handler)).getMessage());
  }

  @Test
  void testCreateInvalidUrlPath() {
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> Route.create("a bad url", handler)).getMessage()
        .startsWith("Invalid path for route"));
  }

  @Test
  void testCreate() {
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
