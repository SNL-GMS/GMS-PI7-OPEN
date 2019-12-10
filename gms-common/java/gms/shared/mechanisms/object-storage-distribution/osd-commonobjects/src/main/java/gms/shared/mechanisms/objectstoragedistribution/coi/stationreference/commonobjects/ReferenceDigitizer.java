package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Define a class which represents a digitizer
 */
public final class ReferenceDigitizer {

  private final UUID entityId;
  private final UUID versionId;
  private final String name;
  private final String manufacturer;
  private final String model;
  private final String serialNumber;
  private final Instant actualChangeTime;
  private final Instant systemChangeTime;
  private final InformationSource informationSource;
  private final String description;
  private final String comment;

  /**
   * Create a new ReferenceDigitizer object
   *
   * @param name the name of the digitizer
   * @param manufacturer Digitizer manufacturer
   * @param model Digitizer model
   * @param serialNumber Digitizer serialNumber
   * @param actualChangeTime The date and time time the information was originally generated
   * @param systemChangeTime The date and time time the information was entered into the system
   * @param informationSource The source of this information
   * @param comment Comments
   * @param description a description
   * @return A new ReferenceDigitizer object
   */
  public static ReferenceDigitizer create(String name, String manufacturer, String model,
      String serialNumber, Instant actualChangeTime, Instant systemChangeTime,
      InformationSource informationSource, String comment, String description)
      throws NullPointerException {

    return new ReferenceDigitizer(name,
        manufacturer, model, serialNumber, actualChangeTime,
        systemChangeTime, informationSource, comment, description);
  }

  /**
   * Create a ReferenceDigitizer object from existing data.
   *
   * @param entityId the id of the entity
   * @param versionId the id of the version of the entity
   * @param name the name of the digitizer
   * @param manufacturer Digitizer manufacturer
   * @param model Digitizer model
   * @param serialNumber Digitizer serialNumber
   * @param actualChangeTime The date and time time the information was originally generated
   * @param systemChangeTime The date and time time the information was entered into the system
   * @param informationSource The source of this information
   * @param comment Comments
   * @param description a description
   * @return A new ReferenceDigitizer object
   */
  public static ReferenceDigitizer from(UUID entityId, UUID versionId,
      String name, String manufacturer, String model, String serialNumber,
      Instant actualChangeTime, Instant systemChangeTime,
      InformationSource informationSource, String comment, String description)
      throws NullPointerException {

    return new ReferenceDigitizer(entityId, versionId, name, manufacturer,
        model, serialNumber, actualChangeTime,
        systemChangeTime, informationSource, comment, description);
  }

  private ReferenceDigitizer(String name, String manufacturer, String model,
      String serialNumber, Instant actualChangeTime, Instant systemChangeTime,
      InformationSource informationSource, String comment, String description)
      throws NullPointerException {

    this.name = Objects.requireNonNull(name);
    this.manufacturer = Objects.requireNonNull(manufacturer);
    this.model = Objects.requireNonNull(model);
    this.serialNumber = Objects.requireNonNull(serialNumber);
    this.actualChangeTime = Objects.requireNonNull(actualChangeTime);
    this.systemChangeTime = Objects.requireNonNull(systemChangeTime);
    this.informationSource = Objects.requireNonNull(informationSource);
    this.comment = Objects.requireNonNull(comment);
    this.description = Objects.requireNonNull(description);
    this.entityId = UUID.nameUUIDFromBytes((this.manufacturer + this.model + this.serialNumber)
        .getBytes(StandardCharsets.UTF_16LE));
    this.versionId = UUID.nameUUIDFromBytes(
        (this.name + this.manufacturer + this.model + this.serialNumber
        + this.actualChangeTime).getBytes(StandardCharsets.UTF_16LE));
  }

  // Private Constructor
  private ReferenceDigitizer(UUID entityId, UUID versionId, String name, String manufacturer,
      String model,
      String serialNumber, Instant actualChangeTime, Instant systemChangeTime,
      InformationSource informationSource, String comment, String description)
      throws NullPointerException {

    this.entityId = Objects.requireNonNull(entityId);
    this.versionId = Objects.requireNonNull(versionId);
    this.name = Objects.requireNonNull(name);
    this.manufacturer = Objects.requireNonNull(manufacturer);
    this.model = Objects.requireNonNull(model);
    this.serialNumber = Objects.requireNonNull(serialNumber);
    this.actualChangeTime = Objects.requireNonNull(actualChangeTime);
    this.systemChangeTime = Objects.requireNonNull(systemChangeTime);
    this.informationSource = Objects.requireNonNull(informationSource);
    this.comment = Objects.requireNonNull(comment);
    this.description = Objects.requireNonNull(description);
  }

  public UUID getEntityId() {
    return entityId;
  }

  public UUID getVersionId() {
    return versionId;
  }

  public String getName() {
    return name;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public String getModel() {
    return model;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public Instant getActualChangeTime() {
    return actualChangeTime;
  }

  public Instant getSystemChangeTime() {
    return systemChangeTime;
  }

  public InformationSource getInformationSource() {
    return informationSource;
  }

  public String getComment() {
    return comment;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceDigitizer that = (ReferenceDigitizer) o;

    if (entityId != null ? !entityId.equals(that.entityId) : that.entityId != null) {
      return false;
    }
    if (versionId != null ? !versionId.equals(that.versionId) : that.versionId != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (manufacturer != null ? !manufacturer.equals(that.manufacturer)
        : that.manufacturer != null) {
      return false;
    }
    if (model != null ? !model.equals(that.model) : that.model != null) {
      return false;
    }
    if (serialNumber != null ? !serialNumber.equals(that.serialNumber)
        : that.serialNumber != null) {
      return false;
    }
    if (actualChangeTime != null ? !actualChangeTime.equals(that.actualChangeTime)
        : that.actualChangeTime != null) {
      return false;
    }
    if (systemChangeTime != null ? !systemChangeTime.equals(that.systemChangeTime)
        : that.systemChangeTime != null) {
      return false;
    }
    if (informationSource != null ? !informationSource.equals(that.informationSource)
        : that.informationSource != null) {
      return false;
    }
    if (description != null ? !description.equals(that.description) : that.description != null) {
      return false;
    }
    return comment != null ? comment.equals(that.comment) : that.comment == null;
  }

  @Override
  public int hashCode() {
    int result = entityId != null ? entityId.hashCode() : 0;
    result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (manufacturer != null ? manufacturer.hashCode() : 0);
    result = 31 * result + (model != null ? model.hashCode() : 0);
    result = 31 * result + (serialNumber != null ? serialNumber.hashCode() : 0);
    result = 31 * result + (actualChangeTime != null ? actualChangeTime.hashCode() : 0);
    result = 31 * result + (systemChangeTime != null ? systemChangeTime.hashCode() : 0);
    result = 31 * result + (informationSource != null ? informationSource.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceDigitizer{" +
        "entityId=" + entityId +
        ", versionId=" + versionId +
        ", name='" + name + '\'' +
        ", manufacturer='" + manufacturer + '\'' +
        ", model='" + model + '\'' +
        ", serialNumber='" + serialNumber + '\'' +
        ", actualChangeTime=" + actualChangeTime +
        ", systemChangeTime=" + systemChangeTime +
        ", informationSource=" + informationSource +
        ", description='" + description + '\'' +
        ", comment='" + comment + '\'' +
        '}';
  }
}
