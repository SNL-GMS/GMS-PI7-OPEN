package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects.InformationSourceDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceCalibration;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.apache.commons.lang3.Validate;

/**
 * Define a Data Access Object to allow read and write access to the relational database.
 */
@Entity
@Table(name="reference_calibration")
public class ReferenceCalibrationDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(unique = true)
  private UUID id;

  @Column()
  private UUID channelId;

  @Column(name="calibration_interval")
  private double calibrationInterval;

  @Column(name="calibration_factor")
  private double calibrationFactor;

  @Column(name="calibration_factor_error")
  private double calibrationFactorError;

  @Column(name="calibration_period")
  private double calibrationPeriod;

  @Column(name="time_shift")
  private double timeShift;

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
  public ReferenceCalibrationDao() {}

  /**
   * Create a DAO from the COI object.
   * @param calibration The ReferenceCalibration object.
   * @throws NullPointerException
   */
  public ReferenceCalibrationDao(ReferenceCalibration calibration) throws NullPointerException {
    Validate.notNull(calibration);
    this.id = calibration.getId();
    this.channelId = calibration.getChannelId();
    this.calibrationInterval = calibration.getCalibrationInterval();
    this.calibrationFactor = calibration.getCalibrationFactor();
    this.calibrationFactorError = calibration.getCalibrationFactorError();
    this.calibrationPeriod = calibration.getCalibrationPeriod();
    this.timeShift = calibration.getTimeShift();
    this.actualTime = calibration.getActualTime();
    this.systemTime = calibration.getSystemTime();
    this.comment = calibration.getComment();
    this.informationSource = new InformationSourceDao(calibration.getInformationSource());
  }

  /**
   * Convert this DAO into its corresponding COI object.
   * @return A ReferenceCalibration COI object.
   */
  public ReferenceCalibration toCoi() {
    return  ReferenceCalibration.from(getId(), getChannelId(), getCalibrationInterval(), getCalibrationFactor(),
        getCalibrationFactorError(), getCalibrationPeriod(), getTimeShift(),
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

  public double getCalibrationInterval() { return calibrationInterval; }

  public void setCalibrationInterval(double calibrationInterval) { this.calibrationInterval = calibrationInterval; }

  public double getCalibrationFactor() { return calibrationFactor; }

  public void setCalibrationFactor(double calibrationFactor) { this.calibrationFactor = calibrationFactor; }

  public double getCalibrationFactorError() { return calibrationFactorError; }

  public void setCalibrationFactorError(double calibrationFactorError) { this.calibrationFactorError = calibrationFactorError; }

  public double getCalibrationPeriod() { return calibrationPeriod; }

  public void setCalibrationPeriod(double calibrationPeriod) { this.calibrationPeriod = calibrationPeriod; }

  public double getTimeShift() { return timeShift; }

  public void setTimeShift(double timeShift) { this.timeShift = timeShift; }

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

    ReferenceCalibrationDao that = (ReferenceCalibrationDao) o;

    if (primaryKey != that.primaryKey) {
      return false;
    }
    if (Double.compare(that.calibrationInterval, calibrationInterval) != 0) {
      return false;
    }
    if (Double.compare(that.calibrationFactor, calibrationFactor) != 0) {
      return false;
    }
    if (Double.compare(that.calibrationFactorError, calibrationFactorError) != 0) {
      return false;
    }
    if (Double.compare(that.calibrationPeriod, calibrationPeriod) != 0) {
      return false;
    }
    if (Double.compare(that.timeShift, timeShift) != 0) {
      return false;
    }
    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (channelId != null ? !channelId.equals(that.channelId) : that.channelId != null) {
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
    int result;
    long temp;
    result = (int) (primaryKey ^ (primaryKey >>> 32));
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (channelId != null ? channelId.hashCode() : 0);
    temp = Double.doubleToLongBits(calibrationInterval);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(calibrationFactor);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(calibrationFactorError);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(calibrationPeriod);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(timeShift);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (actualTime != null ? actualTime.hashCode() : 0);
    result = 31 * result + (systemTime != null ? systemTime.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (informationSource != null ? informationSource.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceCalibrationDao{" +
        "primaryKey=" + primaryKey +
        ", id=" + id +
        ", channelId=" + channelId +
        ", calibrationInterval=" + calibrationInterval +
        ", calibrationFactor=" + calibrationFactor +
        ", calibrationFactorError=" + calibrationFactorError +
        ", calibrationPeriod=" + calibrationPeriod +
        ", timeShift=" + timeShift +
        ", actualTime=" + actualTime +
        ", systemTime=" + systemTime +
        ", comment='" + comment + '\'' +
        ", informationSource=" + informationSource +
        '}';
  }
}
