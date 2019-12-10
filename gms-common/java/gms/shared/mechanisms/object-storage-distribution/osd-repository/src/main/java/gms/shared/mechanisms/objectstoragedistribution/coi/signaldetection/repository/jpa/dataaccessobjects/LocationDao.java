package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import com.google.common.base.Preconditions;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * JPA data access object for {@link Location}
 * @see Location
 */
@Embeddable
public class LocationDao {

  @Column(name = "latitude_degrees")
  private double latitudeDegrees;

  @Column(name = "longitude_degrees")
  private double longitudeDegrees;

  @Column(name = "depth_km")
  private double depthKm;

  @Column(name = "elevation_km")
  private double elevationKm;

  protected LocationDao() {
  }

  private LocationDao(double latitudeDegrees, double longitudeDegrees, double depthKm,
      double elevationKm) {
    this.latitudeDegrees = latitudeDegrees;
    this.longitudeDegrees = longitudeDegrees;
    this.depthKm = depthKm;
    this.elevationKm = elevationKm;
  }

  /**
   * Create a DAO from the COI {@link Location}.
   * @param location the location to convert
   * @return The Location converted to its DAO format
   */
  public static LocationDao from(Location location) {
    Preconditions.checkNotNull(location, "Cannot create dao from null Location");
    return new LocationDao(location.getLatitudeDegrees(), location.getLongitudeDegrees(),
        location.getDepthKm(), location.getElevationKm());
  }

  /**
   * Create a COI from this DAO.
   * @return A Location object.
   */
  public Location toCoi() {
    return Location
        .from(this.latitudeDegrees, this.longitudeDegrees, this.depthKm, this.elevationKm);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocationDao that = (LocationDao) o;
    return Double.compare(that.latitudeDegrees, latitudeDegrees) == 0 &&
        Double.compare(that.longitudeDegrees, longitudeDegrees) == 0 &&
        Double.compare(that.depthKm, depthKm) == 0 &&
        Double.compare(that.elevationKm, elevationKm) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(latitudeDegrees, longitudeDegrees, depthKm, elevationKm);
  }

  @Override
  public String toString() {
    return "LocationDao{" +
        "latitudeDegrees=" + latitudeDegrees +
        ", longitudeDegrees=" + longitudeDegrees +
        ", depthKm=" + depthKm +
        ", elevationKm=" + elevationKm +
        '}';
  }
}
