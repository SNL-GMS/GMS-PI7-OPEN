package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects;

import java.time.Instant;
import java.util.Objects;

/**
 * Information Source captures reference details for information added to the System.
 * E.g. if the System records a seismometer's location based on a value published in a
 * network guidebook then an Information Source object would describe the originating
 * organization that published the book, the date of publication is the information time,
 * and additional details on how others can access the guidebook appear in reference.
 */
public final class InformationSource {

  private final String originatingOrganization;
  private final Instant informationTime;
  private final String reference;


  /**
   * Create a new InformationSource object.
   * @param originatingOrganization The organization which created this information.
   * @param informationTime The date-time the information was generated.
   * @param reference A reference to the information.
   * @return A InformationSource object.
   */
  public static InformationSource create(String originatingOrganization,
      Instant informationTime, String reference) {

    return InformationSource.from(originatingOrganization,
        informationTime, reference);
  }

  /**
   * Create an InformationSource from existing information.
   * @param originatingOrganization The organization which created this information.
   * @param informationTime The date-time the information was generated.
   * @param reference A reference to the information.
   * @return A InformationSource object.
   */
  public static InformationSource from(String originatingOrganization,
      Instant informationTime, String reference) {

    Objects.requireNonNull(originatingOrganization,
        "Cannot create InformationSource from null originatingOrganization");
    Objects.requireNonNull(informationTime,
        "Cannot create InformationSource from null informationTime");
    Objects.requireNonNull(reference,
        "Cannot create InformationSource from null reference");

    return new InformationSource(originatingOrganization, informationTime, reference);
  }

  // Private constructor.
  private InformationSource(String originatingOrganization,
      Instant informationTime, String reference) {
    this.originatingOrganization = originatingOrganization;
    this.informationTime = informationTime;
    this.reference = reference;
  }

  public String getOriginatingOrganization() { return originatingOrganization; }

  public Instant getInformationTime() { return informationTime; }

  public String getReference() { return reference; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    InformationSource that = (InformationSource) o;


    if (getOriginatingOrganization() != null ? !getOriginatingOrganization()
        .equals(that.getOriginatingOrganization()) : that.getOriginatingOrganization() != null) {
      return false;
    }
    if (getInformationTime() != null ? !getInformationTime().equals(that.getInformationTime())
        : that.getInformationTime() != null) {
      return false;
    }
    return getReference() != null ? getReference().equals(that.getReference())
        : that.getReference() == null;
  }

  @Override
  public int hashCode() {
    int result = 31;
    result =
        31 * result + (getOriginatingOrganization() != null ? getOriginatingOrganization()
            .hashCode()
            : 0);
    result = 31 * result + (getInformationTime() != null ? getInformationTime().hashCode() : 0);
    result = 31 * result + (getReference() != null ? getReference().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "InformationSource{" +
        "organization='" + originatingOrganization + '\'' +
        ", informationTime=" + informationTime +
        ", reference='" + reference + '\'' +
        '}';
  }
}
