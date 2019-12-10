package gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFile;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileMetadataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileStatus;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Table(name = "transferred_file")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class TransferredFileDao<T> {

  /**
   * Define a Data Access Object to allow read and write access to the relational database.
   */
  @Id
  @GeneratedValue
  private long primaryKey;

  private String filename;

  private String priority;

  @Column(name = "transfer_time")
  private Instant transferTime;

  @Column(name = "reception_time")
  private Instant receptionTime;

  private TransferredFileStatus status;

  @Column(name = "metadata_type", nullable = false)
  private TransferredFileMetadataType metadataType;

  /**
   * Create a DAO from the COI object.
   *
   * @param transferredFile The TransferredFile object
   */
  @SuppressWarnings("unchecked")
  public TransferredFileDao(TransferredFile transferredFile) {

    Objects.requireNonNull(transferredFile,
        "Cannot create a TransferredFileDao from a null TransferredFile");

    this.filename = transferredFile.getFileName();
    Optional<String> priority = transferredFile.getPriority();
    this.priority = priority.orElse(null);
    Optional<Instant> transferTime = transferredFile.getTransferTime();
    this.transferTime = transferTime.orElse(null);
    Optional<Instant> receptionTime = transferredFile.getReceptionTime();
    this.receptionTime = receptionTime.orElse(null);
    this.status = transferredFile.getStatus();
    this.metadataType = transferredFile.getMetadataType();
  }

  /**
   * Default constructor for JPA.
   */
  public TransferredFileDao() {
  }

  public TransferredFile<T> toCoi() {
    return TransferredFile.from(this.filename, this.priority, this.transferTime,
        this.receptionTime, this.status, this.metadataType, getMetadataCoi());
  }

  public abstract T getMetadataCoi();

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public Instant getTransferTime() {
    return transferTime;
  }

  public void setTransferTime(Instant transferTime) {
    this.transferTime = transferTime;
  }

  public Instant getReceptionTime() {
    return receptionTime;
  }

  public void setReceptionTime(Instant receptionTime) {
    this.receptionTime = receptionTime;
  }

  public TransferredFileStatus getStatus() {
    return status;
  }

  public void setStatus(
      TransferredFileStatus status) {
    this.status = status;
  }

  public TransferredFileMetadataType getMetadataType() {
    return metadataType;
  }

  public void setMetadataType(
      TransferredFileMetadataType metadataType) {
    this.metadataType = metadataType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransferredFileDao<?> that = (TransferredFileDao<?>) o;
    return primaryKey == that.primaryKey &&
        Objects.equals(filename, that.filename) &&
        Objects.equals(priority, that.priority) &&
        Objects.equals(transferTime, that.transferTime) &&
        Objects.equals(receptionTime, that.receptionTime) &&
        status == that.status &&
        metadataType == that.metadataType;
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(primaryKey, filename, priority, transferTime, receptionTime, status, metadataType);
  }

  @Override
  public String toString() {
    return "TransferredFileDao{" +
        "primaryKey=" + primaryKey +
        ", filename='" + filename + '\'' +
        ", priority='" + priority + '\'' +
        ", transferTime=" + transferTime +
        ", receptionTime=" + receptionTime +
        ", status=" + status +
        ", metadataType=" + metadataType +
        '}';
  }
}
