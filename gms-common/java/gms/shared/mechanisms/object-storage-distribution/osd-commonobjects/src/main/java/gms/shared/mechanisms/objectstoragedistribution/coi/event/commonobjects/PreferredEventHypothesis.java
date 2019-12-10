package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import com.google.auto.value.AutoValue;
import java.util.UUID;

@AutoValue
public abstract class PreferredEventHypothesis {

  public abstract UUID getProcessingStageId();
  public abstract EventHypothesis getEventHypothesis();

  /**
   * Create an instance of PreferredEventHypothesis
   *
   * @param processingStageId The ProcessingStage, not null
   * @param eventHypothesis the single EventHypothesis that is designated as the
   * PreferredEventHypothesis for the Event, across all processing stages. Not null.
   */
  public static PreferredEventHypothesis from(UUID processingStageId,
      EventHypothesis eventHypothesis) {
    return new AutoValue_PreferredEventHypothesis(processingStageId, eventHypothesis);
  }
}
