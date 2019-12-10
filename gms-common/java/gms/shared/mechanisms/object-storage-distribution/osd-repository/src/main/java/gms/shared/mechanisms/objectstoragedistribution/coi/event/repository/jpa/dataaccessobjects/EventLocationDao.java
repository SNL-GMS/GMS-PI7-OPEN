package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import java.time.Instant;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * JPA data access object for {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation}
 */
@Embeddable
public class EventLocationDao {

  @Column(name="event_latitude_degrees", nullable = false)
  private double latitudeDegrees;

  @Column(name="event_longitude_degrees", nullable = false)
  private double longitudeDegrees;

  @Column(name="event_depth_km", nullable = false)
  private double depthKm;

  @Column(name="event_time", nullable = false)
  private Instant time;

  /**
   * Default constructor for JPA.
   */
  public EventLocationDao() {
  }

  /**
   * Create a DAO from the COI object.
   * @param location
   */
  public EventLocationDao(EventLocation location) {
    Objects.requireNonNull(location);
    this.latitudeDegrees = location.getLatitudeDegrees();
    this.longitudeDegrees = location.getLongitudeDegrees();
    this.depthKm = location.getDepthKm();
    this.time = location.getTime();
  }

  /**
   * Create a COI object from this DAO.
   * @return A Location object.
   */
  public EventLocation toCoi() {
    return EventLocation.from(this.latitudeDegrees, this.longitudeDegrees, this.depthKm,
        this.time);
  }

  public double getLatitudeDegrees() {
    return latitudeDegrees;
  }

  public void setLatitudeDegrees(double latitudeDegrees) {
    this.latitudeDegrees = latitudeDegrees;
  }

  public double getLongitudeDegrees() {
    return longitudeDegrees;
  }

  public void setLongitudeDegrees(double longitudeDegrees) {
    this.longitudeDegrees = longitudeDegrees;
  }

  public double getDepthKm() {
    return depthKm;
  }

  public void setDepthKm(double depthKm) {
    this.depthKm = depthKm;
  }

  public Instant getTime() {
    return time;
  }

  public void setTime(Instant time) {
    this.time = time;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    EventLocationDao that = (EventLocationDao) o;

    if (Double.compare(that.latitudeDegrees, latitudeDegrees) != 0) {
      return false;
    }
    if (Double.compare(that.longitudeDegrees, longitudeDegrees) != 0) {
      return false;
    }
    if (Double.compare(that.depthKm, depthKm) != 0) {
      return false;
    }
    return time != null ? time.equals(that.time) : that.time == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(latitudeDegrees);
    result = (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(longitudeDegrees);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(depthKm);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (time != null ? time.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "EventLocationDao{" +
        "latitudeDegrees=" + latitudeDegrees +
        ", longitudeDegrees=" + longitudeDegrees +
        ", depthKm=" + depthKm +
        ", time=" + time +
        '}';
  }
}