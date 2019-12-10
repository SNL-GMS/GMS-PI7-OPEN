package gms.core.signaldetection.association.plugins.implementations.globalgrid;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.GridNode;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.NodeStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
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
 * Instances of {@code GridNodeGenerator} are used to instantiate {@code GridNode} instances.
 * This class uses the builder pattern. Think of it as a builder for {@code GridNode} instances.
 * One instance may be used many times to generate
 * grid nodes. The setter methods use the fluent pattern.
 * <p>
 *   The setter methods all return a reference to the {@code GridNodeData} instance so
 *   they can be chained. All the fields of {@code GridNodeGenerator} instance begin with invalid
 *   values that indicate that they have not been set. The setter methods all throw exceptions
 *   unless the arguments are valid. So once all parameters have been set successfully once,
 *   the setters prevent parameters from ever becoming invalid.
 *   This is to facilitate setting up an instance with unchanging parameters outside of a loop
 *   and then, for each loop iteration, setting only the parameters that vary.
 * </p>
 */
public class GridNodeGenerator extends AbstractGenerator<GridNode> {

  private List<ReferenceStation> referenceStations;

  // Similar builder for node stations.
  private final NodeStationGenerator nodeStationGenerator = new NodeStationGenerator();

  /**
   * Public constructor. Before {@code generateGridNode()} may be called without generating
   * an {@code IllegalStateException}, all required parameters must be set via the
   * fluent setter methods.
   */
  public GridNodeGenerator() {}

  @Override
  protected GridNode doGenerate() throws GenerationException {

    try {
      // create Set of NodeStations
      SortedSet<NodeStation> nodeStations = generateNodeStations();

      // Generate and return the grid node wrapped by an optional.
      return nodeStations.isEmpty() ? null :
          GridNode.from(
              UUID.randomUUID(),
              gridPointLatDegrees(),
              gridPointLonDegrees(),
              gridPointDepthKm(),
              nodeStationGenerator.gridCylinderHeightKm(),
              nodeStations
          );

    } catch (GenerationException e) {
      // Rethrow any that propagate from below.
      throw e;
    } catch (Throwable t) {
      // Keep other problems from leaking through exception as the
      // cause of a generation exception. This probably indicates a problem in
      // the code.
      throw new GenerationException("unexpected error generating a grid node", t);
    }
  }

  public double gridPointLatDegrees() {
    return nodeStationGenerator.gridPointLatDegrees();
  }

  public GridNodeGenerator gridPointLatDegrees(double centerLatitudeDegrees) {
    nodeStationGenerator.gridPointLatDegrees(centerLatitudeDegrees);
    return this;
  }

  public double gridPointLonDegrees() {
    return nodeStationGenerator.gridPointLonDegrees();
  }

  public GridNodeGenerator gridPointLonDegrees(double centerLongitudeDegrees) {
    nodeStationGenerator.gridPointLonDegrees(centerLongitudeDegrees);
    return this;
  }

  public double gridPointDepthKm() {
    return nodeStationGenerator.gridPointDepthKm();
  }

  public GridNodeGenerator gridPointDepthKm(double centerDepthKm) {
    nodeStationGenerator.gridPointDepthKm(centerDepthKm);
    return this;
  }

  public SignalFeaturePredictionUtility predictionUtility() {
    return nodeStationGenerator.predictionUtility();
  }

  public GridNodeGenerator predictionUtility(
      SignalFeaturePredictionUtility predictionUtility) {
    nodeStationGenerator.predictionUtility(predictionUtility);
    return this;
  }

  public String travelTimePredictionEarthModel() {
    return nodeStationGenerator.travelTimePredictionEarthModel();
  }

  public GridNodeGenerator travelTimePredictionEarthModel(String travelTimePredictionEarthModel) {
    nodeStationGenerator.travelTimePredictionEarthModel(travelTimePredictionEarthModel);
    return this;
  }

  public String magnitudeAttenuationPredictionEarthModel() {
    return nodeStationGenerator.magnitudeAttenuationPredictionEarthModel();
  }

  public GridNodeGenerator magnitudeAttenuationPredictionEarthModel(
      String magnitudeAttenuationPredictionEarthModel) {
    nodeStationGenerator.magnitudeAttenuationPredictionEarthModel(
        magnitudeAttenuationPredictionEarthModel);
    return this;
  }

  public double minimumMagnitude() {
    return nodeStationGenerator.minimumMagnitude();
  }

  public GridNodeGenerator minimumMagnitude(double minimumMagnitude) {
    nodeStationGenerator.minimumMagnitude(minimumMagnitude);
    return this;
  }

  public double gridCylinderRadiusDegrees() {
    return nodeStationGenerator.gridCylinderRadiusDegrees();
  }

  public GridNodeGenerator gridCylinderRadiusDegrees(double gridCylinderRadiusDegrees) {
    nodeStationGenerator.gridCylinderRadiusDegrees(gridCylinderRadiusDegrees);
    return this;
  }

  public double gridCylinderHeightKm() {
    return nodeStationGenerator.gridCylinderHeightKm();
  }

  public GridNodeGenerator gridCylinderHeightKm(double gridCylinderHeightKm) {
    nodeStationGenerator.gridCylinderHeightKm(gridCylinderHeightKm);
    return this;
  }

  public List<ReferenceStation> referenceStations() {
    return referenceStations;
  }

  public GridNodeGenerator referenceStations(
      List<ReferenceStation> referenceStations) {
    Validate.notEmpty(referenceStations, "Null or empty referenceStations");
    this.referenceStations = referenceStations;
    // So its missingParameterNames() method won't complain.
    nodeStationGenerator.referenceStation(referenceStations.get(0));
    return this;
  }

  public List<PhaseType> phaseTypes() {
    return nodeStationGenerator.phaseTypes();
  }

  public GridNodeGenerator phaseTypes(
      List<PhaseType> phaseTypes) {
    nodeStationGenerator.phaseTypes(phaseTypes);
    return this;
  }

  /**
   * Called to generation the set of node stations.
   * @return
   */
  private SortedSet<NodeStation> generateNodeStations() throws GenerationException {
    // create Set of NodeStations
    SortedSet<NodeStation> nodeStations = new TreeSet<>();
    for (ReferenceStation referenceStation : referenceStations) {
      nodeStationGenerator.referenceStation(referenceStation).generate()
          .ifPresent(nodeStations::add);
    }
    return nodeStations;
  }

  /**
   * Called by generateGridNode() to ensure all parameters have been set.
   * @return an array of names of unset parameters
   */
  public String[] missingParameterNames() {
    List<String> errorMessages = new ArrayList<>();
    if (referenceStations == null) {
      errorMessages.add("referencesStations");
    }
    String[] nodeGeneratorMissingParams = nodeStationGenerator.missingParameterNames();
    if (nodeGeneratorMissingParams.length > 0) {
      errorMessages.addAll(Arrays.asList(nodeGeneratorMissingParams));
    }
    return errorMessages.toArray(new String[errorMessages.size()]);
  }

}
