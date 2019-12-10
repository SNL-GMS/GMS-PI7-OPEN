package gms.core.signaldetection.association.plugins;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.utilities.signalfeaturepredictionutility.SignalFeaturePredictionUtility;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * Interface for core functionality of associator.
 *
 */
public interface SignalDetectionAssociator {

  /**
   * initializes the associator
   */
  void initialize(String modelFilePath,
      Set<ReferenceStation> stations,
      List<SignalFeaturePredictionUtility> predictionUtilities,
      SignalDetectionAssociatorDefinition definition);

  /**
   * Builds events using a geographic grid-based approach to parameterize the Event creation process.
   *
   * @param eventHypotheses Collection of event hypotheses to associate signal detections to
   * @param sdhStationAssociations Collection of Signal Detection Hypotheses associated with the
   * station that detected them.
   * @param definition Set of parameters
   * @return Pair containing new/updated event hypotheses and updated signal detection hypotheses
   */
  Pair<Set<SignalDetectionHypothesis>, Set<EventHypothesis>> associate(
      Collection<EventHypothesis> eventHypotheses,
      Collection<SdhStationAssociation> sdhStationAssociations,
      SignalDetectionAssociatorDefinition definition
  );

}
