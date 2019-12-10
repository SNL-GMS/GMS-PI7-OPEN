package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.converters;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSite;
import java.util.Objects;
import java.util.Set;

/**
 * Converts ReferenceSite to Site.
 */
public class SiteConverter {

  /**
   * Converts a ReferenceSite to a Site, without any Channel's populated.
   * @param s the reference site
   * @return a Site
   */
  public static Site withoutChannels(ReferenceSite s) {
    Objects.requireNonNull(s);

    return Site.from(s.getVersionId(), s.getName(), s.getLatitude(),
        s.getLongitude(), s.getElevation(), Set.of());
  }

  /**
   * Converts a ReferenceSite to a Site, with Channel's populated.
   * @param s the reference site
   * @param channels the channels
   * @return a Site
   */
  public static Site withChannels(ReferenceSite s, Set<Channel> channels) {
    Objects.requireNonNull(s);

    return Site.from(s.getVersionId(), s.getName(), s.getLatitude(),
        s.getLongitude(), s.getElevation(), channels);
  }

}
