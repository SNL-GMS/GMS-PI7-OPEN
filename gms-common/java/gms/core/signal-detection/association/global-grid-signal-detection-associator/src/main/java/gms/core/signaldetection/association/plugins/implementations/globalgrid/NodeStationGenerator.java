package gms.core.signaldetection.association.plugins.implementations.globalgrid;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.NodeStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.PhaseInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.utilities.geomath.GeoMath;
import gms.shared.utilities.javautilities.generation.AbstractGenerator;
import gms.shared.utilities.javautilities.generation.GenerationException;
import gms.shared.utilities.signalfeaturepredictionutility.SignalFeaturePredictionUtility;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

/**
 * A {@code NodeStationGenerator} is a builder for {@code NodeStation}s. After obtaining a
 * new instance via the no-arg constructor, all fluent-styled setter methods must be
 * called to set required parameters prior to calling {@code generateNodeStation()}.
 */
public class NodeStationGenerator extends AbstractGenerator<NodeStation> {

  // A builder for the PhaseInfo instances.
  private final PhaseInfoGenerator phaseInfoGenerator = new PhaseInfoGenerator();

  private List<PhaseType> phaseTypes;

  /**
   * Public constructor. Until all parameters have been set via, the fluent-styled
   * setter methods, calls to {@code generateNodeStation()} result in an
   * {@code IllegalStateException}
   */
  public NodeStationGenerator() {
    // So phaseInfoGenerator never reports phaseType as not being set. This class sets
    // the phaseType of phaseInfoGenerator from the elements in phaseTypes.
    phaseInfoGenerator.phaseType(PhaseType.P);
  }

  @Override
  protected NodeStation doGenerate() throws GenerationException {

    try {

      // create PhaseInfo list
      SortedSet<PhaseInfo> phaseInfos = new TreeSet<>();
      for (PhaseType phaseType : phaseTypes) {
        phaseInfoGenerator.phaseType(phaseType)
            .generate()
            .ifPresent(phaseInfos::add);
      }

      // compute great circle distance from grid point to seismic station
      double degreesToGridPoint = GeoMath.greatCircleAngularSeparation(
          phaseInfoGenerator.referenceStation().getLatitude(),
          phaseInfoGenerator.referenceStation().getLongitude(),
          phaseInfoGenerator.gridPointLatDegrees(),
          phaseInfoGenerator.gridPointLonDegrees());

      // return empty Optional iff no PhaseInfo objects were created
      if (!phaseInfos.isEmpty()) {
        return NodeStation.from(UUID.randomUUID(),
            phaseInfoGenerator.referenceStation().getVersionId(),
            degreesToGridPoint,
            phaseInfos
        );
      } else {
        return null;
      }

    } catch (GenerationException e) {
      // Rethrow generation exceptions from the phase info generator.
      throw e;
    } catch (Throwable t) {
      // Catch-all so nothing leaks through except wrapped as the cause
      // of a generation exception.
      throw new GenerationException("unexpected error generating nodestation", t);
    }
  }

  // Setter and getter methods using the convention for the builder pattern.

  public SignalFeaturePredictionUtility predictionUtility() {
    return phaseInfoGenerator.predictionUtility();
  }

  public NodeStationGenerator predictionUtility(
      SignalFeaturePredictionUtility predictionUtility) {
    phaseInfoGenerator.predictionUtility(predictionUtility);
    return this;
  }

  public String travelTimePredictionEarthModel() {
    return phaseInfoGenerator.travelTimePredictionEarthModel();
  }

  public NodeStationGenerator travelTimePredictionEarthModel(String travelTimePredictionEarthModel) {
    phaseInfoGenerator.travelTimePredictionEarthModel(travelTimePredictionEarthModel);
    return this;
  }

  public String magnitudeAttenuationPredictionEarthModel() {
    return phaseInfoGenerator.magnitudeAttenuationPredictionEarthModel();
  }

  public NodeStationGenerator magnitudeAttenuationPredictionEarthModel(
      String magnitudeAttenuationPredictionEarthModel) {
    phaseInfoGenerator.magnitudeAttenuationPredictionEarthModel(
        magnitudeAttenuationPredictionEarthModel);
    return this;
  }

  public double gridCylinderRadiusDegrees() {
    return phaseInfoGenerator.gridCylinderRadiusDegrees();
  }

  public NodeStationGenerator gridCylinderRadiusDegrees(double gridCylinderRadiusDegrees) {
    phaseInfoGenerator.gridCylinderRadiusDegrees(gridCylinderRadiusDegrees);
    return this;
  }

  public double gridCylinderHeightKm() {
    return phaseInfoGenerator.gridCylinderHeightKm();
  }

  public NodeStationGenerator gridCylinderHeightKm(double gridCylinderHeightKm) {
    phaseInfoGenerator.gridCylinderHeightKm(gridCylinderHeightKm);
    return this;
  }

  public List<PhaseType> phaseTypes() {
    return phaseTypes;
  }

  public NodeStationGenerator phaseTypes(List<PhaseType> phaseTypes) {
    Validate.notEmpty(phaseTypes, "Null or empty phaseTypes");
    this.phaseTypes = new ArrayList<>(phaseTypes);
    phaseInfoGenerator.phaseType(phaseTypes.get(0));
    return this;
  }

  public double gridPointLatDegrees() {
    return phaseInfoGenerator.gridPointLatDegrees();
  }

  public NodeStationGenerator gridPointLatDegrees(double gridPointLatDegrees) {
    phaseInfoGenerator.gridPointLatDegrees(gridPointLatDegrees);
    return this;
  }

  public double gridPointLonDegrees() {
    return phaseInfoGenerator.gridPointLonDegrees();
  }

  public NodeStationGenerator gridPointLonDegrees(double gridPointLonDegrees) {
    phaseInfoGenerator.gridPointLonDegrees(gridPointLonDegrees);
    return this;
  }

  public double gridPointDepthKm() {
    return phaseInfoGenerator.gridPointDepthKm();
  }

  public NodeStationGenerator gridPointDepthKm(double gridPointDepthKm) {
    phaseInfoGenerator.gridPointDepthKm(gridPointDepthKm);
    return this;
  }

  public double minimumMagnitude() {
    return phaseInfoGenerator.minimumMagnitude();
  }

  public NodeStationGenerator minimumMagnitude(double minimumMagnitude) {
    phaseInfoGenerator.minimumMagnitude(minimumMagnitude);
    return this;
  }

  public ReferenceStation referenceStation() {
    return phaseInfoGenerator.referenceStation();
  }

  public NodeStationGenerator referenceStation(ReferenceStation referenceStation) {
    phaseInfoGenerator.referenceStation(referenceStation);
    return this;
  }

  @Override
  public String[] missingParameterNames() {
    List<String> missingParams = new ArrayList<>();
    String[] phaseInfoMissingParams = phaseInfoGenerator.missingParameterNames();
    if (phaseInfoMissingParams.length > 0) {
      missingParams.addAll(Arrays.asList(phaseInfoMissingParams));
    }
    if (phaseTypes == null) {
      missingParams.add("phaseTypes");
    }
    return missingParams.toArray(new String[missingParams.size()]);
  }

}
