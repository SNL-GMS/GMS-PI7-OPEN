package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;


import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 * JPA data access object for {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution}
 */
@Entity
@Table(name = "location_solution")
public class LocationSolutionDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(unique = true, name = "entity_id")
  private UUID entityId;

  @Embedded
  private EventLocationDao location;

  @OneToOne(cascade = CascadeType.ALL)
  private LocationRestraintDao locationRestraint;

  @OneToOne(cascade = CascadeType.ALL)
  private LocationUncertaintyDao locationUncertainty;

  @LazyCollection(LazyCollectionOption.FALSE)
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<LocationBehaviorDao> locationBehaviors;

  @LazyCollection(LazyCollectionOption.FALSE)
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<FeaturePredictionDao<?>> featurePredictions;

  /**
   * Default constructor for JPA.
   */
  public LocationSolutionDao() {
  }

  /**
   * Create a DAO from the COI object.
   */
  public LocationSolutionDao(LocationSolution locationSolution) {
    Objects.requireNonNull(locationSolution);
    this.entityId = locationSolution.getId();
    this.location = new EventLocationDao(locationSolution.getLocation());
    this.locationRestraint = new LocationRestraintDao(locationSolution.getLocationRestraint());
    this.locationUncertainty = locationSolution.getLocationUncertainty().isPresent()
        ? new LocationUncertaintyDao(locationSolution.getLocationUncertainty().get()) : null;
    this.locationBehaviors = locationSolution.getLocationBehaviors().stream()
        .map(LocationBehaviorDao::new).collect(Collectors.toSet());
    this.featurePredictions = locationSolution.getFeaturePredictions().stream()
        .map(FeaturePredictionDao::from).collect(Collectors.toSet());
  }


  /**
   * Create a COI from this DAO.
   *
   * @return LocationSolution object.
   */
  public LocationSolution toCoi() {
    return LocationSolution.from(
        this.entityId,
        this.location.toCoi(),
        this.locationRestraint.toCoi(),
        this.locationUncertainty == null ? null : this.locationUncertainty.toCoi(),
        this.locationBehaviors.stream()
            .map(LocationBehaviorDao::toCoi)
            .collect(Collectors.toSet()),
        this.featurePredictions.stream()
            .map(FeaturePredictionDao::toCoi)
            .collect(Collectors.toSet()));
  }

  public UUID getEntityId() {
    return entityId;
  }

  public void setEntityId(UUID entityId) {
    this.entityId = entityId;
  }

  public EventLocationDao getLocation() {
    return location;
  }

  public void setLocation(
      EventLocationDao location) {
    this.location = location;
  }

  public LocationRestraintDao getLocationRestraint() {
    return locationRestraint;
  }

  public void setLocationRestraint(
      LocationRestraintDao locationRestraint) {
    this.locationRestraint = locationRestraint;
  }

  public LocationUncertaintyDao getLocationUncertainty() {
    return locationUncertainty;
  }

  public void setLocationUncertainty(
      LocationUncertaintyDao locationUncertainty) {
    this.locationUncertainty = locationUncertainty;
  }

  public Set<LocationBehaviorDao> getLocationBehaviors() {
    return locationBehaviors;
  }

  public void setLocationBehaviors(
      Set<LocationBehaviorDao> locationBehaviors) {
    this.locationBehaviors = locationBehaviors;
  }

  public Set<FeaturePredictionDao<?>> getFeaturePredictions() {
    return featurePredictions;
  }

  public void setFeaturePredictions(
      Set<FeaturePredictionDao<?>> featurePredictions) {
    this.featurePredictions = featurePredictions;
  }

  @Override
  public String toString() {
    return "LocationSolutionDao{" +
        "entityId=" + entityId +
        ", location=" + location +
        ", locationRestraint=" + locationRestraint +
        ", locationUncertainty=" + locationUncertainty +
        ", locationBehaviors=" + locationBehaviors +
        ", featurePredictions=" + featurePredictions +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LocationSolutionDao that = (LocationSolutionDao) o;

    if (entityId != null ? !entityId.equals(that.entityId) : that.entityId != null) {
      return false;
    }
    if (location != null ? !location.equals(that.location) : that.location != null) {
      return false;
    }
    if (locationRestraint != null ? !locationRestraint.equals(that.locationRestraint)
        : that.locationRestraint != null) {
      return false;
    }
    if (locationUncertainty != null ? !locationUncertainty.equals(that.locationUncertainty)
        : that.locationUncertainty != null) {
      return false;
    }
    if (locationBehaviors != null ? !locationBehaviors.equals(that.locationBehaviors)
        : that.locationBehaviors != null) {
      return false;
    }
    return featurePredictions != null ? featurePredictions.equals(that.featurePredictions)
        : that.featurePredictions == null;

  }

  @Override
  public int hashCode() {
    int result = entityId != null ? entityId.hashCode() : 0;
    result = 31 * result + (location != null ? location.hashCode() : 0);
    result = 31 * result + (locationRestraint != null ? locationRestraint.hashCode() : 0);
    result = 31 * result + (locationUncertainty != null ? locationUncertainty.hashCode() : 0);
    result = 31 * result + (locationBehaviors != null ? locationBehaviors.hashCode() : 0);
    result = 31 * result + (featurePredictions != null ? featurePredictions.hashCode() : 0);
    return result;
  }
}
