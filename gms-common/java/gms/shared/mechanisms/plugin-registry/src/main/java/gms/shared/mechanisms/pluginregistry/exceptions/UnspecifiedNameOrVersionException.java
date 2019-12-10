package gms.shared.mechanisms.pluginregistry.exceptions;

import gms.shared.mechanisms.pluginregistry.Plugin;

public class UnspecifiedNameOrVersionException extends RuntimeException {

  public UnspecifiedNameOrVersionException(Class<? extends Plugin> pluginClass) {
    super(pluginClass + " is trying to be a plugin but is missing Name or Version annotations");
  }

}
