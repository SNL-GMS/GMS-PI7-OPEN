package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Dao equivalent of {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask},
 * used to perform storage and retrieval operations on the QcMask via JPA.
 */
@Entity
@Table(name = "qcmasks")
public class QcMaskDao {

  @Id
  @GeneratedValue
  private long daoId;

  @Column(updatable = false)
  private UUID id;

  @Column(updatable = false)
  private UUID channelId;

  public QcMaskDao() {
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

  public UUID getChannelId() {
    return channelId;
  }

  public void setChannelId(UUID channelId) {
    this.channelId = channelId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QcMaskDao qcMaskDao = (QcMaskDao) o;
    return daoId == qcMaskDao.daoId &&
        Objects.equals(id, qcMaskDao.id) &&
        Objects.equals(channelId, qcMaskDao.channelId);
  }

  @Override
  public int hashCode() {

    return Objects.hash(daoId, id, channelId);
  }

  @Override
  public String toString() {
    return "QcMaskDao{" +
        "daoId=" + daoId +
        ", id=" + id +
        ", channelId=" + channelId +
        '}';
  }
}
