package gms.shared.frameworks.pluginregistry.fixtures;

import com.google.auto.service.AutoService;
import gms.shared.frameworks.pluginregistry.Plugin;

@AutoService(Plugin.class)
public class FooImplTwo implements IFoo {

  public static final String NAME ="foo_impl_2";

  public static final long VALUE = 4;

  public FooImplTwo() { }

  @Override
  public long getFooValue() {
    return VALUE;
  }

  @Override
  public String getName() {
    return NAME;
  }
}