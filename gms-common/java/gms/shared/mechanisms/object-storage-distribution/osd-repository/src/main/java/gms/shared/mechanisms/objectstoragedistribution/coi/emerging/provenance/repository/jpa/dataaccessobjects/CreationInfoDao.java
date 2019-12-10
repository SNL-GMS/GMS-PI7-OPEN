package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import java.time.Instant;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Define a Data Access Object to allow access to the relational database.
 */
@Embeddable
public class CreationInfoDao {

  @Column(name = "creation_time", nullable = false)
  private Instant creationTime;

  @Column(name = "creator_name", nullable = false)
  private String creatorName;

  @Column(name = "software_component_name", nullable = false)
  private String softwareComponentName;

  @Column(name = "software_component_version", nullable = false)
  private String softwareComponentVersion;

  /**
   * Default constructor for use by JPA.
   */
  public CreationInfoDao() {
  }

  public CreationInfoDao(CreationInfo creationInfo) {
    Objects.requireNonNull(creationInfo);
    SoftwareComponentInfo softwareComponentInfo = creationInfo.getSoftwareInfo();
    Objects.requireNonNull(softwareComponentInfo);
    setCreationTime(creationInfo.getCreationTime());
    setCreatorName(creationInfo.getCreatorName());
    setSoftwareComponentName(softwareComponentInfo.getName());
    setSoftwareComponentVersion(softwareComponentInfo.getVersion());
  }

  /**
   * Convert this DAO into the associated COI object.
   *
   * @return An instance of the CreationInfo object.
   */
  public CreationInfo toCoi() {

    return new CreationInfo(this.creatorName, this.creationTime,
        new SoftwareComponentInfo(this.softwareComponentName, this.softwareComponentVersion));
  }

  public Instant getCreationTime() {
    return creationTime;
  }

  public String getCreatorName() {
    return creatorName;
  }

  public String getSoftwareComponentName() {
    return softwareComponentName;
  }

  public String getSoftwareComponentVersion() {
    return softwareComponentVersion;
  }

  public void setCreationTime(Instant creationTime) {
    this.creationTime = creationTime;
  }

  public void setCreatorName(String creatorName) {
    this.creatorName = creatorName;
  }

  public void setSoftwareComponentName(String softwareComponentName) {
    this.softwareComponentName = softwareComponentName;
  }

  public void setSoftwareComponentVersion(String softwareComponentVersion) {
    this.softwareComponentVersion = softwareComponentVersion;
  }

}
