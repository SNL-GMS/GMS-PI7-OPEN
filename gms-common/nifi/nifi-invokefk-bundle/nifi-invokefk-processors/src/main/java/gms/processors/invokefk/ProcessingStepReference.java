package gms.processors.invokefk;

import java.util.UUID;

public class ProcessingStepReference {

  private final UUID processingStageIntervalId;
  private final UUID processingSequenceIntervalId;
  private final UUID processingStepId;

  public ProcessingStepReference(UUID processingStageIntervalId,
      UUID processingSequenceIntervalId, UUID processingStepId) {
    this.processingStageIntervalId = processingStageIntervalId;
    this.processingSequenceIntervalId = processingSequenceIntervalId;
    this.processingStepId = processingStepId;
  }

  public UUID getProcessingStageIntervalId() {
    return processingStageIntervalId;
  }

  public UUID getProcessingSequenceIntervalId() {
    return processingSequenceIntervalId;
  }

  public UUID getProcessingStepId() {
    return processingStepId;
  }

  @Override
  public String toString() {
    return "ProcessingStepReference{" +
        "processingStageIntervalId=" + processingStageIntervalId +
        ", processingSequenceIntervalId=" + processingSequenceIntervalId +
        ", processingStepId=" + processingStepId +
        '}';
  }
}

