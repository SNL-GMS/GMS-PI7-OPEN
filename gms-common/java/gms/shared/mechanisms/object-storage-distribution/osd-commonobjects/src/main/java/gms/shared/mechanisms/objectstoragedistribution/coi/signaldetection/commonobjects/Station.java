package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

/**
 * Represents a limited set of station information used during the acquisition and
 * processing of data streams.
 *
 *
 */
public final class Station {

  private final UUID id;
  private final String name;
  private final String description;
  private final StationType stationType;
  private final double latitude;
  private final double longitude;
  private final double elevation;
  private final Set<Site> sites;

  /**
   * Create a new Station.
   * @param name The name of the station, not null nor an empty string.
   * @param description The description of the station.
   * @param stationType The station type.
   * @param latitude The station's latitude.
   * @param longitude The station's longitude.
   * @param elevation The station's elevation.
   * @param sites A collection of associated processing site UUIDs.
   * @throws IllegalArgumentException
   * @throws NullPointerException
   */
  public static Station create(String name, String description, StationType stationType, double latitude, double longitude,
                               double elevation, Set<Site> sites) {
    return new Station(UUID.randomUUID(), name, description, stationType, latitude, longitude, elevation, sites);
  }

  /**
   * Create a Station from existing data.
   * @param id The UUID assigned to the object.
   * @param name The name of the station, not null nor an empty string.
   * @param description The description of the station.
   * @param stationType The station type.
   * @param latitude The station's latitude.
   * @param longitude The station's longitude.
   * @param elevation The station's elevation.
   * @param sites A collection of associated processing site UUIDs.
   * @throws IllegalArgumentException
   * @throws NullPointerException
   */
  public static Station from(UUID id, String name, String description, StationType stationType, double latitude,
                             double longitude, double elevation, Set<Site> sites) {
    return new Station(id, name, description, stationType, latitude, longitude, elevation, sites);

  }

  /**
   * Private constructor.
   * @param id The UUID assigned to the object.
   * @param name The name of the station, not null nor an empty string.
   * @param description The description of the station.
   * @param stationType The station type.
   * @param latitude The station's latitude.
   * @param longitude The station's longitude.
   * @param elevation The station's elevation.
   * @param sites A collection of associated processing site UUIDs.
   * @throws IllegalArgumentException
   * @throws NullPointerException
   */
  private Station(UUID id, String name, String description, StationType stationType, double latitude, double longitude,
                  double elevation, Set<Site> sites)
          throws IllegalArgumentException {

    Validate.notBlank(name);
    this.id = Objects.requireNonNull(id);
    this.name = Objects.requireNonNull(name).trim();
    this.description = Objects.requireNonNull(description);
    this.stationType = Objects.requireNonNull(stationType);
    this.latitude = latitude;
    this.longitude = longitude;
    this.elevation = elevation;
    this.sites = Collections.unmodifiableSet(sites);
  }

  public UUID getId() { return id;  }

  public String getName() {
    return name;
  }

  public String getDescription() { return description; }

  public StationType getStationType() { return stationType; }

  public Set<Site> getSites() {
    return this.sites;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public double getElevation() {
    return elevation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Station station = (Station) o;
    return Double.compare(station.getLatitude(), getLatitude()) == 0 &&
            Double.compare(station.getLongitude(), getLongitude()) == 0 &&
            Double.compare(station.getElevation(), getElevation()) == 0 &&
            Objects.equals(getId(), station.getId()) &&
            Objects.equals(getName(), station.getName()) &&
            Objects.equals(getDescription(), station.getDescription()) &&
            getStationType() == station.getStationType() &&
            Objects.equals(getSites(), station.getSites());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getName(), getDescription(), getStationType(), getLatitude(), getLongitude(), getElevation(), getSites());
  }

  @Override
  public String toString() {
    return "Station{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", stationType=" + stationType +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            ", elevation=" + elevation +
            ", sites=" + sites +
            '}';
  }
}
