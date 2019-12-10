package gms.shared.mechanisms.pluginregistry.testing;

import gms.shared.mechanisms.pluginregistry.LogCall;
import gms.shared.mechanisms.pluginregistry.Name;
import gms.shared.mechanisms.pluginregistry.Plugin;
import gms.shared.mechanisms.pluginregistry.Version;
import java.util.Objects;

@Name("dummy1")
@Version("1.0.0")
public class DummyPluginClass1 implements DummyInterface1, Plugin {
  private int dummyValue;

  public DummyPluginClass1() {
    this.dummyValue = 365;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DummyPluginClass1 that = (DummyPluginClass1) o;
    return Double.compare(that.dummyValue, dummyValue) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(dummyValue);
  }

  @Override
  public int getDummyValue1() {
    return dummyValue;
  }
}
