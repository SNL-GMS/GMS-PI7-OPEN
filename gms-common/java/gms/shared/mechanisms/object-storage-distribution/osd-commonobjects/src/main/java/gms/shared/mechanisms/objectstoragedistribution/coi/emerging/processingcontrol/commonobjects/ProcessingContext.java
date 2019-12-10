package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Value object holding provenance information identifying where in the processing workflow an
 * automatic, interactive, or interactive initiated processing sequence occurs.  Also includes the
 * storage visibility of any information stored in the OSD as a result of the action.
 */
public class ProcessingContext {

  /**
   * References where automatic processing occurs by stage, sequence, step.  Optional within the
   * ProcessingContext but AnalystActionReference, ProcessingStepReference, or both must exist.
   */
  private final Optional<ProcessingStepReference> processingStepReference;

  /**
   * References where an interactive processing actions occurs by stage, activity, analyst. Optional
   * within the ProcessingContext but AnalystActionReference, ProcessingStepReference, or both must
   * exist.
   */
  private final Optional<AnalystActionReference> analystActionReference;

  /**
   * Visibility of any objects stored in the OSD as a result of this processing.  Required within
   * the ProcessingContext.
   */
  private final StorageVisibility storageVisibility;

  /**
   * Constructs a ProcessingContext from the analystActionReference, processingStepReference, and
   * storageVisibility.
   *
   * analystActionReference and processingStepReference are both optional but at least one of them
   * must be present.  The static factory operations in this class enforce this constraint.
   *
   * @param analystActionReference optional AnalystActionReference
   * @param processingStepReference optional ProcessingStepReference
   * @param storageVisibility visibility for objects stored in the OSD, not null
   */
  private ProcessingContext(
      Optional<AnalystActionReference> analystActionReference,
      Optional<ProcessingStepReference> processingStepReference,
      StorageVisibility storageVisibility) {

    this.processingStepReference = processingStepReference;
    this.analystActionReference = analystActionReference;
    this.storageVisibility = storageVisibility;
  }

  public static ProcessingContext from(Optional<AnalystActionReference> analystActionReference,
      Optional<ProcessingStepReference> processingStepReference,
      StorageVisibility storageVisibility) {
    Objects.requireNonNull(analystActionReference,
        "Cannot create ProcessingContext from null AnalystActionReference");
    Objects.requireNonNull(processingStepReference,
        "Cannot create ProcessingContext from null ProcessingStepReference");
    Objects.requireNonNull(storageVisibility,
        "Cannot create ProcessingContext from null StorageVisibility");

    return new ProcessingContext(analystActionReference, processingStepReference,
        storageVisibility);
  }

  /**
   * Obtains an instance of ProcessingContext for an automatic processing action from the
   * processingStageIntervalId, processingSequenceIntervalId, processingStepId, and
   * storageVisibility.
   *
   * The id parameters are each an id to instances of the {@link
   * ProcessingStageInterval}, {@link ProcessingSequenceInterval}, and {@link ProcessingStep}
   * objects forming provenance for where the automatic processing action occurs within the GMS
   * processing workflow.
   *
   * @param processingStageIntervalId id to a ProcessingStageInterval, not null
   * @param processingSequenceIntervalId id to a ProcessingSequenceInterval, not null
   * @param processingStepId id to a ProcessingStep, not null
   * @param storageVisibility visibility for objects stored in the OSD, not null
   * @return a ProcessingStepReference, not null
   * @throws IllegalArgumentException if the storageVisibility is null
   */
  public static ProcessingContext createAutomatic(
      UUID processingStageIntervalId,
      UUID processingSequenceIntervalId,
      UUID processingStepId,
      StorageVisibility storageVisibility) {

    Objects.requireNonNull(storageVisibility,
        "Can't build ProcessingContext from a null StorageVisibility");

    //Null checks made within the child from methods
    ProcessingStepReference stepRef = ProcessingStepReference
        .from(processingStageIntervalId, processingSequenceIntervalId, processingStepId);

    return new ProcessingContext(Optional.empty(), Optional.of(stepRef), storageVisibility);
  }

