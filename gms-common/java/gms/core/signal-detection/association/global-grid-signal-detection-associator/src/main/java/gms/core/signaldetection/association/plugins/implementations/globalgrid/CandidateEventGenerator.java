package gms.core.signaldetection.association.plugins.implementations.globalgrid;

import gms.core.signaldetection.association.CandidateEvent;
import gms.core.signaldetection.association.MissingFeatureMeasurementException;
import gms.core.signaldetection.association.MissingNodeStationException;
import gms.core.signaldetection.association.UnexpectedPhasesException;
import gms.core.signaldetection.association.plugins.SdhStationAssociation;
import gms.core.signaldetection.association.plugins.SignalDetectionAssociatorDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.GridNode;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.NodeStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.association.commonobjects.PhaseInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.utilities.geomath.GeoMath;
import gms.shared.utilities.javautilities.generation.AbstractGenerator;
import gms.shared.utilities.javautilities.generation.GenerationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generator for sets of candidate events.
 */
public class CandidateEventGenerator extends AbstractGenerator<Set<CandidateEvent>> {

  private double sigmaTime = Double.NaN;
  private Map<UUID, Set<GridNode>> gridNodeMap;
  private List<SdhStationAssociation> sdhStationAssociations;
  private static Logger logger = LoggerFactory.getLogger(CandidateEventGenerator.class);
  private SignalDetectionAssociatorDefinition definition;

  /**
   * Constructor -- all parameters must be set via the fluent setter methods before invoking {@code
   * generate()} or IllegalStateExceptions will result.
   */
  public CandidateEventGenerator() {
  }

  /**
   * Generates CandidateEvent Set
   *
   * @throws GenerationException if failure occurs
   */
  @Override
  public Set<CandidateEvent> doGenerate() throws GenerationException {

    Set<CandidateEvent> driverSet = new HashSet<>();

    try {
      this.gridNodeMap.keySet().stream()
          .forEach((uuid) -> logger.info(String.format("UUID: %s", uuid.toString())));
      this.sdhStationAssociations.forEach((obj) -> logger.info(String
          .format("SDH association station %s",
              obj.getReferenceStation().getVersionId().toString())));
      for (SdhStationAssociation sdhsa : this.sdhStationAssociations) {
        Set<GridNode> gridNodeSet = this.gridNodeMap
            .get(sdhsa.getReferenceStation().getVersionId());
        if (gridNodeSet == null) {
          logger.info(String.format("Station %s not found in GridNodeMap",
              sdhsa.getReferenceStation().getVersionId()));
        } else {
          for (GridNode gridNode : gridNodeSet) {
            // TODO: need to add a check for phase of SDH
            if (CandidateEvent.passesSlownessConstraintCheck(
                sdhsa.getReferenceStation(),
                sdhsa.getSignalDetectionHypothesis(),
                definition.getSigmaSlowness(),
                gridNode)) {
              driverSet.add(CandidateEvent.from(
                  sdhsa.getReferenceStation().getVersionId(),
                  gridNode,
                  sdhsa.getSignalDetectionHypothesis(),
                  sigmaTime));
            }
          }
        }
      }
    } catch (MissingNodeStationException |
        UnexpectedPhasesException |
        MissingFeatureMeasurementException t) {
      // Catch everything and wrap in a generation exception.
      throw new GenerationException("error generating candidate events", t);
    }

    // Only return a non-null driverSet when it's not empty. In that case,
    // generate() returns an empty optional to clue the caller that no candidate events
    // were generated.
    return driverSet.size() > 0 ? driverSet : null;
  }

  public SignalDetectionAssociatorDefinition definition() { return this.definition; }

  public CandidateEventGenerator definition(SignalDetectionAssociatorDefinition definition) {
    Objects.requireNonNull(definition, "Null SignalDetectionAssociatorDefinition");
    this.definition = definition;
    return this;
  }

  public double sigmaTime() {
    return sigmaTime;
  }

  public CandidateEventGenerator sigmaTime(double sigmaTime) {
    if (Double.isNaN(sigmaTime) || sigmaTime < 0.0) {
      throw new IllegalArgumentException("sigmaTime must be >= 0.0: " + sigmaTime);
    }
    this.sigmaTime = sigmaTime;
    return this;
  }

  public Map<UUID, Set<GridNode>> gridNodeMap() {
    return gridNodeMap;
  }

  public CandidateEventGenerator gridNodeMap(
      Map<UUID, Set<GridNode>> gridNodeMap) {
    Validate.notEmpty(gridNodeMap, "Null or empty gridNodeMap");
    this.gridNodeMap = gridNodeMap;
    return this;
  }

  public List<SdhStationAssociation> sdhStationAssociations() {
    return sdhStationAssociations;
  }

  public CandidateEventGenerator sdhStationAssociations(
      List<SdhStationAssociation> sdhStationAssociations) {
    Validate.notEmpty(sdhStationAssociations, "Null or empty sdhStationAssociations");
    this.sdhStationAssociations = sdhStationAssociations;
    return this;
  }

  @Override
  public String[] missingParameterNames() {
    List<String> missingParamNames = new ArrayList<>();
    if (Double.isNaN(this.sigmaTime)) {
      missingParamNames.add("sigmaTime");
    }
    if (this.gridNodeMap == null) {
      missingParamNames.add("gridNodeMap");
    }
    if (this.sdhStationAssociations == null) {
      missingParamNames.add("sdhStationAssociations");
    }
    return missingParamNames.toArray(new String[missingParamNames.size()]);
  }
}
