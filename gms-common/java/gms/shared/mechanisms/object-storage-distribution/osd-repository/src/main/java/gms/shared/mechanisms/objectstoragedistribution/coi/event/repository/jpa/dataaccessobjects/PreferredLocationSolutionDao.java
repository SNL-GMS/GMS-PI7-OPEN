package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * JPA data access object for {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution}
 */
@Entity
@Table(name = "preferred_location_solution")
public class PreferredLocationSolutionDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @OneToOne(cascade = CascadeType.ALL)
  private LocationSolutionDao locationSolution;


  /**
   * Default constructor for JPA.
   */
  public PreferredLocationSolutionDao() {
  }

  /**
   * Create a DAO from the COI object.
   * @param preferredLocationSolution
   */
  public PreferredLocationSolutionDao(PreferredLocationSolution preferredLocationSolution) {
    Objects.requireNonNull(preferredLocationSolution);
    this.locationSolution = new LocationSolutionDao(
        preferredLocationSolution.getLocationSolution());
  }

  /**
   * Create a COI object from this DAO.
   * @return A Location object.
   */
  public PreferredLocationSolution toCoi() {
    return PreferredLocationSolution.from(this.locationSolution.toCoi());
  }

  public long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(long primaryKey) {
    this.primaryKey = primaryKey;
  }

  public LocationSolutionDao getLocationSolution() {
    return locationSolution;
  }

  public void setLocationSolution(
      LocationSolutionDao locationSolution) {
    this.locationSolution = locationSolution;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PreferredLocationSolutionDao that = (PreferredLocationSolutionDao) o;
    return getPrimaryKey() == that.getPrimaryKey() &&
        Objects.equals(getLocationSolution(), that.getLocationSolution());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getPrimaryKey(), getLocationSolution());
  }

  @Override
  public String toString() {
    return "PreferredLocationSolutionDao{" +
        "primaryKey=" + primaryKey +
        ", locationSolution=" + locationSolution +
        '}';
  }
}