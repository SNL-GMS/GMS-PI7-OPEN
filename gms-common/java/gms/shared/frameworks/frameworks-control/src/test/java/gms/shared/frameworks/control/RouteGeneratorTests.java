package gms.shared.frameworks.control;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.service.HttpStatus.Code;
import gms.shared.frameworks.service.Request;
import gms.shared.frameworks.service.RequestHandler;
import gms.shared.frameworks.service.Response;
import gms.shared.frameworks.service.Route;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.junit.jupiter.api.Test;

class RouteGeneratorTests {

  @Test
  void testGenerateValidatesParameters() {
    assertEquals("RouteGenerator requires non-null businessObject",
        assertThrows(NullPointerException.class, () -> RouteGenerator.generate(null)).getMessage());
  }

  /**
   * Tests a generated route invokes the annotated route handler operation.  Uses a non-trivial
   * input and output class to test serialization.
   */
  @Test
  void testGeneratedRouteInvokesHandlerOperation() throws JsonProcessingException {

    class Handler implements HandlerInterface {

      public Compound<String> foo(Collection<Compound<String>> compounds) {
        final Compound<String> first = compounds.iterator().next();
        final Collection<String> newGenerics = first.getGenericCollection().stream()
            .map(s -> s + " changed")
            .collect(Collectors.toList());
        return Compound.create(first.getInt(), newGenerics);
      }
    }

    final Handler handlerSpy = spy(new Handler());

    // Verify there is a generated route for each @Path operation
    final Set<Route> generatedRoutes = RouteGenerator.generate(handlerSpy);
    assertAll(
        () -> assertNotNull(generatedRoutes),
        () -> assertEquals(2, generatedRoutes.size()),
        () -> assertEquals(Set.of(basePath + intPath, basePath + objectPath),
            generatedRoutes.stream().map(Route::getPath).collect(Collectors.toSet())),
        () -> assertTrue(generatedRoutes.stream().map(Route::getHandler).noneMatch(Objects::isNull))
    );

    // Verify one of the routes has a RequestHandler delegating to the expected operation
    @SuppressWarnings("unchecked") final RequestHandler<Compound> requestHandler = generatedRoutes
        .stream()
        .filter(r -> r.getPath().contains(objectPath))
        .findAny()
        .orElseThrow(() -> new IllegalStateException("Could not find Route for objectPath"))
        .getHandler();

    final Collection<Compound<String>> input = List.of(Compound.create(10, List.of("hi")));
    final Compound<String> expectedOutput = Compound.create(10, List.of("hi changed"));

    final Request mockRequest = mock(Request.class);
    when(mockRequest.clientSentMsgpack()).thenReturn(false);
    when(mockRequest.getBody()).thenReturn(new ObjectMapper().writeValueAsString(input));

    final Response<Compound> response = requestHandler.handle(mockRequest, new ObjectMapper());

    assertAll(
        () -> assertEquals(Code.OK, response.getHttpStatus()),
        () -> assertEquals(expectedOutput, response.getBody().orElse(null))
    );
  }

  private static final String basePath = "/base/";
  private static final String intPath = "foo-int";
  private static final String objectPath = "foo-object";

  @Path(basePath)
  private interface HandlerInterface {

    @Path(objectPath)
    @POST
    Compound<String> foo(Collection<Compound<String>> input);

    @Path(intPath)
    @POST
    default int foo(int input) {
      return input * 2;
    }
  }
}
