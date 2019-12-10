package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.AnalystActionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingStepReference;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents information about how an object was created, such as when and by who/what. See section
 * 2.2.2 'Creation Info' of the GMS Conceptual Data Model document.
 */
public class CreationInformation {

  private final UUID id;
  private final Instant creationTime;
  private final Optional<AnalystActionReference> analystActionReference;
  private final Optional<ProcessingStepReference> processingStepReference;
  private final SoftwareComponentInfo softwareInfo;

  /**
   * Constructs a CreationInformation from the {@link AnalystActionReference}, {@link
   * ProcessingStepReference}, and {@link SoftwareComponentInfo}.
   *
   * analystActionReference and processingStepReference are both optional but at least one of them
   * must be present.  The static factory operations in this class enforce this constraint.
   *
   * @param id unique identifier for CreationInformation
   * @param creationTime time the CreationInformation was created
   * @param analystActionReference optional AnalystActionReference
   * @param processingStepReference optional ProcessingStepReference
   * @param softwareInfo name of software and version
   */
  private CreationInformation(
      UUID id,
      Instant creationTime,
      Optional<AnalystActionReference> analystActionReference,
      Optional<ProcessingStepReference> processingStepReference,
      SoftwareComponentInfo softwareInfo) {

    this.id = id;
    this.creationTime = creationTime;
    this.analystActionReference = analystActionReference;
    this.processingStepReference = processingStepReference;
    this.softwareInfo = softwareInfo;
  }

  /**
   * Obtains an instance of CreationInformation from the CreationInformation's identifier, time it
   * was created, analystActionRef, processingStepRef, and software info.
   *
   * analystActionReference and processingStepReference are both optional but at least one of them
   * must be present.  The static factory operations in this class enforce this constraint.
   *
   * @param id unique identifier for CreationInformation
   * @param creationTime time the CreationInformation was created
   * @param analystActionReference optional AnalystActionReference
   * @param processingStepReference optional ProcessingStepReference
   * @param softwareInfo name of software and version
   * @throws IllegalArgumentException if neither AnalystActionReference nor ProcessingStepReference
   * are present
   */
  public static CreationInformation from(
      UUID id,
      Instant creationTime,
      Optional<AnalystActionReference> analystActionReference,
      Optional<ProcessingStepReference> processingStepReference,
      SoftwareComponentInfo softwareInfo) {

    Objects.requireNonNull(id,
        "Cannot create CreationInformation from null id");
    Objects.requireNonNull(creationTime,
        "Cannot create CreationInformation from null creationTime");
    Objects.requireNonNull(analystActionReference,
        "Cannot create CreationInformation from null AnalystActionReference");
    Objects.requireNonNull(processingStepReference,
        "Cannot create CreationInformation from null ProcessingStepReference");
    Objects
        .requireNonNull(softwareInfo, "Cannot create CreationInformation from null SoftwareInfo");

    if (!(analystActionReference.isPresent() || processingStepReference.isPresent())) {
      String exceptionMessage = "Cannot create CreationInformation. It must contain an AnalystActionReference or a ProcessingStepReference";
      throw new IllegalArgumentException(exceptionMessage);
    }

    return new CreationInformation(id, creationTime, analystActionReference,
        processingStepReference, softwareInfo);
  }

  public static CreationInformation create(
      Optional<AnalystActionReference> analystActionReference,
      Optional<ProcessingStepReference> processingStepReference,
      SoftwareComponentInfo softwareInfo) {

    return CreationInformation
        .from(UUID.randomUUID(), Instant.now(), analystActionReference,
            processingStepReference, softwareInfo);
  }

  public UUID getId() {
    return id;
  }

  public Instant getCreationTime() {
    return creationTime;
  }

  public Optional<AnalystActionReference> getAnalystActionReference() {
    return analystActionReference;
  }

  public Optional<ProcessingStepReference> getProcessingStepReference() {
    return processingStepReference;
  }

  public SoftwareComponentInfo getSoftwareInfo() {
    return softwareInfo;
  }

  @Override
  public String toString() {
    return "CreationInformation{" +
        "id=" + id +
        "creationTime=" + creationTime +
        "analystActionReference=" + analystActionReference +
        "processingStepReference" + processingStepReference +
        "softwareInfo=" + softwareInfo +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreationInformation that = (CreationInformation) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(creationTime, that.creationTime) &&
        Objects.equals(analystActionReference, that.analystActionReference) &&
        Objects.equals(processingStepReference, that.processingStepReference) &&
        Objects.equals(softwareInfo, that.softwareInfo);
  }

  @Override
  public int hashCode() {

    return Objects
        .hash(id, creationTime, analystActionReference, processingStepReference, softwareInfo);
  }
}
