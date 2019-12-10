package gms.core.eventlocation.plugins.pluginutils.seedgeneration;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/**
 * Implements simple seed generator - returns location of the earliest arrival time.
 */
public class SimpleSeedGenerator implements SeedGenerator {

  @Override
  public EventLocation generate(EventLocation defaultSeedLocation,
      Map<SignalDetectionHypothesis, ReferenceStation> observationStationMap) {
    //find the first arrival of ALL SDHs

    Optional<SignalDetectionHypothesis> firstArrivalHypothesesOptional =
        observationStationMap.keySet()
        .stream()
        .filter(signalDetectionHypothesis -> signalDetectionHypothesis
            .getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME).isPresent())
        .sorted(
            Comparator.comparing(
                sdh -> sdh.getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME).get()
                    .getMeasurementValue().getValue()))
        .findFirst();

    SignalDetectionHypothesis firstArrivalHypothesis;

    if (firstArrivalHypothesesOptional.isPresent()) {
      firstArrivalHypothesis = firstArrivalHypothesesOptional.get();
    } else {
      return defaultSeedLocation;
    }

    ReferenceStation firstArrivalStation = observationStationMap.get(firstArrivalHypothesis);

    return EventLocation.from(
        firstArrivalStation.getLatitude(),
        firstArrivalStation.getLongitude(),
        //TODO: Is it right to set this to this?
        0.0,
        //TODO is this the time touse?
        firstArrivalHypothesis.getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME).get()
            .getMeasurementValue().getValue());
  }
}
