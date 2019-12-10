package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.QcMaskCategoryConverter;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.QcMaskTypeConverter;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Dao equivalent from {@link QcMaskVersion},
 * used to perform storage and retrieval operations on the QcMaskVersion via JPA.
 */
@Entity
@Table(name = "qcmask_versions")
public class QcMaskVersionDao {

  @Id
  @GeneratedValue
  private long daoId;

  @ManyToOne(optional = false)
  private QcMaskDao ownerQcMask;

  private long version;

  @ManyToMany(cascade = CascadeType.ALL)
  private List<QcMaskVersionDescriptorDao> parentQcMasks;

  @ElementCollection
  @Column(updatable = false)
  private List<UUID> channelSegmentIds;

  @Convert(converter = QcMaskTypeConverter.class)
  private QcMaskType type;

  @Convert(converter = QcMaskCategoryConverter.class)
  private QcMaskCategory category;

  private String rationale;

  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant startTime;

  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant endTime;

  public QcMaskVersionDao() {
  }

  public long getDaoId() {
    return daoId;
  }

  public void setDaoId(long daoId) {
    this.daoId = daoId;
  }

  public QcMaskDao getOwnerQcMask() {
    return ownerQcMask;
  }

  public void setOwnerQcMask(
      QcMaskDao ownerQcMask) {
    this.ownerQcMask = ownerQcMask;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(
      long version) {
    this.version = version;
  }

  public List<QcMaskVersionDescriptorDao> getParentQcMasks() {
    return parentQcMasks;
  }

  public void setParentQcMasks(
      List<QcMaskVersionDescriptorDao> parentQcMasks) {
    this.parentQcMasks = parentQcMasks;
  }

  public List<UUID> getChannelSegmentIds() {
    return channelSegmentIds;
  }

  public void setChannelSegmentIds(List<UUID> channelSegmentIds) {
    this.channelSegmentIds = channelSegmentIds;
  }

  public QcMaskCategory getCategory() {
    return category;
  }

  public void setCategory(
      QcMaskCategory category) {
    this.category = category;
  }

  public QcMaskType getType() {
    return type;
  }

  public void setType(
      QcMaskType type) {
    this.type = type;
  }

  public String getRationale() {
    return rationale;
  }

  public void setRationale(String rationale) {
    this.rationale = rationale;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QcMaskVersionDao that = (QcMaskVersionDao) o;
    return daoId == that.daoId &&
        version == that.version &&
        Objects.equals(ownerQcMask, that.ownerQcMask) &&
        Objects.equals(parentQcMasks, that.parentQcMasks) &&
        Objects.equals(channelSegmentIds, that.channelSegmentIds) &&
        type == that.type &&
        category == that.category &&
        Objects.equals(rationale, that.rationale) &&
        Objects.equals(startTime, that.startTime) &&
        Objects.equals(endTime, that.endTime);
  }

  @Override
  public int hashCode() {

    return Objects
        .hash(daoId, ownerQcMask, version, parentQcMasks, channelSegmentIds, type, category,
            rationale, startTime, endTime);
  }

  @Override
  public String toString() {
    return "QcMaskVersionDao{" +
        "daoId=" + daoId +
        ", ownerQcMask=" + ownerQcMask +
        ", version=" + version +
        ", parentQcMasks=" + parentQcMasks +
        ", channelSegmentIds=" + channelSegmentIds +
        ", type=" + type +
        ", category=" + category +
        ", rationale='" + rationale + '\'' +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        '}';
  }
}
