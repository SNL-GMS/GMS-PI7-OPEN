package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.dataaccessobjects;

import static javax.persistence.CascadeType.ALL;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects.InformationSourceDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.RelativePositionDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceAlias;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.apache.commons.lang3.Validate;


/**
 * Define a Data Access Object to allow read and write access to the relational database.
 */
@Entity
@Table(name="reference_channel")
public class ReferenceChannelDao implements Serializable {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(name="entity_id")
  private UUID entityId;

  @Column(name="version_id", unique = true)
  private UUID versionId;

  @Column(name = "name")
  private String name;

  @Column(name="type")
  private ChannelType type;

  @Column(name="data_type")
  private ChannelDataType dataType;

  @Column(name="location_code")
  private String locationCode;

  @Column(name="latitude")
  private double latitude;

  @Column(name="longitude")
  private double longitude;

  @Column(name="elevation")
  private double elevation;

  @Column(name="depth")
  private double depth;

  @Column(name="vertical_elevation")
  private double verticalElevation;

  @Column(name="horizontal_elevation")
  private double horizontalElevation;

  @Column(name="nominal_sample_rate")
  private double nominalSampleRate;

  @Column(name="actual_time")
  private Instant actualTime;

  @Column(name="system_time")
  private Instant systemTime;

  @Column(name="comment")
  private String comment;

  @Embedded
  private InformationSourceDao informationSource;

  @OneToMany(cascade = ALL)
  private List<ReferenceAliasDao> aliases;

  @Embedded
  private RelativePositionDao position;

  /**
   * Default constructor for JPA.
   */
  public ReferenceChannelDao() {}

  /**
   * Create a DAO from the COI object.
   * @param channel The ReferenceChannel object.
   * @throws NullPointerException
   */
  public ReferenceChannelDao(ReferenceChannel channel) throws NullPointerException {
    Validate.notNull(channel);
    this.entityId = channel.getEntityId();
    this.versionId = channel.getVersionId();
    this.name = channel.getName();
    this.type = channel.getType();
    this.dataType = channel.getDataType();
    this.locationCode = channel.getLocationCode();
    this.latitude = channel.getLatitude();
    this.longitude = channel.getLongitude();
    this.elevation = channel.getElevation();
    this.depth = channel.getDepth();
    this.verticalElevation = channel.getVerticalAngle();
    this.horizontalElevation = channel.getHorizontalAngle();
    this.nominalSampleRate = channel.getNominalSampleRate();
    this.actualTime = channel.getActualTime();
    this.systemTime = channel.getSystemTime();
    this.comment = channel.getComment();
    this.informationSource = new InformationSourceDao(channel.getInformationSource());
    this.position = RelativePositionDao.from(channel.getPosition());
    this.aliases = channel.getAliases().stream()
        .map(ReferenceAliasDao::new)
        .collect(Collectors.toList());
  }


  /**
   * Convert this DAO into its corresponding COI object.
   * @return A ReferenceChannel COI object.
   */
  public ReferenceChannel toCoi() {
    List<ReferenceAlias> aliasList = this.aliases.stream()
        .map(ReferenceAliasDao::toCoi)
        .collect(Collectors.toList());

    return  ReferenceChannel.create(
        getName(), getType(), getDataType(),
        getLocationCode(), getLatitude(), getLongitude(), getElevation(), getDepth(),
        getVerticalElevation(), getHorizontalElevation(), getNominalSampleRate(),
        getActualTime(), getSystemTime(), getInformationSource().toCoi(), getComment(),
        getPosition().toCoi(), aliasList);
  }

  public long getPrimaryKey() { return primaryKey; }

  public void setPrimaryKey(long primaryKey) { this.primaryKey = primaryKey; }

  public UUID getEntityId() { return entityId; }

  public void setEntityId(UUID id) { this.entityId = id; }

  public UUID getVersionId() { return versionId; }

  public void setVersionId(UUID id) { this.versionId = id; }

  public String getName() { return name; }

  public void setName(String name) { this.name = name; }

  public ChannelType getType() { return type; }

  public void setType(ChannelType type) { this.type = type; }

  public ChannelDataType getDataType() { return dataType; }

