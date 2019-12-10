package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Dao equivalent of {@link gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation}
 * used to perform storage and retrieval operations on the CreationInformation via JPA.
 */
@Entity
@Table(name = "creation_information")
public class CreationInformationDao {

  @Id
  @GeneratedValue
  private long daoId;

  @Column(updatable = false, nullable = false)
  private UUID id;

  @Column(nullable = false)
  private Instant creationTime;

  @Column(updatable = false, nullable = false)
  private UUID processingStageIntervalId;

  @Column(updatable = false)
  private UUID processingActivityIntervalId;

  @Column(updatable = false)
  private UUID analystId;

  @Column(updatable = false)
  private UUID processingSequenceIntervalId;

  @Column(updatable = false)
  private UUID processingStepId;

  @Column(nullable = false)
  private String softwareComponentName;

  @Column(nullable = false)
  private String softwareComponentVersion;

  /**
   * Default constructor for use by JPA.
   */
  public CreationInformationDao() {
  }

  public long getDaoId() {
    return daoId;
  }

  public void setDaoId(long daoId) {
    this.daoId = daoId;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Instant getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(Instant creationTime) {
    this.creationTime = creationTime;
  }

  public String getSoftwareComponentName() {
    return softwareComponentName;
  }

  public void setSoftwareComponentName(String softwareComponentName) {
    this.softwareComponentName = softwareComponentName;
  }

  public String getSoftwareComponentVersion() {
    return softwareComponentVersion;
  }

  public void setSoftwareComponentVersion(String softwareComponentVersion) {
    this.softwareComponentVersion = softwareComponentVersion;
  }

  public UUID getProcessingStepId() {
    return processingStepId;
  }

  public void setProcessingStepId(UUID processingStepId) {
    this.processingStepId = processingStepId;
  }

  public UUID getProcessingStageIntervalId() {
    return processingStageIntervalId;
  }

  public void setProcessingStageIntervalId(UUID processingStageIntervalId) {
    this.processingStageIntervalId = processingStageIntervalId;
  }

  public UUID getProcessingActivityIntervalId() {
    return processingActivityIntervalId;
  }

  public void setProcessingActivityIntervalId(UUID processingActivityIntervalId) {
    this.processingActivityIntervalId = processingActivityIntervalId;
  }

  public UUID getAnalystId() {
    return analystId;
  }

  public void setAnalystId(UUID analystId) {
    this.analystId = analystId;
  }

  public UUID getProcessingSequenceIntervalId() {
    return processingSequenceIntervalId;
  }

  public void setProcessingSequenceIntervalId(UUID processingSequenceIntervalId) {
    this.processingSequenceIntervalId = processingSequenceIntervalId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreationInformationDao that = (CreationInformationDao) o;
    return daoId == that.daoId &&
        Objects.equals(id, that.id) &&
        Objects.equals(creationTime, that.creationTime) &&
        Objects.equals(processingStageIntervalId, that.processingStageIntervalId) &&
        Objects.equals(processingActivityIntervalId, that.processingActivityIntervalId) &&
        Objects.equals(analystId, that.analystId) &&
        Objects.equals(processingSequenceIntervalId, that.processingSequenceIntervalId) &&
        Objects.equals(processingStepId, that.processingStepId) &&
        Objects.equals(softwareComponentName, that.softwareComponentName) &&
        Objects.equals(softwareComponentVersion, that.softwareComponentVersion);
  }

  @Override
  public int hashCode() {

    return Objects
        .hash(daoId, id, creationTime, processingStageIntervalId, processingActivityIntervalId,
            analystId, processingSequenceIntervalId, processingStepId, softwareComponentName,
            softwareComponentVersion);
  }

  @Override
  public String toString() {
    return "CreationInformationDao{" +
        "daoId=" + daoId +
        ", id=" + id +
        ", creationTime=" + creationTime +
        ", processingStageIntervalId=" + processingStageIntervalId +
        ", processingActivityIntervalId=" + processingActivityIntervalId +
        ", analystId=" + analystId +
        ", processingSequenceIntervalId=" + processingSequenceIntervalId +
        ", processingStepId=" + processingStepId +
        ", softwareComponentName='" + softwareComponentName + '\'' +
        ", softwareComponentVersion='" + softwareComponentVersion + '\'' +
        '}';
  }
}
