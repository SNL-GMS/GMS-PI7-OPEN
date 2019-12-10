package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import java.time.Instant;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * JPA data access object for
 * {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint}
 */
@Entity
@Table(name = "location_restraint")
public class LocationRestraintDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Enumerated(EnumType.STRING)
  @Column(name="latitude_restraint_type", nullable = false)
  private RestraintType latitudeRestraintType;

  @Column(name="latitude_restraint_degrees")
  private Double latitudeRestraintDegrees;

  @Enumerated(EnumType.STRING)
  @Column(name="longitude_restraint_type", nullable = false)
  private RestraintType longitudeRestraintType;

  @Column(name="longitude_restraint_degrees")
  private Double longitudeRestraintDegrees;

  @Enumerated(EnumType.STRING)
  @Column(name="depth_restraint_type", nullable = false)
  private DepthRestraintType depthRestraintType;

  @Column(name="depth_restraint_km")
  private Double depthRestraintKm;

  @Enumerated(EnumType.STRING)
  @Column(name="time_restraint_type", nullable = false)
  private RestraintType timeRestraintType;

  @Column(name="time_restraint")
  private Instant timeRestraint;

  /**
   * Default constructor for JPA.
   */
  public LocationRestraintDao() {
  }


  /**
   * Create a DAO from a COI object.
   * @param locationRestraint
   */
  public LocationRestraintDao(LocationRestraint locationRestraint) {
    Objects.requireNonNull(locationRestraint);
    this.latitudeRestraintType = locationRestraint.getLatitudeRestraintType();
    this.latitudeRestraintDegrees = locationRestraint.getLatitudeRestraintDegrees().isPresent() ? locationRestraint.getLatitudeRestraintDegrees().get() : null;
    this.longitudeRestraintType = locationRestraint.getLongitudeRestraintType();
    this.longitudeRestraintDegrees = locationRestraint.getLongitudeRestraintDegrees().isPresent() ? locationRestraint.getLongitudeRestraintDegrees().get() : null;
    this.depthRestraintType = locationRestraint.getDepthRestraintType();
    this.depthRestraintKm = locationRestraint.getDepthRestraintKm().isPresent() ? locationRestraint.getDepthRestraintKm().get() : null;
    this.timeRestraintType = locationRestraint.getTimeRestraintType();
    this.timeRestraint = locationRestraint.getTimeRestraint().isPresent() ? locationRestraint.getTimeRestraint().get() : null;
  }

  /**
   * Create a COI from this DAO.
   * @return LocationRestraint object.
   */
  public LocationRestraint toCoi() {
    return LocationRestraint.from(
        this.latitudeRestraintType,
        this.latitudeRestraintDegrees,
        this.longitudeRestraintType,
        this.longitudeRestraintDegrees,
        this.depthRestraintType,
        this.depthRestraintKm,
        this.timeRestraintType,
        this.timeRestraint);
  }

  public long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(long primaryKey) {
    this.primaryKey = primaryKey;
  }

  public RestraintType getLatitudeRestraintType() {
    return latitudeRestraintType;
  }

  public void setLatitudeRestraintType(
      RestraintType latitudeRestraintType) {
    this.latitudeRestraintType = latitudeRestraintType;
  }

  public Double getLatitudeRestraintDegrees() {
    return latitudeRestraintDegrees;
  }

  public void setLatitudeRestraintDegrees(Double latitudeRestraintDegrees) {
    this.latitudeRestraintDegrees = latitudeRestraintDegrees;
  }

  public RestraintType getLongitudeRestraintType() {
    return longitudeRestraintType;
  }

  public void setLongitudeRestraintType(
      RestraintType longitudeRestraintType) {
    this.longitudeRestraintType = longitudeRestraintType;
  }

  public Double getLongitudeRestraintDegrees() {
    return longitudeRestraintDegrees;
  }

  public void setLongitudeRestraintDegrees(Double longitudeRestraintDegrees) {
    this.longitudeRestraintDegrees = longitudeRestraintDegrees;
  }

  public DepthRestraintType getDepthRestraintType() {
    return depthRestraintType;
  }

  public void setDepthRestraintType(
      DepthRestraintType depthRestraintType) {
    this.depthRestraintType = depthRestraintType;
  }

  public Double getDepthRestraintKm() {
    return depthRestraintKm;
  }

  public void setDepthRestraintKm(Double depthRestraintKm) {
    this.depthRestraintKm = depthRestraintKm;
  }

  public RestraintType getTimeRestraintType() {
    return timeRestraintType;
  }

  public void setTimeRestraintType(
      RestraintType timeRestraintType) {
    this.timeRestraintType = timeRestraintType;
  }

  public Instant getTimeRestraint() {
    return timeRestraint;
  }

  public void setTimeRestraint(Instant timeRestraint) {
    this.timeRestraint = timeRestraint;
  }

  @Override
  public String toString() {
    return "LocationRestraintDao{" +
        "primaryKey=" + primaryKey +
        ", latitudeRestraintType=" + latitudeRestraintType +
        ", latitudeRestraintDegrees=" + latitudeRestraintDegrees +
        ", longitudeRestraintType=" + longitudeRestraintType +
        ", longitudeRestraintDegrees=" + longitudeRestraintDegrees +
        ", depthRestraintType=" + depthRestraintType +
        ", depthRestraintKm=" + depthRestraintKm +
        ", timeRestraintType=" + timeRestraintType +
        ", timeRestraint=" + timeRestraint +
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
    LocationRestraintDao that = (LocationRestraintDao) o;
    return primaryKey == that.primaryKey &&
        latitudeRestraintType == that.latitudeRestraintType &&
        Objects.equals(latitudeRestraintDegrees, that.latitudeRestraintDegrees) &&
        longitudeRestraintType == that.longitudeRestraintType &&
        Objects.equals(longitudeRestraintDegrees, that.longitudeRestraintDegrees) &&
        depthRestraintType == that.depthRestraintType &&
        Objects.equals(depthRestraintKm, that.depthRestraintKm) &&
        timeRestraintType == that.timeRestraintType &&
        Objects.equals(timeRestraint, that.timeRestraint);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(primaryKey, latitudeRestraintType, latitudeRestraintDegrees, longitudeRestraintType,
            longitudeRestraintDegrees, depthRestraintType, depthRestraintKm, timeRestraintType,
            timeRestraint);
  }
}
