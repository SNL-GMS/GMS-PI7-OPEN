package gms.core.signaldetection.association;

import java.util.UUID;

/**
 * A specific expected NodeStation instance is missing from a Collection
 */
public class MissingNodeStationException extends Exception {

  /**
   *
   */
  MissingNodeStationException(UUID uuid) {
    super("Station " + uuid + " is missing.");
  }
  /**
   *
   * @param uuid
   * @param cause
   */
  MissingNodeStationException(UUID uuid, Throwable cause) {
    super("Station " + uuid + " is missing.", cause);
  }
}
