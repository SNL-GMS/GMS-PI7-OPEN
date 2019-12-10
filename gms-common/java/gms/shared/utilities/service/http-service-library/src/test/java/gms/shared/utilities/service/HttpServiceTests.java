package gms.shared.utilities.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.shared.utilities.service.HttpStatus.Code;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HttpServiceTests {

  private static final Set<Route> routes = Set.of(
      // returns the body of the request unchanged.
      // Purposely leave out leading '/' to see that it gets setup correctly anyway.
      Route.create("echoBody",
          (req, deserializer) -> Response.success(req.getBody())),
      // returns the keys from body of the request as an array
      Route.create("/echoKeys", HttpServiceTests::echoKeys),
      // returns the path param 'foo', and if not present sets status to 400
      Route.create("/echoPathParam/:foo", HttpServiceTests::echoPathParam),
      // returns an error
      Route.create("/error", (req, deserializer) -> Response.error(Code.BAD_GATEWAY, "error")),
      // a route that just always throws an exception
      Route.create("/throw", (req, deser) -> {throw new RuntimeException("psyche!");})
  );

  private static final ObjectMapper
      jsonMapper = Defaults.JSON_MAPPER,
      msgpackMapper = Defaults.MSGPACK_MAPPER;

  private static int servicePort;

  private static HttpService service;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @BeforeClass
  public static void setup() throws Exception {
    servicePort = getAvailablePort();
    final ServiceDefinition def = ServiceDefinition.builder()
        .setRoutes(routes)
        .setPort(servicePort)
        .build();
    assertServiceIsUnreachable();
    service = new HttpService(def);
    service.start();
    assertTrue(service.isRunning());
    assertEquals(def, service.getDefinition());
  }

  @AfterClass
  public static void teardown() throws Exception {
    service.stop();
    assertFalse(service.isRunning());
    assertServiceIsUnreachable();
    // call stop again to check that doesn't throw an exception
    service.stop();
  }

  @Test
  public void testServiceStartAgain() {
    exception.expect(IllegalStateException.class);
    exception.expectMessage("Service is already running");
    service.start();
  }

  @Test
  public void testConstructorNullCheck() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot create HttpService will null definition");
    new HttpService(null);
  }

  @Test
  public void testServiceRequests() throws Exception {
    testEchoBodyRoute();
    testEchoPathParamsRoute();
    testEchoKeysRouteJson();
    testEchoKeysRouteMsgpack();
    testErrorRoute();
  }

  @Test
  public void testEchoBodyRoute() throws Exception {
    // test 'echo body' route with JSON
    final String body = "a body";
    HttpResponse<String> response = requestEchoBodyRoute(body, false);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK_200, response.getStatus());
    assertEquals("application/json", response.getHeaders().getFirst("Content-Type"));
    // response body is wrapped in quotes because of how JSON serialization works
    assertEquals("\"" + body + "\"", response.getBody());
    response = requestEchoBodyRoute(body, true);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK_200, response.getStatus());
    assertEquals("application/msgpack", response.getHeaders().getFirst("Content-Type"));
    final byte[] expected = msgpackMapper.writeValueAsBytes(body);
    assertArrayEquals(expected, response.getRawBody().readAllBytes());
  }

  @Test
  public void testEchoPathParamsRoute() throws Exception {
    final String param = "foo";
    final HttpResponse<String> response = requestEchoPathParamsRoute(param);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK_200, response.getStatus());
    assertEquals(response.getBody(), "\"" + param + "\"");
  }

  @Test
  public void testEchoKeysRouteJson() throws Exception {
    final Map<String, String> m = Map.of("key1", "val1", "key2", "val2");
    final HttpResponse<String> response = requestEchoKeysRoute(jsonMapper.writeValueAsString(m));
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK_200, response.getStatus());
    final String[] responseKeys = jsonMapper.readValue(response.getBody(), String[].class);
    assertEquals(Set.of("key1", "key2"), new HashSet<>(Arrays.asList(responseKeys)));
  }

  @Test
  public void testEchoKeysRouteMsgpack() throws Exception {
    final Map<String, String> m = Map.of("key1", "val1", "key2", "val2");
    final HttpResponse<String> response = requestEchoKeysRouteMsgpack(msgpackMapper.writeValueAsBytes(m));
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK_200, response.getStatus());
    final String[] responseKeys = jsonMapper.readValue(response.getBody(), String[].class);
    assertEquals(Set.of("key1", "key2"), new HashSet<>(Arrays.asList(responseKeys)));
  }

  @Test
  public void testErrorRoute() throws Exception {
    final HttpResponse<String> response = Unirest.post("http://localhost:" + servicePort
        + "/error").asString();
    assertNotNull(response.getBody());
    assertEquals(Code.BAD_GATEWAY.getCode(), response.getStatus());
    assertEquals("error", response.getBody());
    assertEquals("text/plain", response.getHeaders().getFirst("Content-Type"));
  }

  @Test
  public void testThrowRoute() throws Exception {
    final HttpResponse<String> response = requestThrowRoute();
    assertEquals(Code.INTERNAL_SERVER_ERROR.getCode(), response.getStatus());
    assertTrue("Expected response to contain error message ('psyche')",
        response.getBody().contains("psyche"));
  }

  private static void assertServiceIsUnreachable() throws Exception {
    try {
      requestEchoBodyRoute("foo", false);
      fail("Expected to throw exception by trying to connect to "
          + "service when it's supposed to be unreachable");
    } catch (UnirestException ex) {
      // do nothing; expected to throw a unirest exception
    }
  }

  private static HttpResponse<String> requestEchoBodyRoute(String body, boolean msgpack) throws Exception {
    return Unirest.post("http://localhost:" + servicePort + "/echoBody")
        .header("Content-Type", "application/json")
        .header("Accept", msgpack ? "application/msgpack" : "application/json")
        .body(body)
        .asString();
  }

  private static HttpResponse<String> requestEchoKeysRoute(String body) throws Exception {
    return Unirest.post("http://localhost:" + servicePort + "/echoKeys")
        .header("Content-Type", "application/json")
        .body(body)
        .asString();
  }

  private static HttpResponse<String> requestEchoKeysRouteMsgpack(byte[] body) throws Exception {
    return Unirest.post("http://localhost:" + servicePort + "/echoKeys")
        .header("Content-Type", "application/msgpack")
        .body(body)
        .asString();
  }

  private static HttpResponse<String> requestEchoPathParamsRoute(String param) throws Exception {
    return Unirest.post("http://localhost:" + servicePort + "/echoPathParam/{foo}")
        .routeParam("foo", param)
        .asString();
  }

  private static HttpResponse<String> requestThrowRoute() throws Exception {
    return Unirest.post("http://localhost:" + servicePort + "/throw")
        .asString();
  }

  private static Response echoKeys(Request request, ObjectMapper deserializer) {
    try {
      // exercise the functionality to get headers
      Map<String, String> headers = request.getHeaders();
      assertNotNull(headers);
      // assert there are at least some headers;
      // hard to rely on specific ones because it depends on the request
      assertFalse(headers.isEmpty());
      JsonNode json = deserializer.readTree(request.getRawBody());
      List<String> keys = new ArrayList<>();
      json.fieldNames().forEachRemaining(keys::add);
      return Response.success(keys);
    } catch (IOException e) {
      return Response.clientError("Exception on deserialization: " + e.getMessage());
    }
  }

  private static Response echoPathParam(Request request, ObjectMapper deserializer) {
    return request.getPathParam("foo").isPresent() ?
        Response.success(request.getPathParam("foo"))
        : Response.clientError("path parameter foo must be specified");
  }

  private static int getAvailablePort() throws Exception {
    ServerSocket ephemeralSocket = new ServerSocket(0);
    final int port = ephemeralSocket.getLocalPort();
    ephemeralSocket.close();
    return port;
  }

}
