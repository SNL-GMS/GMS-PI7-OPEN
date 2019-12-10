package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "qcmask_version_references")
public class QcMaskVersionDescriptorDao {

  @Id
  @GeneratedValue
  private long daoId;

  @Column(updatable = false)
  private UUID qcMaskId;

  private long qcMaskVersionId;

  public QcMaskVersionDescriptorDao() {
  }

  public QcMaskVersionDescriptorDao(
      UUID qcMaskId,
      long qcMaskVersionId) {
    this.qcMaskId = qcMaskId;
    this.qcMaskVersionId = qcMaskVersionId;
  }

  public long getDaoId() {
    return daoId;
  }

  public void setDaoId(long daoId) {
    this.daoId = daoId;
  }

  public UUID getQcMaskId() {
    return qcMaskId;
  }

  public void setQcMaskId(
      UUID qcMaskId) {
    this.qcMaskId = qcMaskId;
  }

  public long getQcMaskVersionId() {
    return qcMaskVersionId;
  }

  public void setQcMaskVersionId(
      long qcMaskVersionId) {
    this.qcMaskVersionId = qcMaskVersionId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QcMaskVersionDescriptorDao that = (QcMaskVersionDescriptorDao) o;
    return daoId == that.daoId &&
        qcMaskVersionId == that.qcMaskVersionId &&
        Objects.equals(qcMaskId, that.qcMaskId);
  }

  @Override
  public int hashCode() {

    return Objects.hash(daoId, qcMaskId, qcMaskVersionId);
  }

  @Override
  public String toString() {
    return "QcMaskVersionDescriptorDao{" +
        "daoId=" + daoId +
        ", qcMaskId=" + qcMaskId +
        ", qcMaskVersionId=" + qcMaskVersionId +
        '}';
  }
}
