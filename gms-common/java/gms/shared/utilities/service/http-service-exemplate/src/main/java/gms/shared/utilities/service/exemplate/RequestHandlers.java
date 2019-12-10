package gms.shared.utilities.service.exemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.utilities.service.Request;
import gms.shared.utilities.service.Response;
import gms.shared.utilities.service.exemplate.osdgateway.OsdGatewayInterface;
import java.time.Instant;
import java.util.Objects;


/**
 * Contains handler functions for the service routes.
 */
public class RequestHandlers {

  private final OsdGatewayInterface osdGateway;

  public RequestHandlers(OsdGatewayInterface osdGateway) {
    this.osdGateway = Objects.requireNonNull(osdGateway);
  }

  /**
   * State of health operation to determine if the service is running.  Returns
   * a message with the current time in plaintext.
   *
   * @return Response code 200 with a plaintext string containing the current time
   */
  public Response<ExampleDto> alive(Request request, ObjectMapper deserializer) {
    // show how to get the raw Spark object of the request
    ExampleDto response = new ExampleDto("alive at " + Instant.now(), osdGateway.getMagicNumber());
    return Response.success(response);
  }
}
