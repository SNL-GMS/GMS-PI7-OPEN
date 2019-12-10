package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.configuration;

public class Endpoints {

  public static final String NETWORKS = "networks";
  public static final String PROCESSING_NETWORKS = "networks/processing/name/";
  public static final String PROCESSING_STATIONS = "stations/processing/name/";
  public static final String PROCESSING_SITES = "sites/processing/name/";
  public static final String STATIONS = "stations";
  public static final String SITES = "sites";
  public static final String DIGITIZERS = "digitizers";
  public static final String CHANNELS = "channels";
  public static final String SENSORS = "sensors";
  public static final String RESPONSES = "responses";
  public static final String CALIBRATIONS = "calibrations";
  public static final String NETWORK_MEMBERSHIPS = "network-memberships";
  public static final String STATION_MEMBERSHIPS = "station-memberships";
  public static final String SITE_MEMBERSHIPS = "site-memberships";
  public static final String DIGITIZER_MEMBERSHIPS = "digitizer-memberships";
  //STORE operations use the /coi/ prefix
  public static final String STORE_NETWORKS = "/coi/reference-networks";
  public static final String STORE_STATIONS = "/coi/reference-stations";
  public static final String STORE_SITES = "/coi/reference-sites";
  public static final String STORE_CHANNELS = "/coi/reference-channels";
  public static final String STORE_CALIBRATIONS = "/coi/reference-calibrations";
  public static final String STORE_RESPONSES = "/coi/reference-responses";
  public static final String STORE_SENSORS = "/coi/reference-sensors";
  public static final String STORE_NETWORK_MEMBERSHIPS = "/coi/reference-network-memberships";
  public static final String STORE_STATION_MEMBERSHIPS = "/coi/reference-station-memberships";
  public static final String STORE_SITE_MEMBERSHIPS = "/coi/reference-site-memberships";
  // New HTTP conventions require use of /coi/ prefix for both query and store ops
  public static final String STATIONS_BY_VERSION_IDS = "/coi/stations/query/versionIds";
  public static final String CHANNELS_BY_VERSION_IDS = "/coi/channels/query/versionIds";
}
