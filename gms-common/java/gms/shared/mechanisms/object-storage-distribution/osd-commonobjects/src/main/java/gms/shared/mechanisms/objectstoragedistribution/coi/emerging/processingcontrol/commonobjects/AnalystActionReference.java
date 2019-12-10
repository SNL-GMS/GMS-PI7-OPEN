package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects;

import java.util.Objects;
import java.util.UUID;


/**
 * Value object holding provenance information identifying where in the interactive processing
 * workflow an analyst initiated a processing action.
 */
public class AnalystActionReference {

  private final UUID processingStageIntervalId;
  private final UUID processingActivityIntervalId;
  private final UUID analystId;

  private AnalystActionReference(
      UUID processingStageIntervalId,
      UUID processingActivityIntervalId,
      UUID analystId) {

    this.processingStageIntervalId = processingStageIntervalId;
    this.processingActivityIntervalId = processingActivityIntervalId;
    this.analystId = analystId;
  }

  /**
   * Obtains an instance of AnalystActionReference from the processingStageIntervalId,
   * processingActivityIntervalId, and analystId.
   *
   * These parameters are each an {@link UUID} to instances of the {@link
   * ProcessingStageInterval}, {@link ProcessingActivityInterval}, and {@link Analyst} objects
   * forming the AnalystActionReference.
   *
   * @param processingStageIntervalId id to a ProcessingStageInterval, not null
   * @param processingActivityIntervalId id to a ProcessingActivityInterval, not null
   * @param analystId id to an Analyst, not null
   * @return an AnalystActionReference, not null
   * @throws IllegalArgumentException if the processingStageIntervalId,
   * processingActivityIntervalId, or analystId are null
   */
  public static AnalystActionReference from(
      UUID processingStageIntervalId,
      UUID processingActivityIntervalId,
      UUID analystId) {

    final String exceptionMessage = "Can't build AnalystActionReference from a null ";
    Objects.requireNonNull(processingStageIntervalId,
        exceptionMessage + "processingStageIntervalId");
    Objects.requireNonNull(processingActivityIntervalId,
        exceptionMessage + "processingActivityIntervalId");
    Objects.requireNonNull(analystId, exceptionMessage + "analystId");

    return new AnalystActionReference(processingStageIntervalId, processingActivityIntervalId,
        analystId);
  }

  /**
   * Gets the {@link UUID} from this AnalystActionReference's {@link ProcessingStageInterval}
   *
   * @return id to a ProcessingStageInterval, not null
   */
  public UUID getProcessingStageIntervalId() {
    return processingStageIntervalId;
  }

  /**
   * Gets the {@link UUID} from this AnalystActionReference's {@link
   * ProcessingActivityInterval}
   *
   * @return id to a ProcessingActivityInterval, not null
   */
  public UUID getProcessingActivityIntervalId() {
    return processingActivityIntervalId;
  }

  /**
   * Gets the {@link UUID} from this AnalystActionReference's {@link Analyst}
   *
   * @return id to a Analyst, not null
   */
  public UUID getAnalystId() {
    return analystId;
  }

  /**
   * Checks if this AnalystActionReference is equal to the other object.
   *
   * @param other the other object, null returns false
   * @return true if the other object is equal to this AnalystActionReference
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    AnalystActionReference that = (AnalystActionReference) other;

    if (!getProcessingStageIntervalId().equals(that.getProcessingStageIntervalId())) {
      return false;
    }
    if (!getProcessingActivityIntervalId().equals(that.getProcessingActivityIntervalId())) {
      return false;
    }
    return getAnalystId().equals(that.getAnalystId());
  }

  /**
   * Gets a hash code for this AnalystActionReference
   *
   * @return a suitable hash code
   */
  @Override
  public int hashCode() {
    int result = getProcessingStageIntervalId().hashCode();
    result = 31 * result + getProcessingActivityIntervalId().hashCode();
    result = 31 * result + getAnalystId().hashCode();
    return result;
  }

  /**
   * Gets a string representation of this AnalystActionReference
   *
   * @return string representation of this AnalystActionReference, not null
   */
  @Override
  public String toString() {
    return "AnalystActionReference{" +
        "processingStageIntervalId=" + processingStageIntervalId +
        ", processingActivityIntervalId=" + processingActivityIntervalId +
        ", analystId=" + analystId +
        '}';
  }
}
