package gms.shared.mechanisms.pluginregistry.testing;

import gms.shared.mechanisms.pluginregistry.Name;
import gms.shared.mechanisms.pluginregistry.Plugin;

@Name("noVersion")
public class PluginNoVersion implements DummyInterface1, Plugin {

  @Override
  public int getDummyValue1() {
    return 0;
  }
}
