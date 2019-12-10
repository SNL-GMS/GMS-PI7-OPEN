package gms.core.signalenhancement.beam.service;

import java.time.Instant;
import org.eclipse.jetty.http.HttpStatus;

/**
 * Common route handlers for the Beam Control Service
 */
public class BeamCommonRouteHandler {

  /**
   * Route handler for alive endpoint.  Ignores any request headers.  Responds with {@link
   * ContentType#TEXT_PLAIN} body containing a simple aliveness message.
   *
   * @return {@link StandardResponse}, not null
   */
  public static StandardResponse alive() {
    final String aliveAt = "Beam Control Service alive at " + Instant.now();
    return StandardResponse.create(HttpStatus.OK_200, aliveAt, ContentType.TEXT_PLAIN);
  }
}
