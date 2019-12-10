package gms.core.signaldetection.association.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@AutoValue
public abstract class SignalDetectionAssociationResult {

  public abstract Set<SignalDetectionHypothesis> getSignalDetections();

  public abstract Set<EventHypothesis> getEvents();


  @JsonCreator
  public static SignalDetectionAssociationResult from(
      @JsonProperty("signalDetections") Set<SignalDetectionHypothesis> detections,
      @JsonProperty("events") Set<EventHypothesis> events
  ) {
    // Signal Detections can be non-null.
    Objects.requireNonNull(detections, "detections can not be a null object");

    // Events should also be non-null as well
    Objects.requireNonNull(events, "events can not be a null object");
    return new AutoValue_SignalDetectionAssociationResult(
        Collections.unmodifiableSet(detections),
        Collections.unmodifiableSet(events));
  }
}
