package gms.core.eventlocation.plugins.pluginutils.seedgeneration;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import java.util.Map;

/**
 * Interface for generating location seeds for Geigers
 */
public interface SeedGenerator {

  /**
   * Generate a seed location from the information contained in a collection of signal detections
   * and stations
   *
   * @param defaultSeedLocation location to return if provided signal detections cant be used to
   * find a seed
   * @param observationStationMap Map of signal detection hypotheses to the station that are
   * associated with
   */
  EventLocation generate(EventLocation defaultSeedLocation, Map<SignalDetectionHypothesis, ReferenceStation>
      observationStationMap);
}
