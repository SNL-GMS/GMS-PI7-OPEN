package gms.core.featureprediction.plugins.implementations.ellipticitycorrection;

import gms.core.featureprediction.plugins.DziewanskiGilbertEllipticityCorrectionPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.pluginregistry.Name;
import gms.shared.mechanisms.pluginregistry.Version;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.tuple.Triple;

/**
 * Cointains ellipticity correction values for phases in one earth model.  The ellipticity
 * corrections are read in from a file, and only contains values for the phases provided in that
 * file.
 */
@Name("dziewonskiGilbertEllipticityModel")
@Version("1.0.0")
public class StandardAsciiDgEllipticityCorrectionPlugin implements
    DziewanskiGilbertEllipticityCorrectionPlugin {

  private DziewonskiGilbertEllipticityModel1dDelegate dziewonskiGilbertEllipticityModel1dDelegate
      = null;

  @Override
  // TODO: Do we need to accept a Set<String> at all???
  public void initialize(Set<String> earthModelNames) throws IOException {
    if (dziewonskiGilbertEllipticityModel1dDelegate == null) {
      dziewonskiGilbertEllipticityModel1dDelegate = new DziewonskiGilbertEllipticityModel1dDelegate();
      dziewonskiGilbertEllipticityModel1dDelegate.initialize(earthModelNames);
    } else {
      throw new IOException("Initialize() method has already been called.");
    }
  }

  @Override
  public Set<String> getEarthModelNames() {
    return dziewonskiGilbertEllipticityModel1dDelegate.getModelNames();
  }

  @Override
  public Set<PhaseType> getPhaseTypes(String modelName) {
    return dziewonskiGilbertEllipticityModel1dDelegate.getPhaseTypesForModelName(modelName);
  }

  @Override
  public double[] getDepthsKm(String modelName, PhaseType p) {
    return dziewonskiGilbertEllipticityModel1dDelegate.getDepthKmForModelPhase(modelName, p);
  }

  @Override
  public double[] getDistancesDeg(String modelName, PhaseType p) {
    return dziewonskiGilbertEllipticityModel1dDelegate.getAngleDegreesForModelPhase(modelName, p);
  }

  @Override
  public Triple<double[][], double[][], double[][]> getValues(String modelName,
      PhaseType p) {
    return dziewonskiGilbertEllipticityModel1dDelegate.getTravelTimesForModelPhase(modelName, p);
  }

  @Override
  public Optional<double[]> getDistanceModelingErrors(String modelName, PhaseType p) {
    return dziewonskiGilbertEllipticityModel1dDelegate.getModelingErrorDistancesForModelPhase(modelName, p);
  }

  @Override
  public Optional<double[]> getDepthModelingErrors(String modelName, PhaseType p) {
    return dziewonskiGilbertEllipticityModel1dDelegate.getModelingErrorDepthsForModelPhase(modelName, p);
  }

  @Override
  public Optional<Triple<double[][], double[][], double[][]>> getValueModelingErrors(
      String modelName, PhaseType p) {
    return dziewonskiGilbertEllipticityModel1dDelegate.getModelingErrorValuesForModelPhase(modelName, p);
  }


}
