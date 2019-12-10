package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object holding provenance information identifying where in the processing workflow an
 * automatic processing action occurs.
 */
public class ProcessingStepReference {

  private final UUID processingStageIntervalId;
  private final UUID processingSequenceIntervalId;
  private final UUID processingStepId;

  private ProcessingStepReference(
      UUID processingStageIntervalId,
      UUID processingSequenceIntervalId,
      UUID processingStepId) {

    this.processingStageIntervalId = processingStageIntervalId;
    this.processingSequenceIntervalId = processingSequenceIntervalId;
    this.processingStepId = processingStepId;
  }

  /**
   * Obtains an instance from ProcessingStepReference from the processingStageIntervalId,
   * processingSequenceIntervalId, and processingStepId.
   *
   * These parameters are each an id to instances from the {@link
   * ProcessingStageInterval}, {@link ProcessingSequenceInterval}, and {@link ProcessingStep}
   * objects forming the ProcessingStepReference.
   *
   * @param processingStageIntervalId id to a ProcessingStageInterval, not null
   * @param processingSequenceIntervalId id to a ProcessingSequenceInterval, not null
   * @param processingStepId id to a ProcessingStep, not null
   * @return a ProcessingStepReference, not null
   * @throws IllegalArgumentException if the processingStageIntervalId,
   * processingSequenceIntervalId, or processingStepId are null
   */
  public static ProcessingStepReference from(
      UUID processingStageIntervalId,
      UUID processingSequenceIntervalId,
      UUID processingStepId) {

    final String exceptionMessage = "Can't build ProcessingStepReference from a null ";
    Objects.requireNonNull(processingStageIntervalId,
        exceptionMessage + "processingStageIntervalId");
    Objects.requireNonNull(processingSequenceIntervalId,
        exceptionMessage + "processingSequenceIntervalId");
    Objects.requireNonNull(processingStepId,
        exceptionMessage + "processingStepId");

    return new ProcessingStepReference(processingStageIntervalId, processingSequenceIntervalId,
        processingStepId);
  }

  /**
   * Gets the {@link UUID} from this ProcessingStepReference's {@link ProcessingStageInterval}
   *
   * @return id to a ProcessingStageInterval, not null
   */
  public UUID getProcessingStageIntervalId() {
    return processingStageIntervalId;
  }

  /**
   * Gets the {@link UUID} from this ProcessingStepReference's {@link
   * ProcessingSequenceInterval}
   *
   * @return id to a ProcessingSequenceInterval, not null
   */
  public UUID getProcessingSequenceIntervalId() {
    return processingSequenceIntervalId;
  }

  /**
   * Gets the {@link UUID} from this ProcessingStepReference's {@link ProcessingStep}
   *
   * @return id to a ProcessingStep, not null
   */
  public UUID getProcessingStepId() {
    return processingStepId;
  }

  /**
   * Checks if this ProcessingStepReference is equal to the other object.
   *
   * @param other the other object, null returns false
   * @return true if the other object is equal to this ProcessingStepReference
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    ProcessingStepReference that = (ProcessingStepReference) other;
    return Objects.equals(processingStageIntervalId, that.processingStageIntervalId) &&
        Objects.equals(processingSequenceIntervalId, that.processingSequenceIntervalId) &&
        Objects.equals(processingStepId, that.processingStepId);
  }

  /**
   * Gets a hash code for this ProcessingStepReference
   *
   * @return a suitable hash code
   */
  @Override
  public int hashCode() {

    return Objects.hash(processingStageIntervalId, processingSequenceIntervalId, processingStepId);

  }

  /**
   * Gets a string representation from this ProcessingStepReference
   *
   * @return string representation from this ProcessingStepReference, not null
   */
  @Override
  public String toString() {
    return "ProcessingStepReference{" +
        "processingStageIntervalId=" + processingStageIntervalId +
        ", processingSequenceIntervalId=" + processingSequenceIntervalId +
        ", processingStepId=" + processingStepId +
        '}';
  }
}
