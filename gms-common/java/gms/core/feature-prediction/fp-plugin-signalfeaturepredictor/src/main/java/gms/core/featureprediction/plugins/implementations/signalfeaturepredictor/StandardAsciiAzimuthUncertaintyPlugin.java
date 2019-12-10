package gms.core.featureprediction.plugins.implementations.signalfeaturepredictor;

import gms.core.featureprediction.plugins.AzimuthUncertaintyPlugin;
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
 * Azimuth uncertainty (i.e., standard deviation).  Uncertainty values are interpolated (bicubic
 * spline interpolation) from table of uncertainty values for a specific phase of a specific 1D
 * earth model.  This object reads and stores uncertainty tables for all the phases of all 1D earth
 * models.
 */
@Name("standardAsciiAzimuthUncertaintyPlugin")
@Version("1.0.0")
public class StandardAsciiAzimuthUncertaintyPlugin implements AzimuthUncertaintyPlugin {

  // unmodifiable map of earth model names to AzimuthUncertaintyModel1D objects
  private Map<String, AzimuthUncertaintyModel1D> azimuthUncertaintyModels1D;

  /**
   * Initialize the class.  Must be called before calling other methods.
   *
   * @param earthModelNames names of the earth models to load
   * @throws IOException if any models can not be loaded
   */
  public void initialize(Set<String> earthModelNames) throws IOException {
    Map<String, AzimuthUncertaintyModel1D> azimuthUncertaintyMap = new HashMap<>();

    for (String earthModelName : earthModelNames) {
      String earthModelFolderLocation = "azimuthuncertainties/" + earthModelName;
      azimuthUncertaintyMap
          .put(earthModelName, AzimuthUncertaintyModel1D.from(earthModelFolderLocation));
    }

    this.azimuthUncertaintyModels1D = Collections.unmodifiableMap(azimuthUncertaintyMap);
  }

  /**
   * Retrieves names of available 1D earth models
   *
   * @return set of names of models
   */
  @Override
  public Set<String> getEarthModelNames() {
    return this.azimuthUncertaintyModels1D.keySet();
  }

  /**
   * Retreives phase types available for a specific 1D earth model
   *
   * @param modelName name of 1D earth model
   * @return set of phase types
   */
  @Override
  public Set<PhaseType> getPhaseTypes(String modelName) {
    return this.azimuthUncertaintyModels1D.get(modelName).getPhaseTypes();
  }

  /**
   * Retrieves the distance values for rows in the uncertainty table
   *
   * @param p phase type
   * @return array of distance values in degrees
   */
  @Override
  public double[] getDistancesDeg(String modelName, PhaseType p) {
    return this.azimuthUncertaintyModels1D.get(modelName).getDistanceDegreesForPhase(p);
  }

  /**
   * Retrieves uncertainty values from table. Should contain just one column of data.
   *
   * @param modelName earth model name
   * @param p phase type
   * @return array of uncertainty data
   */
  @Override
  public double[] getValues(String modelName, PhaseType p) {
    return this.azimuthUncertaintyModels1D.get(modelName).getAzimuthUncertaintyForPhase(p);
  }

  @Override
  public Optional<double[]> getDistanceModelingErrors(String earthModelName, PhaseType phase) {
    throw new RuntimeException(
        "getDistanceModelingErrors not implemented for " + this.getClass().getName());
  }

  @Override
  public Optional<double[]> getValueModelingErrors(String earthModelName, PhaseType phase) {
    throw new RuntimeException(
        "getValueModelingErrors not implemented for " + this.getClass().getName());

  }

}
