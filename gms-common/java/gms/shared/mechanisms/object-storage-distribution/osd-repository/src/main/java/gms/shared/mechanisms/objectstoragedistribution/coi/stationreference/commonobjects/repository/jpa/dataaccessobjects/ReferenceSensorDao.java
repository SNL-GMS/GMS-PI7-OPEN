package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects.InformationSourceDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSensor;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.apache.commons.lang3.Validate;

@Entity
@Table(name="reference_sensor")
public class ReferenceSensorDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(unique = true)
  private UUID id;

  @Column()
  private UUID channelId;

  @Column(name="instrument_manufacturer")
  private String instrumentManufacturer;

  @Column(name="instrument_model")
  private String instrumentModel;

  @Column(name="serial_number")
  private String serialNumber;

  @Column(name="number_of_components")
  private int numberOfComponents;

  @Column(name="corner_period")
  private double cornerPeriod;

  @Column(name="low_passband")
  private double lowPassband;

  @Column(name="high_passband")
  private double highPassband;

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
  public ReferenceSensorDao() {}

  /**
   * Create a DAO from the COI object.
   * @param sensor The ReferenceSensor object.
   * @throws NullPointerException
   */
  public ReferenceSensorDao(ReferenceSensor sensor) throws NullPointerException {
    Validate.notNull(sensor);
    this.id = sensor.getId();
    this.channelId = sensor.getChannelId();
    this.instrumentManufacturer = sensor.getInstrumentManufacturer();
    this.instrumentModel = sensor.getInstrumentModel();
    this.serialNumber = sensor.getSerialNumber();
    this.numberOfComponents = sensor.getNumberOfComponents();
    this.cornerPeriod = sensor.getCornerPeriod();
    this.lowPassband = sensor.getLowPassband();
    this.highPassband = sensor.getHighPassband();
    this.actualTime = sensor.getActualTime();
    this.systemTime = sensor.getSystemTime();
    this.comment = sensor.getComment();
    this.informationSource = new InformationSourceDao(sensor.getInformationSource());
  }

  /**
   * Convert this DAO into its corresponding COI object.
   * @return A ReferenceSensor COI object.
   */
  public ReferenceSensor toCoi() {
    return  ReferenceSensor.from(getId(), getChannelId(), getInstrumentManufacturer(), getInstrumentModel(),
        getSerialNumber(), getNumberOfComponents(), getCornerPeriod(), getLowPassband(), getHighPassband(),
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

  public String getInstrumentManufacturer() { return instrumentManufacturer; }

  public void setInstrumentManufacturer(String instrumentManufacturer) { this.instrumentManufacturer = instrumentManufacturer; }

  public String getInstrumentModel() { return instrumentModel; }

  public void setInstrumentModel(String instrumentModel) { this.instrumentModel = instrumentModel; }

  public String getSerialNumber() { return serialNumber; }

  public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

  public int getNumberOfComponents() { return numberOfComponents; }

  public void setNumberOfComponents(int numberOfComponents) { this.numberOfComponents = numberOfComponents; }

  public double getCornerPeriod() { return cornerPeriod; }

  public void setCornerPeriod(double cornerPeriod) { this.cornerPeriod = cornerPeriod; }

  public double getLowPassband() { return lowPassband; }

  public void setLowPassband(double lowPassband) { this.lowPassband = lowPassband; }

  public double getHighPassband() { return highPassband; }

  public void setHighPassband(double highPassband) { this.highPassband = highPassband; }

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

    ReferenceSensorDao sensorDao = (ReferenceSensorDao) o;

    if (primaryKey != sensorDao.primaryKey) {
      return false;
    }
    if (numberOfComponents != sensorDao.numberOfComponents) {
      return false;
    }
    if (Double.compare(sensorDao.cornerPeriod, cornerPeriod) != 0) {
      return false;
    }
    if (Double.compare(sensorDao.lowPassband, lowPassband) != 0) {
      return false;
    }
    if (Double.compare(sensorDao.highPassband, highPassband) != 0) {
      return false;
    }
    if (id != null ? !id.equals(sensorDao.id) : sensorDao.id != null) {
      return false;
    }
    if (channelId != null ? !channelId.equals(sensorDao.channelId) : sensorDao.channelId != null) {
      return false;
    }
    if (instrumentManufacturer != null ? !instrumentManufacturer
        .equals(sensorDao.instrumentManufacturer) : sensorDao.instrumentManufacturer != null) {
      return false;
    }
    if (instrumentModel != null ? !instrumentModel.equals(sensorDao.instrumentModel)
        : sensorDao.instrumentModel != null) {
      return false;
    }
    if (serialNumber != null ? !serialNumber.equals(sensorDao.serialNumber)
        : sensorDao.serialNumber != null) {
      return false;
    }
    if (actualTime != null ? !actualTime.equals(sensorDao.actualTime)
        : sensorDao.actualTime != null) {
      return false;
    }
    if (systemTime != null ? !systemTime.equals(sensorDao.systemTime)
        : sensorDao.systemTime != null) {
      return false;
    }
    if (comment != null ? !comment.equals(sensorDao.comment) : sensorDao.comment != null) {
      return false;
    }
    return informationSource != null ? informationSource.equals(sensorDao.informationSource)
        : sensorDao.informationSource == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = (int) (primaryKey ^ (primaryKey >>> 32));
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (channelId != null ? channelId.hashCode() : 0);
    result = 31 * result + (instrumentManufacturer != null ? instrumentManufacturer.hashCode() : 0);
    result = 31 * result + (instrumentModel != null ? instrumentModel.hashCode() : 0);
    result = 31 * result + (serialNumber != null ? serialNumber.hashCode() : 0);
    result = 31 * result + numberOfComponents;
    temp = Double.doubleToLongBits(cornerPeriod);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(lowPassband);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(highPassband);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (actualTime != null ? actualTime.hashCode() : 0);
    result = 31 * result + (systemTime != null ? systemTime.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (informationSource != null ? informationSource.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceSensorDao{" +
        "primaryKey=" + primaryKey +
        ", id=" + id +
        ", channelId=" + channelId +
        ", instrumentManufacturer='" + instrumentManufacturer + '\'' +
        ", instrumentModel='" + instrumentModel + '\'' +
        ", serialNumber='" + serialNumber + '\'' +
        ", numberOfComponents=" + numberOfComponents +
        ", cornerPeriod=" + cornerPeriod +
        ", lowPassband=" + lowPassband +
        ", highPassband=" + highPassband +
        ", actualTime=" + actualTime +
        ", systemTime=" + systemTime +
        ", comment='" + comment + '\'' +
        ", informationSource=" + informationSource +
        '}';
  }
}
