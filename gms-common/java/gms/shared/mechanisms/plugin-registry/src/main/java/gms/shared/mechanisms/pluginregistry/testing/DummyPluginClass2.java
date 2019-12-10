package gms.shared.mechanisms.pluginregistry.testing;

import gms.shared.mechanisms.pluginregistry.LogCall;
import gms.shared.mechanisms.pluginregistry.Name;
import gms.shared.mechanisms.pluginregistry.Plugin;
import gms.shared.mechanisms.pluginregistry.Version;
import java.util.Objects;

@Name("dummy2")
@Version("1.0.0")
public class DummyPluginClass2 implements DummyInterface1, Plugin {
  private int dummyValue;

  public DummyPluginClass2() {
    this.dummyValue = 366;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DummyPluginClass2 that = (DummyPluginClass2) o;
    return Double.compare(that.dummyValue, dummyValue) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(dummyValue);
  }

  @Override
  @LogCall
  public int getDummyValue1() {
    return dummyValue;
  }
}