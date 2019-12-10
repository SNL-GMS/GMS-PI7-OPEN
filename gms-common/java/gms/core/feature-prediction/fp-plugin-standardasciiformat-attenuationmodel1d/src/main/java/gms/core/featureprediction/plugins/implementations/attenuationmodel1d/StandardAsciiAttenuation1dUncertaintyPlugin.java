package gms.core.featureprediction.plugins.implementations.attenuationmodel1d;

import gms.core.featureprediction.plugins.Attenuation1dUncertaintyPlugin;
import gms.shared.mechanisms.pluginregistry.Name;
import gms.core.featureprediction.plugins.implementations.earthmodel1d.StandardAsciiTravelTime1dFileReader;
import gms.core.featureprediction.plugins.implementations.earthmodel1d.StandardAsciiTravelTime1dPlugin;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import gms.shared.mechanisms.pluginregistry.Version;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;


@Name("standardAsciiAttenuation1dUncertaintyPlugin")
@Version("1.0.0")
public class StandardAsciiAttenuation1dUncertaintyPlugin extends
    StandardAsciiTravelTime1dPlugin implements
    Attenuation1dUncertaintyPlugin {

  @Override
  public void initialize(Set<String> earthModelNames) throws IOException {
    if (fileReaderMap == null) {
      fileReaderMap = new HashMap<>();
      for (String earthModelName : earthModelNames) {
        String path = "attenuationUncertaintyModels/" + earthModelName;
        fileReaderMap.put(earthModelName, StandardAsciiTravelTime1dFileReader.from(path));
      }
      fileReaderMap = Collections.unmodifiableMap(fileReaderMap);

      PluginRegistry pluginRegistry = PluginRegistry.getRegistry();
      pluginRegistry.loadAndRegister();

    } else {
      throw new IOException("initialize() method has already been called.");
    }
  }
}