  /**
   * Obtains an instance of ProcessingContext for an interactive processing action from the
   * processingStageIntervalId, processingActivityIntervalId, analystId, and storageVisibility.
   *
   * The id parameters are each an id to instances of the {@link
   * ProcessingStageInterval}, {@link ProcessingActivityInterval}, and {@link Analyst} objects
   * forming provenance for where the interactive processing action occurs within the GMS processing
   * workflow.
   *
   * @param processingStageIntervalId id to a ProcessingStageInterval, not null
   * @param processingActivityIntervalId id to a ProcessingActivityInterval, not null
   * @param analystId id to an Analyst, not null
   * @param storageVisibility visibility for objects stored in the OSD, not null
   * @return a ProcessingStepReference, not null
   * @throws IllegalArgumentException if the storageVisibility is null
   */
  public static ProcessingContext createInteractive(
      UUID processingStageIntervalId,
      UUID processingActivityIntervalId,
      UUID analystId, StorageVisibility storageVisibility) {

    Objects.requireNonNull(storageVisibility,
        "Can't build ProcessingContext from a null StorageVisibility");

    //Null checks made within the child from methods
    AnalystActionReference actionRef = AnalystActionReference
        .from(processingStageIntervalId, processingActivityIntervalId, analystId);

    return new ProcessingContext(Optional.of(actionRef), Optional.empty(), storageVisibility);
  }

  /**
   * Obtains an instance of ProcessingContext for an interactive initiated processing sequence from
   * the processingStageIntervalId, processingActivityIntervalId, analystId,
   * processingSequenceIntervalId, processingStepId, and storageVisibility.
   *
   * The id parameters are each an id to instances of the {@link
   * ProcessingStageInterval}, {@link ProcessingActivityInterval}, {@link Analyst}, {@link
   * ProcessingSequenceInterval}, and {@link ProcessingStep} objects forming provenance for where
   * the interactive processing action occurs within the GMS processing workflow and which
   * processing sequence is initiated.
   *
   * @param processingStageIntervalId id to a ProcessingStageInterval, not null
   * @param processingActivityIntervalId id to a ProcessingActivityInterval, not null
   * @param analystId id to an Analyst, not null
   * @param processingSequenceIntervalId id to a ProcessingSequenceInterval, not null
   * @param processingStepId id to a ProcessingStep, not null
   * @param storageVisibility visibility for objects stored in the OSD, not null
   * @return a ProcessingStepReference, not null
   * @throws IllegalArgumentException if the storageVisibility is null
   */
  public static ProcessingContext createInteractiveInitiatedAutomatic(
      UUID processingStageIntervalId,
      UUID processingActivityIntervalId,
      UUID analystId,
      UUID processingSequenceIntervalId,
      UUID processingStepId, StorageVisibility storageVisibility) {

    Objects.requireNonNull(storageVisibility,
        "Can't build ProcessingContext from a null StorageVisibility");

    //Null checks made within the child from methods
    AnalystActionReference actionRef = AnalystActionReference
        .from(processingStageIntervalId, processingActivityIntervalId, analystId);

    ProcessingStepReference stepRef = ProcessingStepReference
        .from(processingStageIntervalId, processingSequenceIntervalId, processingStepId);

    return new ProcessingContext(Optional.of(actionRef), Optional.of(stepRef), storageVisibility);
  }

  /**
   * Gets the {@link ProcessingStepReference} from this ProcessingContext.  The Optional will be
   * empty if this ProcessingContext does not have a ProcessingStepReference.
   *
   * @return an Optional ProcessingStepReference
   */
  public Optional<ProcessingStepReference> getProcessingStepReference() {
    return processingStepReference;
  }

  /**
   * Gets the {@link AnalystActionReference} from this ProcessingContext.  The Optional will be
   * empty if this ProcessingContext does not have a AnalystActionReference.
   *
   * @return an Optional AnalystActionReference
   */
  public Optional<AnalystActionReference> getAnalystActionReference() {
    return analystActionReference;
  }

  /**
   * Gets the {@link StorageVisibility} from this ProcessingContext.
   *
   * @return a StorageVisibility, not null
   */
  public StorageVisibility getStorageVisibility() {
    return storageVisibility;
  }

  /**
   * Checks if this ProcessingContext is equal to the other object.
   *
   * @param other the other object, null returns false
   * @return true if the other object is equal to this ProcessingContext
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    ProcessingContext that = (ProcessingContext) other;
    return Objects.equals(processingStepReference, that.processingStepReference) &&
        Objects.equals(analystActionReference, that.analystActionReference) &&
        storageVisibility == that.storageVisibility;
  }

  /**
   * Gets a hash code for this ProcessingContext
   *
   * @return a suitable hash code
   */
  @Override
  public int hashCode() {

    return Objects.hash(processingStepReference, analystActionReference, storageVisibility);
  }

  /**
   * Gets a string representation from this ProcessingContext
   *
   * @return string representation from this ProcessingContext, not null
   */
  @Override
  public String toString() {
    return "ProcessingContext{" +
        "processingStepReference=" + processingStepReference +
        ", analystActionReference=" + analystActionReference +
        ", storageVisibility=" + storageVisibility +
        '}';
  }
}
