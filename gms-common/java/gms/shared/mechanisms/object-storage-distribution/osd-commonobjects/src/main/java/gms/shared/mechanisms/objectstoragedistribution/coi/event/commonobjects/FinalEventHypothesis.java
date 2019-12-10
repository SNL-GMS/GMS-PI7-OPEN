package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class FinalEventHypothesis {

  public abstract EventHypothesis getEventHypothesis();

  public static FinalEventHypothesis from(EventHypothesis eventHypothesis) {
    return new AutoValue_FinalEventHypothesis(eventHypothesis);
  }
}
