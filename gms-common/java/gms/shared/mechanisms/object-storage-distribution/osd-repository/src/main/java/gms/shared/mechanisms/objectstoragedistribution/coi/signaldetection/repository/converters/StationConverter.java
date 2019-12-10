package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.converters;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import java.util.Objects;
import java.util.Set;

/**
 * Converts ReferenceStation to Station.
 */
public class StationConverter {

  /**
   * Converts a ReferenceStation to a Station, without any Sites populated.
   * @param s the reference station
   * @return a Station
   */
  public static Station withoutSites(ReferenceStation s) {
    Objects.requireNonNull(s);

    return Station.from(s.getVersionId(), s.getName(), s.getDescription(),  s.getStationType(),
            s.getLatitude(), s.getLongitude(), s.getElevation(), Set.of());
  }

  /**
   * Converts a ReferenceStation to a Station, with Site's populated.
   * @param s the reference station
   * @param sites the sites
   * @return a Station
   */
  public static Station withSites(ReferenceStation s, Set<Site> sites) {
    Objects.requireNonNull(s);

    return Station.from(s.getVersionId(), s.getName(), s.getDescription(), s.getStationType(),
            s.getLatitude(), s.getLongitude(), s.getElevation(), sites);
  }

}
