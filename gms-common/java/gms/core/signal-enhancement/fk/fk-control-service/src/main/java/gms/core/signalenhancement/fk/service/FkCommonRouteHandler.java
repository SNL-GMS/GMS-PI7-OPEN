package gms.core.signalenhancement.fk.service;

import java.time.Instant;
import org.eclipse.jetty.http.HttpStatus;

/**
 * Common route handlers for the FK Control Service that are not specific to either FK Spectra
 * or FK Measurements calculations.
 */
public class FkCommonRouteHandler {

  /**
   * Route handler for alive endpoint.  Ignores any request headers.  Responds with {@link
   * ContentType#TEXT_PLAIN} body containing a simple aliveness message.
   *
   * @return {@link StandardResponse}, not null
   */
  public static StandardResponse alive() {
    final String aliveAt = "Fk Control Service alive at " + Instant.now();
    return StandardResponse.create(HttpStatus.OK_200, aliveAt, ContentType.TEXT_PLAIN);
  }
}
