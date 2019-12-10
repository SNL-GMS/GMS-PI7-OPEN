package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * As with the manufacturer-provided calibration information, this calibration information is stored
 * in the Calibration and Response classes. Calibration information is used to convert the output of
 * the instrument (e.g., volts, counts) into the phenomenon that the instrument is measuring (e.g.,
 * seismic ground displacement). The Calibration class includes information about when an instrument
 * was calibrated (actual change time) and what the response (calibration factor) was for a
 * particular calibration period (i.e., inverse of frequency). Response includes the full response
 * function across a range of periods/frequencies. The actual change time attribute in both the
 * Calibration and Response classes captures when the calibration was actually performed, and both
 * classes also include system change time as attributes, in order to track when the response
 * information was available for use by the System.
 */
public final class ReferenceCalibration {

  private final UUID id;
  private final UUID channelId;
  private final double calibrationInterval;
  private final double calibrationFactor;
  private final double calibrationFactorError;
  private final double calibrationPeriod;
  private final double timeShift;
  private final Instant actualTime;
  private final Instant systemTime;
  private final InformationSource informationSource;
  private final String comment;

  /**
   * Create a new ReferenceCalibration.
   *
   * @param actualTime The date and time the information was originally generated.
   * @param informationSource The source of this information.
   * @param comment A comment.
   * @return A new ReferenceResponse object.
   */
  public static ReferenceCalibration create(UUID channelId, double calibrationInterval,
      double calibrationFactor, double calibrationFactorError, double calibrationPeriod,
      double timeShift, Instant actualTime, Instant systemTime, InformationSource informationSource,
      String comment) {

    return new ReferenceCalibration(channelId, calibrationInterval, calibrationFactor,
        calibrationFactorError, calibrationPeriod, timeShift,
        actualTime, systemTime, informationSource, comment);
  }

  /**
   * Create a new ReferenceCalibration.
   *
   * @param actualTime The date and time the information was originally generated.
   * @param systemTime The date and time the information was entered into the system.
   * @param informationSource The source of this information.
   * @param comment A comment.
   * @return A new ReferenceCalibration object.
   */
  public static ReferenceCalibration from(UUID id, UUID channelId,
      double calibrationInterval, double calibrationFactor,
      double calibrationFactorError, double calibrationPeriod, double timeShift,
      Instant actualTime, Instant systemTime, InformationSource informationSource, String comment) {
    return new ReferenceCalibration(id, channelId, calibrationInterval, calibrationFactor,
        calibrationFactorError, calibrationPeriod, timeShift,
        actualTime, systemTime, informationSource, comment);
  }

  private ReferenceCalibration(UUID channelId, double calibrationInterval, double calibrationFactor,
      double calibrationFactorError, double calibrationPeriod, double timeShift,
      Instant actualTime, Instant systemTime, InformationSource informationSource, String comment)
      throws NullPointerException, InvalidParameterException {

    this.channelId = Objects.requireNonNull(channelId);
    this.calibrationInterval = calibrationInterval;
    this.calibrationFactor = calibrationFactor;
    this.calibrationFactorError = calibrationFactorError;
    this.calibrationPeriod = calibrationPeriod;
    this.timeShift = timeShift;
    this.actualTime = Objects.requireNonNull(actualTime);
    this.systemTime = Objects.requireNonNull(systemTime);
    this.informationSource = Objects.requireNonNull(informationSource);
    this.comment = Objects.requireNonNull(comment);
    this.id = UUID.nameUUIDFromBytes(
        ((this.channelId.toString() + this.calibrationInterval + this.calibrationFactor
            + this.calibrationFactorError + this.calibrationPeriod + this.timeShift
            + this.actualTime + this.systemTime))
            .getBytes(StandardCharsets.UTF_16LE));
  }

  /**
   * Private constructor.
   */
  private ReferenceCalibration(UUID id, UUID channelId, double calibrationInterval,
      double calibrationFactor,
      double calibrationFactorError, double calibrationPeriod, double timeShift,
      Instant actualTime, Instant systemTime, InformationSource informationSource, String comment)
      throws NullPointerException, InvalidParameterException {

    this.id = Objects.requireNonNull(id);
    this.channelId = Objects.requireNonNull(channelId);
    this.calibrationInterval = calibrationInterval;
    this.calibrationFactor = calibrationFactor;
    this.calibrationFactorError = calibrationFactorError;
    this.calibrationPeriod = calibrationPeriod;
    this.timeShift = timeShift;
    this.actualTime = Objects.requireNonNull(actualTime);
    this.systemTime = Objects.requireNonNull(systemTime);
    this.informationSource = Objects.requireNonNull(informationSource);
    this.comment = Objects.requireNonNull(comment);
  }

  public UUID getId() {
    return id;
  }

  public UUID getChannelId() {
    return channelId;
  }

  public double getCalibrationInterval() {
    return calibrationInterval;
  }

  public double getCalibrationFactor() {
    return calibrationFactor;
  }

  public double getCalibrationFactorError() {
    return calibrationFactorError;
  }

  public double getCalibrationPeriod() {
    return calibrationPeriod;
  }

  public double getTimeShift() {
    return timeShift;
  }

  public Instant getActualTime() {
    return actualTime;
  }

  public Instant getSystemTime() {
    return systemTime;
  }

  public InformationSource getInformationSource() {
    return informationSource;
  }

  public String getComment() {
    return comment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceCalibration that = (ReferenceCalibration) o;

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
    if (informationSource != null ? !informationSource.equals(that.informationSource)
        : that.informationSource != null) {
      return false;
    }
    return comment != null ? comment.equals(that.comment) : that.comment == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = id != null ? id.hashCode() : 0;
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
    result = 31 * result + (informationSource != null ? informationSource.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceCalibration{" +
        "id=" + id +
        ", channelId=" + channelId +
        ", calibrationInterval=" + calibrationInterval +
        ", calibrationFactor=" + calibrationFactor +
        ", calibrationFactorError=" + calibrationFactorError +
        ", calibrationPeriod=" + calibrationPeriod +
        ", timeShift=" + timeShift +
        ", actualTime=" + actualTime +
        ", systemTime=" + systemTime +
        ", informationSource=" + informationSource +
        ", comment='" + comment + '\'' +
        '}';
  }
}
