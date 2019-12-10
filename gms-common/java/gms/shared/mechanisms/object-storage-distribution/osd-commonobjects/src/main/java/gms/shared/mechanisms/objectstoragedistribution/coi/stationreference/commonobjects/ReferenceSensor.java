package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


/**
 * A channel is an abstract entity specifying a basic type of measurement capability;
 * the actual hardware that produces the data stream for that channel is an instrument.
 * The sensor class has a manufacturer, a model, a serial number, and nominal calibration
 * and response information (i.e., the specifications provided by the manufacturer), which
 * are captured in the Calibration and Response classes. While the type of information a
 * channel records will not change, the actual instrument used may (e.g., an upgrade to a
 * more current model); hence Channel can point to more than one Instrument, and Instrument
 * includes on time and off time as attributes.
 */
public final class ReferenceSensor {

  private final UUID id;
  private final UUID channelId;
  private final String instrumentManufacturer;
  private final String instrumentModel;
  private final String serialNumber;
  private final int numberOfComponents;
  private final double cornerPeriod;
  private final double lowPassband;
  private final double highPassband;
  private final Instant actualTime;
  private final Instant systemTime;
  private final InformationSource informationSource;
  private final String comment;

  /**
   * Create a new ReferenceSensor.
   *
   * @param actualTime The date and time the information was originally generated.
   * @param systemChangeTime The date and time time the information was entered into the system
   * @param informationSource The source of this information.
   * @param comment A comment.
   * @return A new ReferenceSensor object.
   */
  public static ReferenceSensor create(UUID channelId, String instrumentManufacturer,
      String instrumentModel, String serialNumber, int numberOfComponents,
      double cornerPeriod, double lowPassband, double highPassband,
      Instant actualTime, Instant systemChangeTime,
      InformationSource informationSource, String comment) {

    return new ReferenceSensor(channelId, instrumentManufacturer, instrumentModel,
        serialNumber, numberOfComponents, cornerPeriod, lowPassband, highPassband,
        actualTime, systemChangeTime, informationSource, comment);
  }

  /**
   * Create a new ReferenceSensor.
   *
   * @param actualTime The date and time the information was originally generated.
   * @param systemTime The date and time the information was entered into the system.
   * @param informationSource The source of this information.
   * @param comment A comment.
   * @return A new ReferenceSensor object.
   */
  public static ReferenceSensor from(UUID id, UUID channelId, String instrumentManufacturer,
      String instrumentModel, String serialNumber, int numberOfComponents,
      double cornerPeriod, double lowPassband, double highPassband,
      Instant actualTime, Instant systemTime, InformationSource informationSource, String comment) {
    return new ReferenceSensor(id, channelId, instrumentManufacturer, instrumentModel,
        serialNumber, numberOfComponents, cornerPeriod, lowPassband, highPassband,
        actualTime, systemTime, informationSource, comment);
  }

  private ReferenceSensor(UUID channelId, String instrumentManufacturer,
      String instrumentModel, String serialNumber, int numberOfComponents,
      double cornerPeriod, double lowPassband, double highPassband,
      Instant actualTime, Instant systemTime, InformationSource informationSource, String comment)
      throws NullPointerException, InvalidParameterException {

    this.channelId = Objects.requireNonNull(channelId);
    this.instrumentManufacturer = Objects.requireNonNull(instrumentManufacturer);
    this.instrumentModel = Objects.requireNonNull(instrumentModel);
    this.serialNumber = Objects.requireNonNull(serialNumber);
    this.numberOfComponents = numberOfComponents;
    this.cornerPeriod = cornerPeriod;
    this.lowPassband = lowPassband;
    this.highPassband = highPassband;
    this.actualTime = Objects.requireNonNull(actualTime);
    this.systemTime = Objects.requireNonNull(systemTime);
    this.informationSource = Objects.requireNonNull(informationSource);
    this.comment = Objects.requireNonNull(comment);
    this.id = UUID.nameUUIDFromBytes(
        (this.channelId + this.instrumentManufacturer + this.instrumentModel
        + this.serialNumber + this.numberOfComponents + this.cornerPeriod
        + this.lowPassband + this.highPassband + this.actualTime + this.systemTime)
            .getBytes(StandardCharsets.UTF_16LE));
  }

  /**
   * Private constructor.
   */
  private ReferenceSensor(UUID id, UUID channelId, String instrumentManufacturer,
      String instrumentModel, String serialNumber, int numberOfComponents,
      double cornerPeriod, double lowPassband, double highPassband,
      Instant actualTime, Instant systemTime, InformationSource informationSource, String comment)
      throws NullPointerException, InvalidParameterException {

    this.id = Objects.requireNonNull(id);
    this.channelId = Objects.requireNonNull(channelId);
    this.instrumentManufacturer = Objects.requireNonNull(instrumentManufacturer);
    this.instrumentModel = Objects.requireNonNull(instrumentModel);
    this.serialNumber = Objects.requireNonNull(serialNumber);
    this.numberOfComponents = numberOfComponents;
    this.cornerPeriod = cornerPeriod;
    this.lowPassband = lowPassband;
    this.highPassband = highPassband;
    this.actualTime = Objects.requireNonNull(actualTime);
    this.systemTime = Objects.requireNonNull(systemTime);
    this.informationSource = Objects.requireNonNull(informationSource);
    this.comment = Objects.requireNonNull(comment);
  }

  public UUID getId() { return id; }

  public UUID getChannelId() {
    return channelId;
  }

  public String getInstrumentManufacturer() { return instrumentManufacturer; }

  public String getInstrumentModel() { return instrumentModel; }

  public String getSerialNumber() { return serialNumber; }

  public int getNumberOfComponents() { return numberOfComponents; }

  public double getCornerPeriod() { return cornerPeriod; }

  public double getLowPassband() { return lowPassband; }

  public double getHighPassband() { return highPassband; }

  public Instant getActualTime() { return actualTime; }

  public Instant getSystemTime() { return systemTime; }

  public InformationSource getInformationSource() { return informationSource; }

  public String getComment() { return comment; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceSensor that = (ReferenceSensor) o;

    if (numberOfComponents != that.numberOfComponents) {
      return false;
    }
    if (Double.compare(that.cornerPeriod, cornerPeriod) != 0) {
      return false;
    }
    if (Double.compare(that.lowPassband, lowPassband) != 0) {
      return false;
    }
    if (Double.compare(that.highPassband, highPassband) != 0) {
      return false;
    }
    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (channelId != null ? !channelId.equals(that.channelId) : that.channelId != null) {
      return false;
    }
    if (instrumentManufacturer != null ? !instrumentManufacturer.equals(that.instrumentManufacturer)
        : that.instrumentManufacturer != null) {
      return false;
    }
    if (instrumentModel != null ? !instrumentModel.equals(that.instrumentModel)
        : that.instrumentModel != null) {
      return false;
    }
    if (serialNumber != null ? !serialNumber.equals(that.serialNumber)
        : that.serialNumber != null) {
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
    result = 31 * result + (informationSource != null ? informationSource.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceSensor{" +
        "id=" + id +
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
        ", informationSource=" + informationSource +
        ", comment='" + comment + '\'' +
        '}';
  }
}

