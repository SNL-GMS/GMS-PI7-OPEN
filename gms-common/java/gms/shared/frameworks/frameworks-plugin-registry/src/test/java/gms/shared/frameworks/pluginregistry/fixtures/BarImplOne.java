package gms.shared.frameworks.pluginregistry.fixtures;

import com.google.auto.service.AutoService;
import gms.shared.frameworks.pluginregistry.Plugin;

@AutoService(Plugin.class)
public class BarImplOne implements IBar {

  public static final String NAME = "bar_impl_1";

  public static final long VALUE = 1;

  public BarImplOne() {
  }

  @Override
  public long getBarValue() {
    return VALUE;
  }

  @Override
  public String getName() {
    return NAME;
  }
}
