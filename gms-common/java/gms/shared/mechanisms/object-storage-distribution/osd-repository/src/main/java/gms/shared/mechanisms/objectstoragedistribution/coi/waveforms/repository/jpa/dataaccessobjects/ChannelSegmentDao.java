package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects.CreationInfoDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries.Type;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.apache.commons.lang3.Validate;

/**
 * Define a Data Access Object to allow access to the relational database.
 */

@Entity
@Table(name = "channel_segment",
  indexes = {@Index
      (name = "channelSegmentChannelIdStartEnd", columnList = "channel_id, startTime, endTime", unique = true)})
//        uniqueConstraints = {
//        // TODO: "channel_id" should be added to this list.  However, CSS loader service
//        // would probably need to query for a ProcessingChannel from the OSD first and if found,
//        // use that ID in the ChannelSegment it receives from the CSS loader client.  Without this check,
//        // the attempt to store an identical ChannelSegment except with different channelId will succeed.
//        // In summary, the issue is when CSS loader creates a ChannelSegment with a new channelId even though
//        // the ProcessingChannel actually exists in the OSD.
//                @UniqueConstraint(columnNames = {"name", "type", "timeseriesType", "startTime", "endTime"})},
//    indexes = {@Index(columnList = "channel_id,startTime")})
public class ChannelSegmentDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(unique = true)
  private UUID id;

  @Column(name = "channel_id")
  private UUID channelId;

  @Column(name = "name", nullable=false)
  private String name;

  @Column(name = "type", nullable=false)
  private ChannelSegment.Type type;

  @Column(name = "timeseriesType", nullable = false)
  private Timeseries.Type timeseriesType;

  @Column(name = "startTime", nullable=false)
  private Instant startTime;

  @Column(name = "endTime", nullable=false)
  private Instant endTime;

  @Embedded
  private CreationInfoDao creationInfo;

  @ElementCollection(fetch = FetchType.EAGER)
  private List<UUID> timeSeriesIds;

  @Transient
  private ChannelSegment channelSegment;

  /**
   * Default constructor for use by JPA
   */
  public ChannelSegmentDao() {
  }

  /**
   * Create this DAO from the COI object.
   *
   * @param channelSegment  COI object
   */
  public ChannelSegmentDao(ChannelSegment<? extends Timeseries> channelSegment) throws NullPointerException {
    Validate.notNull(channelSegment);
    this.id = channelSegment.getId();
    this.channelId = channelSegment.getChannelId();
    this.name = channelSegment.getName();
    this.type = channelSegment.getType();
    this.timeseriesType = channelSegment.getTimeseriesType();
    this.startTime = channelSegment.getStartTime();
    this.endTime = channelSegment.getEndTime();
    this.creationInfo = new CreationInfoDao(channelSegment.getCreationInfo());
    this.channelSegment = channelSegment;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ChannelSegment.Type getType() {
    return type;
  }

  public void setType(
      ChannelSegment.Type type) {
    this.type = type;
  }

  public Type getTimeseriesType() {
    return timeseriesType;
  }

  public void setTimeseriesType(
      Type timeseriesType) {
    this.timeseriesType = timeseriesType;
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

  public ChannelSegment getChannelSegment() {
    return channelSegment;
  }

  public void setChannelSegment(
      ChannelSegment channelSegment) {
    this.channelSegment = channelSegment;
  }

  public CreationInfoDao getCreationInfo() {
    return creationInfo;
  }

  public void setCreationInfo(CreationInfoDao creationInfo) {
    this.creationInfo = creationInfo;
  }

  public List<UUID> getTimeSeriesIds() {
    return timeSeriesIds;
  }

  public void setTimeSeriesIds(List<UUID> timeSeriesIds) {
    this.timeSeriesIds = timeSeriesIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ChannelSegmentDao)) {
      return false;
    }

    ChannelSegmentDao that = (ChannelSegmentDao) o;

    if (primaryKey != that.primaryKey) {
      return false;
    }
    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (channelId != null ? !channelId.equals(that.channelId) : that.channelId != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (type != that.type) {
      return false;
    }
    if (timeseriesType != that.timeseriesType) {
      return false;
    }
    if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) {
      return false;
    }
    if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) {
      return false;
    }
    if (creationInfo != null ? !creationInfo.equals(that.creationInfo)
        : that.creationInfo != null) {
      return false;
    }
    if (timeSeriesIds != null ? !timeSeriesIds.equals(that.timeSeriesIds)
        : that.timeSeriesIds != null) {
      return false;
    }
    return channelSegment != null ? channelSegment.equals(that.channelSegment)
        : that.channelSegment == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (primaryKey ^ (primaryKey >>> 32));
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (channelId != null ? channelId.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (timeseriesType != null ? timeseriesType.hashCode() : 0);
    result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
    result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
    result = 31 * result + (creationInfo != null ? creationInfo.hashCode() : 0);
    result = 31 * result + (timeSeriesIds != null ? timeSeriesIds.hashCode() : 0);
    result = 31 * result + (channelSegment != null ? channelSegment.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ChannelSegmentDao{" +
        "primaryKey=" + primaryKey +
        ", id=" + id +
        ", channelId=" + channelId +
        ", name='" + name + '\'' +
        ", type=" + type +
        ", timeseriesType=" + timeseriesType +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", creationInfo=" + creationInfo +
        ", timeSeriesIds=" + timeSeriesIds +
        ", channelSegment=" + channelSegment +
        '}';
  }
}
