package gms.shared.mechanisms.pluginregistry.testing;

import gms.shared.mechanisms.pluginregistry.Name;
import gms.shared.mechanisms.pluginregistry.Plugin;
import gms.shared.mechanisms.pluginregistry.Version;
import java.util.Objects;

@Name("muhDummyClass3")
@Version("1.0.0")
public class DummyPluginClass3 implements DummyInterface2, Plugin {
  private int dummyValue;

  public DummyPluginClass3() {
    this.dummyValue = 367;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DummyPluginClass3 that = (DummyPluginClass3) o;
    return Double.compare(that.dummyValue, dummyValue) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(dummyValue);
  }

  @Override
  public int getDummyValue2() {
    return dummyValue;
  }
}