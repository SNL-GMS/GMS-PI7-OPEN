package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects.InformationSourceDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceDigitizer;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="reference_digitizer")
public class ReferenceDigitizerDao {
  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(name = "entity_id")
  private UUID entityId;

  @Column(name = "version_id", unique = true)
  private UUID versionId;

  @Column
  private String name;

  @Column
  private String manufacturer;

  @Column
  private String model;

  @Column(name = "serialNumber")
  private String serialNumber;

  @Column(name="actual_time")
  private Instant actualTime;

  @Column(name="system_time")
  private Instant systemTime;

  @Column
  private String description;

  @Column
  private String comment;

  @Embedded
  private InformationSourceDao informationSource;

  /**
   * Default constructor for JPA.
   */
  public ReferenceDigitizerDao() {  }

   /**
   * Create a DAO from the corresponding COI object.
   * @param digitizer The ReferenceDigitizer object.
   * @throws NullPointerException
   */
  public ReferenceDigitizerDao(ReferenceDigitizer digitizer) throws NullPointerException {
    Objects.requireNonNull(digitizer);
    this.entityId = digitizer.getEntityId();
    this.versionId = digitizer.getVersionId();
    this.name = digitizer.getName();
    this.manufacturer = digitizer.getManufacturer();
    this.model = digitizer.getModel();
    this.serialNumber = digitizer.getSerialNumber();
    this.actualTime = digitizer.getActualChangeTime();
    this.systemTime = digitizer.getSystemChangeTime();
    this.informationSource = new InformationSourceDao(digitizer.getInformationSource());
    this.comment = digitizer.getComment();
    this.description = digitizer.getDescription();
  }

    /**
   * Convert this DAO into its corresponding COI object.
   * @return A ReferenceDigitizer COI object.
   */
  public ReferenceDigitizer toCoi() {
    return ReferenceDigitizer.from(getEntityId(), getVersionId(),
        getName(), getManufacturer(), getModel(), getSerialNumber(),
        getActualTime(), getSystemTime(), getInformationSource().toCoi(),
        getComment(), getDescription());
  }

  public long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(long primaryKey) {
    this.primaryKey = primaryKey;
  }

  public UUID getEntityId() {
    return entityId;
  }

  public void setEntityId(UUID id) {
    this.entityId = id;
  }

  public UUID getVersionId() {
    return versionId;
  }

  public void setVersionId(UUID id) {
    this.versionId = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(
      String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public String getModel() {
    return model;
  }

  public void setModel(
      String model) {
    this.model = model;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  public Instant getActualTime() {
    return actualTime;
  }

  public void setActualTime(Instant actualTime) {
    this.actualTime = actualTime;
  }

  public Instant getSystemTime() {
    return systemTime;
  }

  public void setSystemTime(Instant systemTime) {
    this.systemTime = systemTime;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String desc) {
    this.description = desc;
  }

  public InformationSourceDao getInformationSource() {
    return informationSource;
  }

  public void setInformationSource(
      InformationSourceDao informationSource) {
    this.informationSource = informationSource;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceDigitizerDao that = (ReferenceDigitizerDao) o;

    if (primaryKey != that.primaryKey) {
      return false;
    }
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
    if (actualTime != null ? !actualTime.equals(that.actualTime) : that.actualTime != null) {
      return false;
    }
    if (systemTime != null ? !systemTime.equals(that.systemTime) : that.systemTime != null) {
      return false;
    }
    if (description != null ? !description.equals(that.description) : that.description != null) {
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
    result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
    result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (manufacturer != null ? manufacturer.hashCode() : 0);
    result = 31 * result + (model != null ? model.hashCode() : 0);
    result = 31 * result + (serialNumber != null ? serialNumber.hashCode() : 0);
    result = 31 * result + (actualTime != null ? actualTime.hashCode() : 0);
    result = 31 * result + (systemTime != null ? systemTime.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (informationSource != null ? informationSource.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceDigitizerDao{" +
        "primaryKey=" + primaryKey +
        ", entityId=" + entityId +
        ", versionId=" + versionId +
        ", name='" + name + '\'' +
        ", manufacturer='" + manufacturer + '\'' +
        ", model='" + model + '\'' +
        ", serialNumber='" + serialNumber + '\'' +
        ", actualTime=" + actualTime +
        ", systemTime=" + systemTime +
        ", description='" + description + '\'' +
        ", comment='" + comment + '\'' +
        ", informationSource=" + informationSource +
        '}';
  }
}
