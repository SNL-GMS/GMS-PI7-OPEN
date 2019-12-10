package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import java.time.Instant;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class InformationSourceDao {

  @Column(name = "originatingOrganization", nullable = false)
  private String originatingOrganization;

  @Column(name = "informationTime", nullable = false)
  private Instant informationTime;

  @Column(name = "reference", nullable = false)
  private String reference;

  /**
   * Default constructor for use by JPA.
   */
  public InformationSourceDao() {
  }

  public InformationSourceDao(InformationSource informationSource) {
    Objects.requireNonNull(informationSource);
    setOriginatingOrganization(informationSource.getOriginatingOrganization());
    setInformationTime(informationSource.getInformationTime());
    setReference(informationSource.getReference());
  }

  /**
   * Convert this DAO into the associated COI object.
   *
   * @return An instance of the InformationSource object.
   */
  public InformationSource toCoi() {

    return InformationSource.from(getOriginatingOrganization(),
        getInformationTime(), getReference());
  }


  public String getOriginatingOrganization() {
    return originatingOrganization;
  }

  public void setOriginatingOrganization(String originatingOrganization) {
    this.originatingOrganization = originatingOrganization;
  }

  public Instant getInformationTime() {
    return informationTime;
  }

  public void setInformationTime(Instant informationTime) {
    this.informationTime = informationTime;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    InformationSourceDao that = (InformationSourceDao) o;

    if (originatingOrganization != null ? !originatingOrganization
        .equals(that.originatingOrganization) : that.originatingOrganization != null) {
      return false;
    }
    if (informationTime != null ? !informationTime.equals(that.informationTime)
        : that.informationTime != null) {
      return false;
    }
    return reference != null ? reference.equals(that.reference) : that.reference == null;
  }

  @Override
  public int hashCode() {

    int result = originatingOrganization != null ? originatingOrganization.hashCode() : 0;
    result = 31 * result + (informationTime != null ? informationTime.hashCode() : 0);
    result = 31 * result + (reference != null ? reference.hashCode() : 0);
    return result;
  }
}