  public void setDataType(ChannelDataType dataType) { this.dataType = dataType; }

  public String getLocationCode() { return locationCode; }

  public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

  public double getLatitude() { return latitude; }

  public void setLatitude(double latitude) { this.latitude = latitude; }

  public double getLongitude() { return longitude; }

  public void setLongitude(double longitude) { this.longitude = longitude; }

  public double getElevation() { return elevation; }

  public void setElevation(double elevation) { this.elevation = elevation; }

  public double getDepth() { return depth; }

  public void setDepth(double depth) { this.depth = depth; }

  public double getVerticalElevation() { return verticalElevation; }

  public void setVerticalElevation(double verticalElevation) { this.verticalElevation = verticalElevation; }

  public double getHorizontalElevation() { return horizontalElevation; }

  public void setHorizontalElevation(double horizontalElevation) { this.horizontalElevation = horizontalElevation; }

  public double getNominalSampleRate() { return nominalSampleRate; }

  public void setNominalSampleRate(double nominalSampleRate) { this.nominalSampleRate = nominalSampleRate; }

  public Instant getActualTime() { return actualTime; }

  public void setActualTime(Instant actualTime) { this.actualTime = actualTime; }

  public Instant getSystemTime() { return systemTime; }

  public void setSystemTime(Instant systemTime) { this.systemTime = systemTime; }

  public String getComment() { return comment; }

  public void setComment(String comment) { this.comment = comment; }

  public InformationSourceDao getInformationSource() { return informationSource; }

  public void setInformationSource(InformationSourceDao informationSource) { this.informationSource = informationSource; }

  public RelativePositionDao getPosition() {
    return position;
  }

  public void setAliases(List<ReferenceAliasDao> aliases) {
    this.aliases = aliases;
  }

  public List<ReferenceAliasDao> getAliases() {
    return aliases; }

  public void setPosition(RelativePositionDao position) {
    this.position = position;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReferenceChannelDao that = (ReferenceChannelDao) o;
    return primaryKey == that.primaryKey &&
        Double.compare(that.latitude, latitude) == 0 &&
        Double.compare(that.longitude, longitude) == 0 &&
        Double.compare(that.elevation, elevation) == 0 &&
        Double.compare(that.depth, depth) == 0 &&
        Double.compare(that.verticalElevation, verticalElevation) == 0 &&
        Double.compare(that.horizontalElevation, horizontalElevation) == 0 &&
        Double.compare(that.nominalSampleRate, nominalSampleRate) == 0 &&
        Objects.equals(entityId, that.entityId) &&
        Objects.equals(versionId, that.versionId) &&
        Objects.equals(name, that.name) &&
        type == that.type &&
        dataType == that.dataType &&
        Objects.equals(locationCode, that.locationCode) &&
        Objects.equals(actualTime, that.actualTime) &&
        Objects.equals(systemTime, that.systemTime) &&
        Objects.equals(comment, that.comment) &&
        Objects.equals(informationSource, that.informationSource) &&
        Objects.equals(aliases, that.aliases) &&
        Objects.equals(position, that.position);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(primaryKey, entityId, versionId, name, type, dataType, locationCode, latitude,
            longitude, elevation, depth, verticalElevation, horizontalElevation, nominalSampleRate,
            actualTime, systemTime, comment, informationSource, aliases, position);
  }

  @Override
  public String toString() {
    return "ReferenceChannelDao{" +
        "primaryKey=" + primaryKey +
        ", entityId=" + entityId +
        ", versionId=" + versionId +
        ", name='" + name + '\'' +
        ", type=" + type +
        ", dataType=" + dataType +
        ", locationCode=" + locationCode +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", elevation=" + elevation +
        ", depth=" + depth +
        ", verticalElevation=" + verticalElevation +
        ", horizontalElevation=" + horizontalElevation +
        ", nominalSampleRate=" + nominalSampleRate +
        ", actualTime=" + actualTime +
        ", systemTime=" + systemTime +
        ", comment='" + comment + '\'' +
        ", informationSource=" + informationSource +
        ", aliases=" + aliases +
        ", position=" + position +
        '}';
  }
}
