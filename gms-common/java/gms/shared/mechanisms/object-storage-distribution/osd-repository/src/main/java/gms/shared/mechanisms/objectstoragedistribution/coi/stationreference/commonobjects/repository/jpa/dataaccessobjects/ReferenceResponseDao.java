package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects.InformationSourceDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceResponse;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import org.apache.commons.lang3.Validate;

/**
 * Define a Data Access Object to allow read and write access to the relational database.
 */
@Entity
@Table(name="reference_response")
public class ReferenceResponseDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(unique = true)
  private UUID id;

  @Column()
  private UUID channelId;

  @Column(name="type")
  private String type;

  @Lob
  @Column(nullable=false, name = "data")
  private byte[] data;

  @Column(name="units")
  private String units;

  @Column(name="actual_time")
  private Instant actualTime;

  @Column(name="system_time")
  private Instant systemTime;

  @Column(name="comment")
  private String comment;

  @Embedded
  private InformationSourceDao informationSource;

  /**
   * Default constructor for JPA.
   */
  public ReferenceResponseDao() {}

  /**
   * Create a DAO from the COI object.
   * @param response The ReferenceResponse object.
   * @throws NullPointerException
   */
  public ReferenceResponseDao(ReferenceResponse response) throws NullPointerException {
    Validate.notNull(response);
    this.id = response.getId();
    this.channelId = response.getChannelId();
    this.type = response.getResponseType();
    this.data = response.getResponseData();
    this.units = response.getUnits();
    this.actualTime = response.getActualTime();
    this.systemTime = response.getSystemTime();
    this.comment = response.getComment();
    this.informationSource = new InformationSourceDao(response.getInformationSource());
  }

  /**
   * Convert this DAO into its corresponding COI object.
   * @return A ReferenceResponse COI object.
   */
  public ReferenceResponse toCoi() {
    return  ReferenceResponse.from(getId(), getChannelId(), getType(), getData(), getUnits(),
        getActualTime(), getSystemTime(), getInformationSource().toCoi(), getComment());
  }

  public long getPrimaryKey() { return primaryKey; }

  public void setPrimaryKey(long primaryKey) { this.primaryKey = primaryKey; }

  public UUID getId() { return id; }

  public void setId(UUID id) { this.id = id; }

  public UUID getChannelId() {
    return channelId;
  }

  public void setChannelId(UUID channelId) {
    this.channelId = channelId;
  }

  public String getType() { return type; }

  public void setType(String type) { this.type = type; }

  public byte[] getData() { return data; }

  public void setData(byte[] data) { this.data = data; }

  public String getUnits() { return units; }

  public void setUnits(String units) { this.units = units; }

  public Instant getActualTime() { return actualTime; }

  public void setActualTime(Instant actualTime) { this.actualTime = actualTime; }

  public Instant getSystemTime() { return systemTime; }

  public void setSystemTime(Instant systemTime) { this.systemTime = systemTime; }

  public String getComment() { return comment; }

  public void setComment(String comment) { this.comment = comment; }

  public InformationSourceDao getInformationSource() { return informationSource; }

  public void setInformationSource(InformationSourceDao informationSource) { this.informationSource = informationSource; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceResponseDao that = (ReferenceResponseDao) o;

    if (primaryKey != that.primaryKey) {
      return false;
    }
    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (channelId != null ? !channelId.equals(that.channelId) : that.channelId != null) {
      return false;
    }
    if (type != null ? !type.equals(that.type) : that.type != null) {
      return false;
    }
    if (!Arrays.equals(data, that.data)) {
      return false;
    }
    if (units != null ? !units.equals(that.units) : that.units != null) {
      return false;
    }
    if (actualTime != null ? !actualTime.equals(that.actualTime) : that.actualTime != null) {
      return false;
    }
    if (systemTime != null ? !systemTime.equals(that.systemTime) : that.systemTime != null) {
      return false;
    }
    if (comment != null ? !comment.equals(that.comment) : that.comment != null) {
      return false;
    }
    return informationSource != null ? informationSource.equals(that.informationSource)
        : that.informationSource == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (primaryKey ^ (primaryKey >>> 32));
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (channelId != null ? channelId.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + Arrays.hashCode(data);
    result = 31 * result + (units != null ? units.hashCode() : 0);
    result = 31 * result + (actualTime != null ? actualTime.hashCode() : 0);
    result = 31 * result + (systemTime != null ? systemTime.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (informationSource != null ? informationSource.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceResponseDao{" +
        "primaryKey=" + primaryKey +
        ", id=" + id +
        ", channelId=" + channelId +
        ", type='" + type + '\'' +
        ", data=" + Arrays.toString(data) +
        ", units='" + units + '\'' +
        ", actualTime=" + actualTime +
        ", systemTime=" + systemTime +
        ", comment='" + comment + '\'' +
        ", informationSource=" + informationSource +
        '}';
  }
}
