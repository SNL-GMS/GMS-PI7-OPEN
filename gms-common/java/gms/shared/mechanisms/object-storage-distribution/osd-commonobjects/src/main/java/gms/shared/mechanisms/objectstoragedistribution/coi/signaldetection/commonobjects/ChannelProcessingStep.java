package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ChannelProcessingStep {

  private final UUID id;
  private final String name;
  private final ChannelProcessingStepType type;
  private final Instant actualChangeTime;
  private final Instant systemChangeTime;
  private final String status;
  private final String comment;
  private final UUID processingGroupId;

  public enum ChannelProcessingStepType {
    RAW, WAVEFORM_QC, FILTER, COHERENT_BEAM, DETECTOR
  }

  public static ChannelProcessingStep create(UUID processingGroupId, String name,
      ChannelProcessingStepType type, Instant actualChangeTime, Instant systemChangeTime,
      String status, String comment) {

    Objects.requireNonNull(name,
        "ChannelProcessingStep expects non-null name.");
    Objects.requireNonNull(type,
        "ChannelProcessingStep expects non-null channel processing step type.");
    Objects.requireNonNull(actualChangeTime,
        "ChannelProcessingStep expects non-null actual change time.");
    Objects.requireNonNull(systemChangeTime,
        "ChannelProcessingStep expects non-null system change time.");
    Objects.requireNonNull(status, "ChannelProcessingStep expects non-null status.");
    Objects.requireNonNull(comment, "ChannelProcessingStep expects non-null comment.");
    Objects.requireNonNull(processingGroupId,
        "ChannelProcessingStep expects non-null channel processing processing group id.");

    return new ChannelProcessingStep(UUID.randomUUID(), name, type, actualChangeTime,
        systemChangeTime, status, comment, processingGroupId);
  }

  private ChannelProcessingStep(UUID id, String name,
      ChannelProcessingStepType type, Instant actualChangeTime, Instant systemChangeTime,
      String status, String comment, UUID processingGroupId) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.actualChangeTime = actualChangeTime;
    this.systemChangeTime = systemChangeTime;
    this.status = status;
    this.comment = comment;
    this.processingGroupId = processingGroupId;
  }

  public String getName() {
    return name;
  }

  public ChannelProcessingStepType getType() {
    return type;
  }

  public Instant getActualChangeTime() {
    return actualChangeTime;
  }

  public Instant getSystemChangeTime() {
    return systemChangeTime;
  }

  public String getStatus() {
    return status;
  }

  public String getComment() {
    return comment;
  }

  public UUID getProcessingGroupId() {
    return processingGroupId;
  }
}
