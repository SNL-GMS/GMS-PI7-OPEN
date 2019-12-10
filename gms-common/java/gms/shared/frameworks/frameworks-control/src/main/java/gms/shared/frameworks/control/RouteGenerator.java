package gms.shared.frameworks.control;

import gms.shared.frameworks.service.Route;
import gms.shared.frameworks.utilities.PathMethod;
import gms.shared.frameworks.utilities.ServiceReflectionUtilities;
import gms.shared.frameworks.utilities.Validation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates {@link Route}s for operations of an object annotated with {@link Path}.  Each of these
 * operations must also be annotated as {@link POST}.  The object's class must also be annotated
 * with {@link Path}; this annotation provides the base path.  Generated routes use {@link
 * DelegatingRequestHandler} which handles deserializing the input, invoking the annotated handler
 * operation with that input, serializing the handler operation's output, and returning an
 * appropriate status code.
 */
public class RouteGenerator {

  private static final Logger logger = LoggerFactory.getLogger(RouteGenerator.class);

  private RouteGenerator() {
  }

  /**
   * Creates {@link Route}s for all of the {@link Path} annotated operations in the provided
   * businessObject.  The businessObject may have any number of annotated operations.  This
   * operation fails if any of the annotated operations have incorrect signatures, if a {@link
   * Route} can't be created for any of the annotated operations, or if the Routes do not have
   * unique relative paths.
   *
   * A correct path signature is: 1) the method is public; 2) the method accepts a single input
   * parameter; 3) the method has a non-void return; 4) the method is also annotated with {@link
   * POST}.
   *
   * Strips leading whitespace and trailing whitespace from all {@link Path} values.  Interprets
   * {@link Path} values according to the slash conventions documented in {@link Path} (ignore a
   * leading slash, assume a trailing slash).  {@link Route#getPath()} calls on the returned {@link
   * Route} objects will have paths beginning and ending with a slash, e.g.
   * "/complete/path/for/route/"
   *
   * @param businessObject object containing annotated {@link Path} operations, not null
   * @return Set of {@link Route}, not null
   * @throws IllegalArgumentException if any of businessObject's {@link Path} handler operations do
   * not have the required signature; if there is a failure creating a {@link Route} for any of the
   * RouteHandlers; if the Routes have non-unique {@link Route#getPath()}
   * @throws NullPointerException if businessObject is null
   */
  public static Set<Route> generate(Object businessObject) {
    Objects
        .requireNonNull(businessObject, "RouteGenerator requires non-null businessObject");

    return createRoutes(businessObject,
        ServiceReflectionUtilities.findPathAnnotatedMethods(businessObject.getClass()));
  }

  /**
   * Creates a {@link Route} for each of the pathMethods in the businessObject.  All of the
   * pathMethods have the signature required by {@link Path}.
   *
   * @param businessObject object containing the pathMethods, not null
   * @param pathMethods collection of {@link PathMethod} containing valid Path annotated handler
   * operations
   * @return Set of {@link Route} containing a Route for each of the pathMethods
   * @throws IllegalArgumentException if there is a failure creating a {@link Route} for any of the
   * RouteHandlers.
   */
  private static Set<Route> createRoutes(Object businessObject,
      Collection<PathMethod> pathMethods) {

    // Try to create a Route for each handler operation
    final Set<Route> routes = new HashSet<>();
    final Set<Method> badRouteHandlerMethods = new HashSet<>();

    pathMethods.forEach(m -> toRoute(businessObject, m)
        .ifPresentOrElse(routes::add, () -> badRouteHandlerMethods.add(m.getMethod())));

    // Throw an exception if a Route could not be created for any of the pathMethods
    if (!badRouteHandlerMethods.isEmpty()) {
      final String description = "RouteGenerator could not generate Routes for every @Path "
          + "operation.  Failures occurred for these @Path operations:\n";

      Validation.throwForMethods(description, badRouteHandlerMethods);
    }

    return routes;
  }

  /**
   * Create a {@link Route} for a pathMethod {@link Method} in a businessObject.  The Route exposes
   * an endpoint beginning with the provided relativePath.  Produces an empty Optional if an
   * exception occurs creating the Route.
   *
   * @param businessObject object containing the pathMethod, not null
   * @param pathMethod a {@link PathMethod} for the annotated handler operation, not null
   * @return Optional {@link Route}, not null
   */
  private static Optional<Route> toRoute(Object businessObject, PathMethod pathMethod) {
    try {
      logger.trace("Creating route for annotated method {}", pathMethod);

      return Optional.of(Route.create(pathMethod.getRelativePath(),
          DelegatingRequestHandler.create(
              pathMethod.getMethod().getGenericParameterTypes()[0],
              invokeHandlerOperation(businessObject, pathMethod.getMethod()))));
    } catch (Exception e) {
      logger.error("Error creating Route for @Path operation", e);
    }

    return Optional.empty();
  }

  /**
   * Obtains a {@link Function} which invokes the handlerOperation {@link Method} on the
   * businessObject.  When applied, the Function throws an IllegalStateException if the
   * handlerOperation can't be invoked.
   *
   * @param businessObject invoke the handlerOperation {@link Method} on this object, not null
   * @param handlerOperation {@link Method} representing a handlerOperation accepting an input T and
   * returning an output, not null
   * @param <T> handlerOperation's input argument type
   * @return {@link Function} computing a result by invoking the handlerOperation on the
   * businessObject with an input of type T, not null
   */
  private static <T> Function<T, ?> invokeHandlerOperation(Object businessObject,
      Method handlerOperation) {

    final Function<Exception, RuntimeException> wrapException = e -> new IllegalStateException(
        "Failed to call method " + handlerOperation, e);

    try {
      handlerOperation.setAccessible(true);
    } catch (Exception e) {
      logger.error("Could not access method " + handlerOperation, e);
      throw wrapException.apply(e);
    }

    return i -> {
      logger.trace("invokeHandlerOperation lambda calling: {} with parameter: {}",
          handlerOperation, i);
      try {
        return handlerOperation.invoke(businessObject, i);
      } catch (Exception e) {
        logger.error("Error calling handlerOperation " + handlerOperation, e);
        throw wrapException.apply(e);
      }
    };
  }
}
