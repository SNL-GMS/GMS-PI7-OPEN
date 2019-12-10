package gms.shared.frameworks.service;

import com.google.auto.value.AutoValue;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.lang3.Validate;

/**
 * Defines a service route, which is a mapping from URL to a handler for requests.
 */
@AutoValue
public abstract class Route {

  /**
   * Gets the path (URL) of the route
   *
   * @return a string representing the URL, e.g. "/foo/bar"
   */
  public abstract String getPath();

  /**
   * Gets the handler that is to be used for requests matching this route.
   *
   * @return the request handler
   */
  public abstract RequestHandler getHandler();

  /**
   * Creates a Route
   *
   * @param path the path (URL) of the route
   * @param handler the handler that is to be used for requests matching this route
   * @return a route
   */
  public static Route create(String path, RequestHandler handler) {
    Validate.notBlank(path, "Path cannot be blank or null");
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    validateIsUrl(path);
    return new AutoValue_Route(path, handler);
  }

  private static void validateIsUrl(String path) {
    try {
      new URI(path);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(String.format(
          "Invalid path for route; path is %s...error: %s",
          path, e.getMessage()));
    }
  }
}
