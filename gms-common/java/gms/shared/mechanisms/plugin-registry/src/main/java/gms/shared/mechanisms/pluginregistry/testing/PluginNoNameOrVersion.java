package gms.shared.mechanisms.pluginregistry.testing;

import gms.shared.mechanisms.pluginregistry.Plugin;

public class PluginNoNameOrVersion implements DummyInterface1, Plugin {

  @Override
  public int getDummyValue1() {
    return 0;
  }
}
