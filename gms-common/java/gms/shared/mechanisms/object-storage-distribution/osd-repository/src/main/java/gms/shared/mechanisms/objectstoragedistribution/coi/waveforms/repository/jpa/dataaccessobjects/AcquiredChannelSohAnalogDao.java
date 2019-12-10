package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects.CreationInfoDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import org.apache.commons.lang3.Validate;

/**
 * Define a Data Access Object to allow access to the relational database.
 */
@Entity
@Table(name = "channel_soh_analog",
       indexes = {@Index
           (name = "analogSohChannelIdStartEnd", columnList="channel_id, startTime, endTime", unique = true)})
public class AcquiredChannelSohAnalogDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "channel_id", nullable = false)
  private UUID channelId;

  @Column(name = "type", nullable = false)
  private AcquiredChannelSoh.AcquiredChannelSohType type;

  @Column(name = "startTime", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant startTime;

  @Column(name = "endTime", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant endTime;

  @Column(name = "status", nullable = false)
  private double status;

  @Embedded
  private CreationInfoDao creationInfo;


  /**
   * Default constructor for use by JPA.
   */
  public AcquiredChannelSohAnalogDao() {
  }

  /**
   * Create this DAO from the COI object.
   *
   * @param channelSoh The COI object
   */
  public AcquiredChannelSohAnalogDao(AcquiredChannelSohAnalog channelSoh)
      throws NullPointerException {
    Validate.notNull(channelSoh);

    this.id = channelSoh.getId();
    this.channelId = channelSoh.getChannelId();
    this.type = channelSoh.getType();
    this.startTime = channelSoh.getStartTime();
    this.endTime = channelSoh.getEndTime();
    this.status = channelSoh.getStatus();
    this.creationInfo = new CreationInfoDao(channelSoh.getCreationInfo());
  }

  public long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(long primaryKey) {
    this.primaryKey = primaryKey;
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

  public AcquiredChannelSoh.AcquiredChannelSohType getType() {
    return type;
  }

  public void setType(
      AcquiredChannelSoh.AcquiredChannelSohType type) {
    this.type = type;
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

  public double getStatus() {
    return status;
  }

  public void setStatus(double status) {
    this.status = status;
  }

  public CreationInfoDao getCreationInfo() {
    return creationInfo;
  }

  public void setCreationInfo(
      CreationInfoDao creationInfo) {
    this.creationInfo = creationInfo;
  }

  public AcquiredChannelSohAnalog toCoi() {
    return AcquiredChannelSohAnalog.from(
        this.id, this.channelId, this.type,
        this.startTime, this.endTime, this.status,
        this.creationInfo.toCoi());
  }
}
