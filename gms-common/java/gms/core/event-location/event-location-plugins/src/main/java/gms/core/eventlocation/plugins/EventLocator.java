package gms.core.eventlocation.plugins;

import gms.core.eventlocation.plugins.exceptions.TooManyRestraintsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import java.util.List;
import java.util.Optional;


// TODO: Idea here is to seperate "being a plugin" from "being an event locator". Probably want
// TODO:    to move this interface to some other location at some point.
public interface EventLocator {

  void initialize();

  List<LocationSolution> locate(Optional<PreferredLocationSolution> start,
      List<SignalDetectionHypothesis> observations, List<ReferenceStation> stations,
      EventLocationDefinition parameters) throws TooManyRestraintsException;
}
