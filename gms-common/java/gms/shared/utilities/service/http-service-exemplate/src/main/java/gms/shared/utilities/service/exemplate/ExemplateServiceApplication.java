package gms.shared.utilities.service.exemplate;

import gms.shared.utilities.service.HttpService;
import gms.shared.utilities.service.Route;
import gms.shared.utilities.service.ServiceDefinition;
import gms.shared.utilities.service.exemplate.osdgateway.OsdGateway;
import java.util.Set;


public class ExemplateServiceApplication {

  public static void main(String[] args) {
    RequestHandlers handlers = new RequestHandlers(new OsdGateway());
    ServiceDefinition def = ServiceDefinition.builder()
        .setRoutes(Set.of(Route.create("/alive", handlers::alive)))
        .setPort(8081)
        .build();
    final HttpService service = new HttpService(def);
    service.start();
  }
}
