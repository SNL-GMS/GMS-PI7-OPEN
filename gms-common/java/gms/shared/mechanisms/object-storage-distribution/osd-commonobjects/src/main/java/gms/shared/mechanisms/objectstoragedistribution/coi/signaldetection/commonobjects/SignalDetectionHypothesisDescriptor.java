package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.UUID;

@AutoValue
public abstract class SignalDetectionHypothesisDescriptor {

  public abstract SignalDetectionHypothesis getSignalDetectionHypothesis();

  public abstract UUID getStationId();

  @JsonCreator
  public static SignalDetectionHypothesisDescriptor from(
      @JsonProperty("signalDetectionHypothesis") SignalDetectionHypothesis signalDetectionHypothesis,
      @JsonProperty("stationId") UUID stationId) {
    return new AutoValue_SignalDetectionHypothesisDescriptor(signalDetectionHypothesis, stationId);
  }
}
