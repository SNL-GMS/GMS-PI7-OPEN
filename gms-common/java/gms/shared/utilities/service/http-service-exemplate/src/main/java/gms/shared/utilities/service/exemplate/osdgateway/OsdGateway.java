package gms.shared.utilities.service.exemplate.osdgateway;

import java.util.Random;

public class OsdGateway implements OsdGatewayInterface {

  private final Random rand = new Random();

  @Override
  public int getMagicNumber() {
    return rand.nextInt();
  }
}
