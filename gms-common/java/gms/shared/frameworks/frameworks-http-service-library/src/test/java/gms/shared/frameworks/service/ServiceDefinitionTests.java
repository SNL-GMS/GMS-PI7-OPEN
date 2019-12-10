package gms.shared.frameworks.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gms.shared.frameworks.utilities.ServerConfig;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class ServiceDefinitionTests {

  private ServiceDefinition.Builder baseBuilder;

  @BeforeEach
  void setup() {
    baseBuilder = ServiceDefinition.builder(
        ServerConfig.from(8080, 1, 2, Duration.ofMillis(50)));
  }

  @Test
  void testBuilder() {
    final Route r = createRoute();
    final ObjectMapper jsonMapper = new ObjectMapper();
    final ObjectMapper msgpackMapper = new ObjectMapper();
    // set some properties of the object mapper to not match the defaults
    // so it can be determined that they were actually taken in by the ServiceDefinition
    jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
    msgpackMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    final ServerConfig serverConfig = ServerConfig.from(
        1234, 5, 13, Duration.ofMillis(5000));
    final ServiceDefinition def = ServiceDefinition.builder(serverConfig)
        .setJsonMapper(jsonMapper)
        .setMsgpackMapper(msgpackMapper)
        .setRoutes(Set.of(r))
        .build();
    assertEquals(jsonMapper, def.getJsonMapper());
    assertEquals(msgpackMapper, def.getMsgpackMapper());
    assertEquals(serverConfig, def.getServerConfig());
    assertTrue(def.getRoutes().contains(r),
        "Expected the added route to be contained: " + r);
  }

  @Test
  void testRoutesImmutable() {
    assertThrows(UnsupportedOperationException.class,
        () -> baseBuilder.build().getRoutes().add(createRoute()));
  }

  @Test
  void testDuplicateRoutesExpectIllegalArgumentException() {
    final Set<Route> routes = Set.of(
        createRoute("/foo", "hello"), createRoute("/foo", "bye"),
        createRoute("/bar", "hello"), createRoute("/bar", "bye"));

    assertIllegalArgumentExceptionThrown(
        () -> baseBuilder.setRoutes(routes).build(),
        Set.of(
            "Each route must have a unique path but paths but the following paths are duplicated",
            "/foo", "/bar"));
  }

  @Test
  void testHealthcheckRouteDefinedExpectIllegalArgumentException() {
    assertIllegalArgumentExceptionThrown(
        () -> baseBuilder
            .setRoutes(Set.of(createRoute(ServiceDefinition.HEALTHCHECK_PATH, "hello"))).build(),
        "Endpoint " + ServiceDefinition.HEALTHCHECK_PATH
            + " is reserved and cannot be provided as a Route's path");
  }

  private static void assertIllegalArgumentExceptionThrown(Executable r, String message) {
    assertIllegalArgumentExceptionThrown(r, Set.of(message));
  }

  private static void assertIllegalArgumentExceptionThrown(Executable r, Set<String> message) {
    final String actualMessage = assertThrows(IllegalArgumentException.class, r).getMessage();

    message.forEach(m -> assertTrue(actualMessage.contains(m),
        "Expected exception message '" + actualMessage + "' to contain message '" + m + "'"));
  }

  private static Route createRoute() {
    return createRoute("/foo", "hello");
  }

  private static Route createRoute(String path, String response) {
    return Route.create(path, (req, deserializer) -> Response.success(response));
  }
}
