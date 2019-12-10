package gms.core.featureprediction.plugins.implementations.attenuationmodel1d;

import gms.core.featureprediction.plugins.Attenuation1dPlugin;
import gms.core.featureprediction.plugins.Attenuation1dUncertaintyPlugin;
import gms.core.featureprediction.plugins.implementations.earthmodel1d.StandardAsciiTravelTime1dFileReader;
import gms.core.featureprediction.plugins.implementations.earthmodel1d.StandardAsciiTravelTime1dPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.pluginregistry.Name;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import gms.shared.mechanisms.pluginregistry.Version;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Name("standardAsciiAttenuation1dPlugin")
@Version("1.0.0")
public class StandardAsciiAttenuation1dPlugin extends StandardAsciiTravelTime1dPlugin implements
    Attenuation1dPlugin {

  private Attenuation1dUncertaintyPlugin attenuationUncertaintyPlugin;

  @Override
  public void initialize(Set<String> earthModelNames) throws IOException {
    if (fileReaderMap == null) {
      fileReaderMap = new HashMap<>();
      for (String earthModelName : earthModelNames) {
        String path = "attenuationModels/" + earthModelName;
        fileReaderMap.put(earthModelName, StandardAsciiTravelTime1dFileReader.from(path));
      }
      fileReaderMap = Collections.unmodifiableMap(fileReaderMap);

      PluginRegistry pluginRegistry = PluginRegistry.getRegistry();
      pluginRegistry.loadAndRegister();

      // load uncertainty plugin
      attenuationUncertaintyPlugin = pluginRegistry.lookup(
          PluginInfo.from(
              "standardAsciiAttenuation1dUncertaintyPlugin",
              "1.0.0"),
          Attenuation1dUncertaintyPlugin.class).orElseThrow(
          () -> new IllegalStateException(
              "Plugin standardAsciiAttenuation1dUncertaintyPlugin (version 1.0.0) not found"));

      attenuationUncertaintyPlugin.initialize(earthModelNames);
    } else {
      throw new IOException("initialize() method has already been called.");
    }
  }

  @Override
  public Optional<double[]> getDepthModelingErrors(String earthModelName, PhaseType phase) {
    Optional<double[]> optionalModelingErrorDepths = fileReaderMap.get(earthModelName)
        .getModelingErrorDepthsForPhase(phase);

    if (!optionalModelingErrorDepths.isPresent() && !Objects
        .isNull(this.attenuationUncertaintyPlugin)) {

      optionalModelingErrorDepths = Optional.of(this.attenuationUncertaintyPlugin
          .getDepthsKm(earthModelName, phase));
    }

    return optionalModelingErrorDepths;
  }

  @Override
  public Optional<double[]> getDistanceModelingErrors(String earthModelName, PhaseType phase) {
    Optional<double[]> optionalModelingErrorDistances = fileReaderMap.get(earthModelName)
        .getModelingErrorDistancesForPhase(phase);

    if (!optionalModelingErrorDistances.isPresent() && !Objects
        .isNull(this.attenuationUncertaintyPlugin)) {

      optionalModelingErrorDistances = Optional
          .of(this.attenuationUncertaintyPlugin.getDistancesDeg(earthModelName, phase));
    }

    return optionalModelingErrorDistances;
  }

  @Override
  public Optional<double[][]> getValueModelingErrors(String earthModelName, PhaseType phase) {
    Optional<double[][]> optionalModelingErrorValues = fileReaderMap.get(earthModelName)
        .getModelingErrorValuesForPhase(phase);

    if (!optionalModelingErrorValues.isPresent() && !Objects
        .isNull(this.attenuationUncertaintyPlugin)) {

      optionalModelingErrorValues = Optional
          .of(this.attenuationUncertaintyPlugin.getValues(earthModelName, phase));
    }

    return optionalModelingErrorValues;
  }
}
