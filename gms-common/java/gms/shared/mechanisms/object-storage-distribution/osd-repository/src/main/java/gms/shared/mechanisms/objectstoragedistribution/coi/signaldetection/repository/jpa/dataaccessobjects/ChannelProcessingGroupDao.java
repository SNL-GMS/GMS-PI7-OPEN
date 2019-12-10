package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroupType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.ChannelProcessingGroupTypeConverter;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;


/**
 * JPA data access object for {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup}
 */
@Entity
@Table(name = "channel_processing_group")
public class ChannelProcessingGroupDao {

  @Id
  @GeneratedValue
  private long daoId;

  @Column()
  private UUID id;

  @Convert(converter = ChannelProcessingGroupTypeConverter.class)
  private ChannelProcessingGroupType type;

  @ElementCollection
  @CollectionTable(name = "channel_processing_group_channel_ids")
  @LazyCollection(LazyCollectionOption.FALSE)
  @Column()
  private Set<UUID> channelIds;

  private Instant actualChangeTime;
  private Instant systemChangeTime;
  private String status;
  private String comment;

  public ChannelProcessingGroupDao() {
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

  public ChannelProcessingGroupType getType() {
    return type;
  }

  public void setType(
      ChannelProcessingGroupType type) {
    this.type = type;
  }

  public Set<UUID> getChannelIds() {
    return channelIds;
  }

  public void setChannelIds(Set<UUID> channelIds) {
    this.channelIds = channelIds;
  }

  public Instant getActualChangeTime() {
    return actualChangeTime;
  }

  public void setActualChangeTime(Instant actualChangeTime) {
    this.actualChangeTime = actualChangeTime;
  }

  public Instant getSystemChangeTime() {
    return systemChangeTime;
  }

  public void setSystemChangeTime(Instant systemChangeTime) {
    this.systemChangeTime = systemChangeTime;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ChannelProcessingGroupDao that = (ChannelProcessingGroupDao) o;

    if (daoId != that.daoId) {
      return false;
    }
    if (!id.equals(that.id)) {
      return false;
    }
    if (type != that.type) {
      return false;
    }
    if (!channelIds.equals(that.channelIds)) {
      return false;
    }
    if (!actualChangeTime.equals(that.actualChangeTime)) {
      return false;
    }
    if (!systemChangeTime.equals(that.systemChangeTime)) {
      return false;
    }
    if (!status.equals(that.status)) {
      return false;
    }
    return comment.equals(that.comment);
  }

  @Override
  public int hashCode() {
    int result = (int) (daoId ^ (daoId >>> 32));
    result = 31 * result + id.hashCode();
    result = 31 * result + type.hashCode();
    result = 31 * result + channelIds.hashCode();
    result = 31 * result + actualChangeTime.hashCode();
    result = 31 * result + systemChangeTime.hashCode();
    result = 31 * result + status.hashCode();
    result = 31 * result + comment.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ChannelProcessingGroupDao{" +
        "daoId=" + daoId +
        ", id=" + id +
        ", type=" + type +
        ", channelIds=" + channelIds +
        ", actualChangeTime=" + actualChangeTime +
        ", systemChangeTime=" + systemChangeTime +
        ", status='" + status + '\'' +
        ", comment='" + comment + '\'' +
        '}';
  }

}
