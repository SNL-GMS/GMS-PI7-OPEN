package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.converters;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Network;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import java.util.Objects;
import java.util.Set;

/**
 * Converts ReferenceNetwork to Network.
 */
public class NetworkConverter {

  /**
   * Converts a ReferenceNetwork to a Network without any stations populated.
   * @param n the reference network
   * @return a Network
   */
  public static Network withoutStations(ReferenceNetwork n) {
    Objects.requireNonNull(n);

    return Network.from(n.getVersionId(), n.getName(), n.getOrganization(),
        n.getRegion(), Set.of());
  }

  /**
   * Converts a ReferenceNetwork to a Network with any stations populated.
   * @param n the reference network
   * @param stations the stations
   * @return a Network
   */
  public static Network withStations(ReferenceNetwork n, Set<Station> stations) {
    Objects.requireNonNull(n);

    return Network.from(n.getVersionId(), n.getName(), n.getOrganization(),
        n.getRegion(), stations);
  }

}
