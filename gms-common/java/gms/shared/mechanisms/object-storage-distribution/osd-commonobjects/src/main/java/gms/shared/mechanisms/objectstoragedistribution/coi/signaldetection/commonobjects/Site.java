package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.Validate;


/**
 * Represents a limited set of site information used during the acquisition of data streams.
 */
public final class Site {

  private final UUID id;
  private final String name;
  private final double latitude;
  private final double longitude;
  private final double elevation;
  private final Set<Channel> channels;

  /**
   * Create an instance of the class.
   *
   * @param name The name of the site, not null nor an empty string.
   * @param channels A collection of channels related to this site.
   * @return a ProcessingSite
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static Site create(String name, double latitude, double longitude, double elevation,
      Set<Channel> channels) {
    return new Site(UUID.randomUUID(), name, latitude, longitude, elevation, channels);
  }

  /**
   * Recreates a ProcessingSite given all params.
   *
   * @param id the id of the site
   * @param name The name of the site, not null nor an empty string.
   * @param channels A collection of channels related to this site.
   * @return a ProcessingSite
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static Site from(UUID id, String name, double latitude, double longitude,
      double elevation, Set<Channel> channels) {
    return new Site(id, name, latitude, longitude, elevation, channels);
  }

  /**
   * Private constructor.
   *
   * @param id the identifier to use
   * @param name The name of the site, not null nor an empty string.
   * @param channels A collection of channels related to this site.
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  private Site(UUID id, String name, double latitude, double longitude, double elevation,
      Set<Channel> channels) {

    Validate.notBlank(name);
    this.name = Objects.requireNonNull(name).trim();
    this.id = Objects.requireNonNull(id);
    this.latitude = latitude;
    this.longitude = longitude;
    this.elevation = elevation;
    this.channels = Collections.unmodifiableSet(channels);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
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

  public Set<Channel> getChannels() {
    return channels;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Site site = (Site) o;
    return Double.compare(site.latitude, latitude) == 0 &&
        Double.compare(site.longitude, longitude) == 0 &&
        Double.compare(site.elevation, elevation) == 0 &&
        Objects.equals(id, site.id) &&
        Objects.equals(name, site.name) &&
        Objects.equals(channels, site.channels);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, latitude, longitude, elevation, channels);
  }

  @Override
  public String toString() {
    return "Site{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", elevation=" + elevation +
        ", channels=" + channels +
        '}';
  }
}
