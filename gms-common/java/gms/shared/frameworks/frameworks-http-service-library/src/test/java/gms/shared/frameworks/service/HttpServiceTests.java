package gms.shared.frameworks.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.shared.frameworks.service.HttpStatus.Code;
import gms.shared.frameworks.utilities.ServerConfig;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
      Route.create("/throw", (req, deser) -> {
        throw new RuntimeException("psyche!");
      })
  );

  private static final ObjectMapper jsonMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private static final ObjectMapper msgpackMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();

  private static int servicePort;

  private static HttpService service;

  @BeforeAll
  static void setup() throws Exception {
    servicePort = getAvailablePort();
    final ServiceDefinition def = ServiceDefinition.builder(
        ServerConfig.from(servicePort, 10, 20, Duration.ofMillis(100)))
        .setRoutes(routes).build();
    assertServiceIsUnreachable();
    service = new HttpService(def);
    service.start();
    assertTrue(service.isRunning());
    assertEquals(def, service.getDefinition());
  }

  @AfterAll
  static void teardown() throws Exception {
    service.stop();
    assertFalse(service.isRunning());
    assertServiceIsUnreachable();
    // call stop again to check that doesn't throw an exception
    service.stop();
  }

  @Test
  void testServiceStartAgain() {
    assertEquals("Service is already running",
        assertThrows(IllegalStateException.class,
            () -> service.start()).getMessage());
  }

  @Test
  void testConstructorNullCheck() {
    assertEquals("Cannot create HttpService will null definition",
        assertThrows(NullPointerException.class,
            () -> new HttpService(null)).getMessage());
  }

  @Test
  void testServiceRequests() throws Exception {
    testEchoBodyRoute();
    testEchoPathParamsRoute();
    testEchoKeysRouteJson();
    testEchoKeysRouteMsgpack();
    testErrorRoute();
  }

  @Test
  void testEchoBodyRoute() throws Exception {
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
  void testEchoPathParamsRoute() throws Exception {
    final String param = "foo";
    final HttpResponse<String> response = requestEchoPathParamsRoute(param);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK_200, response.getStatus());
    assertEquals(response.getBody(), "\"" + param + "\"");
  }

  @Test
  void testEchoKeysRouteJson() throws Exception {
    final Map<String, String> m = Map.of("key1", "val1", "key2", "val2");
    final HttpResponse<String> response = requestEchoKeysRoute(jsonMapper.writeValueAsString(m));
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK_200, response.getStatus());
    final String[] responseKeys = jsonMapper.readValue(response.getBody(), String[].class);
    assertEquals(Set.of("key1", "key2"), new HashSet<>(Arrays.asList(responseKeys)));
  }

  @Test
  void testEchoKeysRouteMsgpack() throws Exception {
    final Map<String, String> m = Map.of("key1", "val1", "key2", "val2");
    final HttpResponse<String> response = requestEchoKeysRouteMsgpack(
        msgpackMapper.writeValueAsBytes(m));
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK_200, response.getStatus());
    final String[] responseKeys = jsonMapper.readValue(response.getBody(), String[].class);
    assertEquals(Set.of("key1", "key2"), new HashSet<>(Arrays.asList(responseKeys)));
  }

  @Test
  void testErrorRoute() throws Exception {
    final HttpResponse<String> response = Unirest.post("http://localhost:" + servicePort
        + "/error").asString();
    assertNotNull(response.getBody());
    assertEquals(Code.BAD_GATEWAY.getStatusCode(), response.getStatus());
    assertEquals("error", response.getBody());
    assertEquals("text/plain", response.getHeaders().getFirst("Content-Type"));
  }

  @Test
  void testThrowRoute() throws Exception {
    final HttpResponse<String> response = requestThrowRoute();
    assertEquals(Code.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    assertTrue(response.getBody().contains("psyche"),
        "Expected response to contain error message ('psyche')");
  }

  @Test
  void testServiceMakesHealthcheckRoute() throws Exception {
    final HttpResponse<String> response = Unirest
        .get("http://localhost:" + servicePort + ServiceDefinition.HEALTHCHECK_PATH)
        .asString();
    assertEquals(Code.OK.getStatusCode(), response.getStatus());
    assertTrue(response.getBody().contains("alive at"));
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

  private static HttpResponse<String> requestEchoBodyRoute(String body, boolean msgpack)
      throws Exception {
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
