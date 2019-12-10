package gms.shared.mechanisms.pluginregistry.testing;

import gms.shared.mechanisms.pluginregistry.Plugin;
import gms.shared.mechanisms.pluginregistry.Version;

@Version("1.0.0")
public class PluginNoName implements DummyInterface1, Plugin {

  @Override
  public int getDummyValue1() {
    return 0;
  }
}
