package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkOrganization;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkRegion;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

/**
 * Represents a processing network.  This is a subset of the ReferenceNetwork class.
 */
public final class Network {

  private final UUID id;
  private final String name;
  private final NetworkOrganization organization;
  private final NetworkRegion region;
  private final Set<Station> stations;

  /**
   * Create a new Network object.
   * @param name The name of the network.
   * @param organization The organization responsible for the network.
   * @param region The network region.
   * @param stations A list of Station UUIDs associated with the network.
   * @return A Network object.
   */
  public static Network create(String name, NetworkOrganization organization,
      NetworkRegion region, Set<Station> stations) {
    return new Network(UUID.randomUUID(), name, organization, region, stations);
  }

  /**
   * Create a Network object from existing data.
   * @param id The UUID assigned to the object.
   * @param name The name of the network.
   * @param organization The organization responsible for the network.
   * @param region The network region.
   * @param stations A list of Station UUIDs associated with the network.
   * @return A Network object.
   */
  public static Network from(UUID id, String name, NetworkOrganization organization,
      NetworkRegion region, Set<Station> stations) {
    return new Network(id, name, organization, region, stations);
  }

  /**
   * Private Constructor.
   * @param id The UUID assigned to the object.
   * @param name The name of the network.
   * @param organization The organization responsible for the network.
   * @param region The network region.
   * @param stations A list of Station UUIDs associated with the network.
   */
  private Network(UUID id, String name, NetworkOrganization organization,
      NetworkRegion region, Set<Station> stations) {
    Validate.notBlank(name);
    this.id = Objects.requireNonNull(id);
    this.name = Objects.requireNonNull(name).trim();
    this.organization = Objects.requireNonNull(organization);
    this.region = Objects.requireNonNull(region);
    this.stations = Collections.unmodifiableSet(stations);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public NetworkOrganization getOrganization() {
    return organization;
  }

  public NetworkRegion getRegion() {
    return region;
  }

  public Set<Station> getStations() {
    return stations;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Network network = (Network) o;

    if (id != null ? !id.equals(network.id) : network.id != null) {
      return false;
    }
    if (name != null ? !name.equals(network.name) : network.name != null) {
      return false;
    }
    if (organization != network.organization) {
      return false;
    }
    if (region != network.region) {
      return false;
    }
    return stations != null ? stations.equals(network.stations) : network.stations == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (organization != null ? organization.hashCode() : 0);
    result = 31 * result + (region != null ? region.hashCode() : 0);
    result = 31 * result + (stations != null ? stations.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Network{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", organization=" + organization +
        ", region=" + region +
        ", stations=" + stations +
        '}';
  }
}
