package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationBehavior;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * JPA data access object for
 * {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationBehavior}
 */
@Entity
@Table(name = "location_behavior")
public class LocationBehaviorDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(name = "residual", nullable = false)
  private double residual;

  @Column(name = "weight", nullable = false)
  private double weight;

  @Column(name = "is_defining", nullable = false)
  private boolean isDefining;

  private UUID featurePredictionId;

  private UUID featureMeasurementId;

  /**
   * Default constructor for JPA.
   */
  public LocationBehaviorDao() {}

  /**
   * Create a DAO from the COI.
   * @param locationBehavior The COI object.
   */
  public LocationBehaviorDao(LocationBehavior locationBehavior) {
    Objects.requireNonNull(locationBehavior);
    this.residual = locationBehavior.getResidual();
    this.weight = locationBehavior.getWeight();
    this.isDefining = locationBehavior.isDefining();
    this.featurePredictionId = locationBehavior.getFeaturePredictionId();
    this.featureMeasurementId = locationBehavior.getFeatureMeasurementId();
  }


  /**
   * Create a COI from this DAO.
   * @return A LocationBehavior object.
   */
  public LocationBehavior toCoi() {
    return LocationBehavior.from(this.residual, this.weight, this.isDefining,
        this.featurePredictionId, this.featureMeasurementId);
  }

  public double getResidual() {
    return residual;
  }

  public void setResidual(double residual) {
    this.residual = residual;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }

  public boolean isDefining() {
    return isDefining;
  }

  public void setDefining(boolean defining) {
    isDefining = defining;
  }

  public UUID getFeaturePredictionId() {
    return featurePredictionId;
  }

  public void setFeaturePredictionId(
      UUID featurePredictionId) {
    this.featurePredictionId = featurePredictionId;
  }

  public UUID getFeatureMeasurementId() {
    return featureMeasurementId;
  }

  public void setFeatureMeasurementId(
      UUID featureMeasurementId) {
    this.featureMeasurementId = featureMeasurementId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocationBehaviorDao that = (LocationBehaviorDao) o;
    return primaryKey == that.primaryKey &&
        Double.compare(that.residual, residual) == 0 &&
        Double.compare(that.weight, weight) == 0 &&
        isDefining == that.isDefining &&
        Objects.equals(featurePredictionId, that.featurePredictionId) &&
        Objects.equals(featureMeasurementId, that.featureMeasurementId);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(primaryKey, residual, weight, isDefining, featurePredictionId, featureMeasurementId);
  }

  @Override
  public String toString() {
    return "LocationBehaviorDao{" +
        "primaryKey=" + primaryKey +
        ", residual=" + residual +
        ", weight=" + weight +
        ", isDefining=" + isDefining +
        ", featurePredictionId=" + featurePredictionId +
        ", featureMeasurementId=" + featureMeasurementId +
        '}';
  }
}
