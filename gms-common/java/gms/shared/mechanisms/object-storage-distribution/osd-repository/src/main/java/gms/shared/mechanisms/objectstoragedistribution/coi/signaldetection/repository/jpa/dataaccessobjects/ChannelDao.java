package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.ChannelDataTypeConverter;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.ChannelTypeConverter;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * JPA data access object for {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel}
 */
@Entity
@Table(name = "channel")
public class ChannelDao {

  @Id
  @GeneratedValue
  private long daoId;

  @Column()
  private UUID id;
  private String name;

  @Convert(converter = ChannelTypeConverter.class)
  private ChannelType channelType;

  @Convert(converter = ChannelDataTypeConverter.class)
  private ChannelDataType dataType;

  private double latitude;
  private double longitude;
  private double elevation;
  private double depth;
  private double verticalAngle;
  private double horizontalAngle;
  private double sampleRate;

  public ChannelDao() {
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ChannelType getChannelType() {
    return channelType;
  }

  public void setChannelType(ChannelType channelType) {
    this.channelType = channelType;
  }

  public ChannelDataType getDataType() {
    return dataType;
  }

  public void setDataType(ChannelDataType dataType) {
    this.dataType = dataType;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public double getElevation() {
    return elevation;
  }

  public void setElevation(double elevation) {
    this.elevation = elevation;
  }

  public double getDepth() {
    return depth;
  }

  public void setDepth(double depth) {
    this.depth = depth;
  }

  public double getVerticalAngle() {
    return verticalAngle;
  }

  public void setVerticalAngle(double verticalAngle) {
    this.verticalAngle = verticalAngle;
  }

  public double getHorizontalAngle() {
    return horizontalAngle;
  }

  public void setHorizontalAngle(double horizontalAngle) {
    this.horizontalAngle = horizontalAngle;
  }

  public double getSampleRate() {
    return sampleRate;
  }

  public void setSampleRate(double sampleRate) {
    this.sampleRate = sampleRate;
  }

  @Override
  public String toString() {
    return "ChannelDao{" +
        "daoId=" + daoId +
        ", id=" + id +
        ", name='" + name + '\'' +
        ", channelType=" + channelType +
        ", dataType=" + dataType +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", elevation=" + elevation +
        ", depth=" + depth +
        ", verticalAngle=" + verticalAngle +
        ", horizontalAngle=" + horizontalAngle +
        ", sampleRate=" + sampleRate +
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
    ChannelDao that = (ChannelDao) o;
    return daoId == that.daoId &&
        Double.compare(that.latitude, latitude) == 0 &&
        Double.compare(that.longitude, longitude) == 0 &&
        Double.compare(that.elevation, elevation) == 0 &&
        Double.compare(that.depth, depth) == 0 &&
        Double.compare(that.verticalAngle, verticalAngle) == 0 &&
        Double.compare(that.horizontalAngle, horizontalAngle) == 0 &&
        Double.compare(that.sampleRate, sampleRate) == 0 &&
        Objects.equals(id, that.id) &&
        Objects.equals(name, that.name) &&
        channelType == that.channelType &&
        dataType == that.dataType;
  }

  @Override
  public int hashCode() {

    return Objects
        .hash(daoId, id, name, channelType, dataType,
            latitude, longitude, elevation, depth,
            verticalAngle, horizontalAngle, sampleRate);
  }
}
