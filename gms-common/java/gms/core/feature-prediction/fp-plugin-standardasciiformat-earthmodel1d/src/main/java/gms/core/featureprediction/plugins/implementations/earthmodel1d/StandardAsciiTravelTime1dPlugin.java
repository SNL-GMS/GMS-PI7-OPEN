package gms.core.featureprediction.plugins.implementations.earthmodel1d;

import gms.core.featureprediction.plugins.TravelTime1dPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.pluginregistry.Name;
import gms.shared.mechanisms.pluginregistry.Version;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Time travel model/plugin which reads time travel data from the "standard" format Ascii/flatfiles.
 */
@Name("standardAsciiTravelTime1dPlugin")
@Version("1.0.0")
public class StandardAsciiTravelTime1dPlugin implements TravelTime1dPlugin {

  protected Map<String, StandardAsciiTravelTime1dFileReader> fileReaderMap = null;

  @Override
  public void initialize(Set<String> earthModelNames) throws IOException {
    if (fileReaderMap == null) {
      fileReaderMap = new HashMap<>();
      for (String earthModelName : earthModelNames) {
        String path = "earthmodels1d/" + earthModelName;
        fileReaderMap.put(earthModelName, StandardAsciiTravelTime1dFileReader.from(path));
      }
      fileReaderMap = Collections.unmodifiableMap(fileReaderMap);
    } else {
      throw new IOException("initialize() method has already been called.");
    }
  }

  @Override
  public Set<String> getEarthModelNames() {
    return fileReaderMap.keySet();
  }

  @Override
  public Set<PhaseType> getPhaseTypes(String earthModelName) {
    return fileReaderMap.get(earthModelName).getPhaseTypes();
  }

  @Override
  public double[] getDepthsKm(String earthModelName, PhaseType phase) {
    return fileReaderMap.get(earthModelName).getDepthKmForPhase(phase);
  }

  @Override
  public double[] getDistancesDeg(String earthModelName, PhaseType phase) {
    return fileReaderMap.get(earthModelName).getAngleDegreesForPhase(phase);
  }

  @Override
  public double[][] getValues(String earthModelName, PhaseType phase) {
    return fileReaderMap.get(earthModelName).getTravelTimesForPhase(phase);
  }

  @Override
  public Optional<double[]> getDepthModelingErrors(String earthModelName, PhaseType phase) {
    return fileReaderMap.get(earthModelName).getModelingErrorDepthsForPhase(phase);
  }

  @Override
  public Optional<double[]> getDistanceModelingErrors(String earthModelName, PhaseType phase) {
    return fileReaderMap.get(earthModelName).getModelingErrorDistancesForPhase(phase);
  }

  @Override
  public Optional<double[][]> getValueModelingErrors(String earthModelName, PhaseType phase) {
    return fileReaderMap.get(earthModelName).getModelingErrorValuesForPhase(phase);
  }
}