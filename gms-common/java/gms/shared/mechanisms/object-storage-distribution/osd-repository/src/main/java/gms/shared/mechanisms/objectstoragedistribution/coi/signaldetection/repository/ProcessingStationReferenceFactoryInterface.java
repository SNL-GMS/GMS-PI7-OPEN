package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Network;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import java.time.Instant;
import java.util.Optional;

/**
 * Interface for storing and retrieving (mission domain) 'processing' versions of station reference
 * information.
 */
public interface ProcessingStationReferenceFactoryInterface {

  /**
   * Defaults the default value for the 'slim' parameter to false. Defining here makes it easier to
   * change the default for this parameter.
   */
  boolean DEFAULT_SLIM = false;

  /**
   * Retrieve a Network object by name and time-frame.
   *
   * @param name Name to search for.
   * @param actualChangeTime the actual change time to use for comparison
   * @param systemChangeTime the system change time to use for comparison
   * @param slim if true, children objects (Station's and below) will not be populated. If false,
   * they will be populated (so called 'fat' query).
   * @return A Network, or empty if none or multiple are found by this name.
   */
  Optional<Network> networkFromName(String name, Instant actualChangeTime,
      Instant systemChangeTime, boolean slim);

  /**
   * Retrieve a 'fat' (populated) Network object by name and time-frame.
   *
   * @param name Name to search for.
   * @param actualChangeTime the actual change time to use for comparison
   * @param systemChangeTime the system change time to use for comparison
   * @return A Network, or empty if none or multiple are found by this name.
   */
  default Optional<Network> networkFromName(String name,
      Instant actualChangeTime, Instant systemChangeTime) {
    return networkFromName(name, actualChangeTime, systemChangeTime, DEFAULT_SLIM);
  }

  /**
   * Retrieve a Network object by name and time-frame.
   *
   * @param name Name to search for.
   * @param slim if true, children objects (Station's and below) will not be populated.
   * @return A Network, or empty if none or multiple are found by this name.
   */
  default Optional<Network> networkFromName(String name, boolean slim) {
    return networkFromName(name, Instant.now(), Instant.now(), slim);
  }

  /**
   * Retrieve a 'fat' (populated) Network object by name for the current time.
   *
   * @param name Name to search for.
   * @return A Network, or empty if none or multiple are found by this name.
   */
  default Optional<Network> networkFromName(String name) {
    return networkFromName(name, DEFAULT_SLIM);
  }

  /**
   * Retrieve a Station object by name and time-frame.
   *
   * @param name The name to search for.
   * @param actualChangeTime the actual change time to use for comparison
   * @param systemChangeTime the system change time to use for comparison
   * @param slim if true, children objects (Site's and below) will not be populated. If false, they
   * will be populated (so called 'fat' query).
   * @return The Station object or empty if not found.
   */
  Optional<Station> stationFromName(String name, Instant actualChangeTime,
      Instant systemChangeTime, boolean slim);

  /**
   * Retrieve a 'fat' (populated) Station object by name and time-frame.
   *
   * @param name The name to search for.
   * @param actualChangeTime the actual change time to use for comparison
   * @param systemChangeTime the system change time to use for comparison
   * @return The Station object or empty if not found.
   */
  default Optional<Station> stationFromName(String name, Instant actualChangeTime,
      Instant systemChangeTime) {
    return stationFromName(name, actualChangeTime, systemChangeTime, DEFAULT_SLIM);
  }

  /**
   * Retrieve a Station object by name for the current time.
   *
   * @param name The name to search for.
   * @param slim if true, children objects (Site's and below) will not be populated. If false, they
   * will be populated (so called 'fat' query).
   * @return The Station object or empty if not found.
   */
  default Optional<Station> stationFromName(String name, boolean slim) {
    return stationFromName(name, Instant.now(), Instant.now(), slim);
  }

  /**
   * Retrieve a 'fat' (populated) Station object by name for the current time.
   *
   * @param name The name to search for.
   * @return The Station object or empty if not found.
   */
  default Optional<Station> stationFromName(String name) {
    return stationFromName(name, DEFAULT_SLIM);
  }

  /**
   * Retrieve a Site object by name and time-frame.
   *
   * @param name The name or alias of the site.
   * @param actualChangeTime the actual change time to use for comparison
   * @param systemChangeTime the system change time to use for comparison
   * @param slim if true, children objects (Channel's and below) will not be populated. If false,
   * they will be populated (so called 'fat' query).
   * @return A Site or empty if not found.
   */
  Optional<Site> siteFromName(String name, Instant actualChangeTime, Instant systemChangeTime,
      boolean slim);

  /**
   * Retrieve a 'fat' (populated) Site object by name and time-frame.
   *
   * @param name The name or alias of the site.
   * @param actualChangeTime the actual change time to use for comparison
   * @param systemChangeTime the system change time to use for comparison
   * @return A Site or empty if not found.
   */
  default Optional<Site> siteFromName(String name, Instant actualChangeTime,
      Instant systemChangeTime) {
    return siteFromName(name, actualChangeTime, systemChangeTime, DEFAULT_SLIM);
  }

  /**
   * Retrieve a Site object by name for the current time.
   *
   * @param name The name or alias of the site.
   * @param slim if true, children objects (Channel's and below) will not be populated. If false,
   * they will be populated (so called 'fat' query).
   * @return A Site or empty if not found.
   */
  default Optional<Site> siteFromName(String name, boolean slim) {
    return siteFromName(name, Instant.now(), Instant.now(), slim);
  }

  /**
   * Retrieve a 'fat' (populated) Site object by name for the current time.
   *
   * @param name The name or alias of the site.
   * @return A Site or empty if not found.
   */
  default Optional<Site> siteFromName(String name) {
    return siteFromName(name, DEFAULT_SLIM);
  }
}
