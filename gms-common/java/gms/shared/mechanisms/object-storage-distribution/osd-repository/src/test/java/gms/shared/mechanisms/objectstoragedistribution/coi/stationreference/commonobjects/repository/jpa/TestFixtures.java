package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.DigitizerManufacturers;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.DigitizerModels;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkOrganization;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkRegion;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceCalibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceDigitizer;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceDigitizerMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetworkMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSensor;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSite;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSiteMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStationMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ResponseTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StatusType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TestFixtures {

  public static ReferenceStation jnu_v1, jnu_v2, jnu_v3;
  public static List<ReferenceStation> jnuVersions;
  public static ReferenceNetwork net_ims_aux, net_idc_da;
  public static ReferenceChannel
    chan_jnu_bhe_v1, chan_jnu_bhe_v2, chan_jnu_bhe_v3,
    chan_jnu_bhn_v1, chan_jnu_bhn_v2, chan_jnu_bhn_v3,
    chan_jnu_bhz_v1, chan_jnu_bhz_v2, chan_jnu_bhz_v3;
  public static List<ReferenceChannel> allChannels;
  public static ReferenceSite jnu_site_v1, jnu_site_v2, jnu_site_v3;
  public static List<ReferenceSite> jnuSiteVersions;
  public static ReferenceDigitizer jnu_digitizer_v1, jnu_digitizer_v2, jnu_digitizer_v3;
  public static List<ReferenceDigitizer> allDigitizers;
  public static Set<ReferenceNetworkMembership> networkMemberships;
  public static Set<ReferenceStationMembership> stationMemberships;
  public static Set<ReferenceSiteMembership> siteMemberships;
  public static Set<ReferenceDigitizerMembership> digitizerMemberships;
  private static final double NA_VALUE = -999.0;
  public static final Location location = Location.from(0, 0, 0, 0);
  private static final RelativePosition ZERO_POSITION = RelativePosition.from(
      0.0, 0.0, 0.0);
  public static final Instant
      changeTime1 = Instant.ofEpochSecond(797731200),
      changeTime2 = Instant.ofEpochSecond(1195430400),
      changeTime3 = Instant.ofEpochSecond(1232496000);
  public static final InformationSource infoSource = InformationSource.create(
      "IDC", Instant.now(), "IDC");
  public static final ReferenceSensor
      sensor_bhe_v1, sensor_bhe_v2, sensor_bhe_v3,
      sensor_bhn_v1, sensor_bhn_v2, sensor_bhn_v3,
      sensor_bhz_v1, sensor_bhz_v2, sensor_bhz_v3;
  public static final List<ReferenceSensor> allSensors;
  public static final ReferenceCalibration
      calibration_bhe_v1, calibration_bhe_v2, calibration_bhe_v3,
      calibration_bhn_v1, calibration_bhn_v2, calibration_bhn_v3,
      calibration_bhz_v1, calibration_bhz_v2, calibration_bhz_v3;
  public static final List<ReferenceCalibration> allCalibrations;
  public static final ReferenceResponse
      response_bhe_v1, response_bhe_v2, response_bhe_v3,
      response_bhn_v1, response_bhn_v2, response_bhn_v3,
      response_bhz_v1, response_bhz_v2, response_bhz_v3;
  public static final List<ReferenceResponse> allResponses;


  // Initialize the station reference info
  static {
    // Define networks
    Instant net_imx_aux_changeTime = Instant.ofEpochSecond(604713600);
    net_ims_aux = ReferenceNetwork.create("IMS_AUX",
        "All IMS auxiliary seismic stations", NetworkOrganization.CTBTO,
        NetworkRegion.GLOBAL, infoSource,
        "", net_imx_aux_changeTime, net_imx_aux_changeTime);

    Instant idc_da_changeTime = Instant.ofEpochSecond(228700800);
    net_idc_da = ReferenceNetwork.create("IDC_DA",
        "All acquired stations - used by update interval",
        NetworkOrganization.UNKNOWN, NetworkRegion.GLOBAL, infoSource,
        "", idc_da_changeTime, idc_da_changeTime);
    ////////////////////////////////////////////////////////////////////////////
    // Stations - 3 versions of 'JNU'
    jnu_v1 = ReferenceStation.create("JNU",
        "Ohita, Japan", StationType.Seismic3Component, infoSource, "",
        33.1217, 130.8783, 0.54, changeTime1, changeTime1,
        new ArrayList<>());

    jnu_v2 = ReferenceStation.create("JNU",
        "", StationType.Seismic3Component, infoSource, "upgrade for IMS",
        NA_VALUE, NA_VALUE, NA_VALUE, changeTime2, changeTime2,
        new ArrayList<>());

    jnu_v3 = ReferenceStation.create("JNU",
        "Oita Nakatsue, Japan Meterological Agency Seismic Network",
        StationType.Seismic3Component, infoSource,
        "", 33.121667, 130.87833, 0.573,
        changeTime3, changeTime3, new ArrayList<>());
    ////////////////////////////////////////////////////////////////////////////
    // Sites - 3 versions of the one that is in JNU.
    jnu_site_v1 = ReferenceSite.create("JNU",
        "Ohita, Japan",
        infoSource, "", 33.1217, 130.8783, 0.54,
        changeTime1, changeTime1, ZERO_POSITION,
        new ArrayList<>());

    jnu_site_v2 = ReferenceSite.create("JNU",
        "", infoSource, "upgrade for IMS", NA_VALUE, NA_VALUE, NA_VALUE,
        changeTime2, changeTime2, ZERO_POSITION, new ArrayList<>());

    jnu_site_v3 = ReferenceSite.create("JNU",
        "Oita Nakatsue, Japan Meterological Agency Seismic Network",
        infoSource, "", 33.121667, 130.87833, 0.573,
        changeTime3, changeTime3, ZERO_POSITION, new ArrayList<>());
    //////////////////////////////////////////////////////////////////////////////////////
    // Digitizers
    jnu_digitizer_v1 = ReferenceDigitizer.create(
        "jnu digitizer", DigitizerManufacturers.NANOMETRICS,
        DigitizerModels.UNKNOWN, "5612", changeTime1, changeTime1,
        infoSource, "", "unknown Nanometrics type digitizer");

    jnu_digitizer_v2 = ReferenceDigitizer.create(
        "jnu digitizer", DigitizerManufacturers.UNKNOWN, DigitizerModels.UNKNOWN,
        "-", changeTime2, changeTime2, infoSource, "", "decommissioned");

    jnu_digitizer_v3 = ReferenceDigitizer.create(
        "jnu digitizer", DigitizerManufacturers.NANOMETRICS, DigitizerModels.EUROPA_T,
        "724", changeTime3, changeTime3, infoSource, "",
        "unknown Nanometrics type digitizer");
    //////////////////////////////////////////////////////////////////////////////////////
    // Channels
    // Channel BHE (3 versions)
    chan_jnu_bhe_v1 = ReferenceChannel.create("BHE",
        ChannelType.BROADBAND_HIGH_GAIN_EAST_WEST, ChannelDataType.SEISMIC_3_COMPONENT,
        "0", 33.1217, 130.8783, 0.54,
        1, 90, 90, 20,
        changeTime1, changeTime1, infoSource,
        "", ZERO_POSITION, new ArrayList<>());

    chan_jnu_bhe_v2 = ReferenceChannel.create("BHE",
        ChannelType.BROADBAND_HIGH_GAIN_EAST_WEST, ChannelDataType.SEISMIC_3_COMPONENT,
        "0", NA_VALUE, NA_VALUE, NA_VALUE,
        NA_VALUE, -1, -1, 20,
        changeTime2, changeTime2, infoSource,
        "decommissioned", ZERO_POSITION, new ArrayList<>());

    chan_jnu_bhe_v3 = ReferenceChannel.create("BHE",
        ChannelType.BROADBAND_HIGH_GAIN_EAST_WEST, ChannelDataType.SEISMIC_3_COMPONENT,
        "0", 33.121667, 130.87833, 0.573, 0,
        90, 90, 40,
        changeTime3, changeTime3, infoSource,
        "decommissioned", ZERO_POSITION, new ArrayList<>());
    // Channel BHN (3 versions)
    chan_jnu_bhn_v1 = ReferenceChannel.create("BHN",
        ChannelType.BROADBAND_HIGH_GAIN_NORTH_SOUTH, ChannelDataType.SEISMIC_3_COMPONENT,
        "0", 33.1217, 130.8783, 0.54,
        1, 90, 0, 20,
        changeTime1, changeTime1, infoSource,
        "", ZERO_POSITION, new ArrayList<>());

    chan_jnu_bhn_v2 = ReferenceChannel.create("BHN",
        ChannelType.BROADBAND_HIGH_GAIN_NORTH_SOUTH, ChannelDataType.SEISMIC_3_COMPONENT,
        "0", NA_VALUE, NA_VALUE, NA_VALUE,
        NA_VALUE, -1, -1, 20,
        changeTime2, changeTime2, infoSource,
        "decommissioned", ZERO_POSITION, new ArrayList<>());

    chan_jnu_bhn_v3 = ReferenceChannel.create("BHN",
        ChannelType.BROADBAND_HIGH_GAIN_NORTH_SOUTH, ChannelDataType.SEISMIC_3_COMPONENT,
        "0", 33.121667, 130.87833, 0.573, 0,
        90, 0, 40,
        changeTime3, changeTime3, infoSource,
        "decommissioned", ZERO_POSITION, new ArrayList<>());
    //////////////////////////////////////////////////////////////////////////////////////
    // Channel BHZ (3 versions)
    chan_jnu_bhz_v1 = ReferenceChannel.create("BHZ",
        ChannelType.BROADBAND_HIGH_GAIN_VERTICAL, ChannelDataType.SEISMIC_3_COMPONENT,
        "0", 33.1217, 130.8783, 0.54,
        1, 0, -1, 20,
        changeTime1, changeTime1, infoSource,
        "", ZERO_POSITION, new ArrayList<>());

    chan_jnu_bhz_v2 = ReferenceChannel.create("BHZ",
        ChannelType.BROADBAND_HIGH_GAIN_VERTICAL, ChannelDataType.SEISMIC_3_COMPONENT,
        "0", NA_VALUE, NA_VALUE, NA_VALUE,
        NA_VALUE, 0, -1, 20,
        changeTime2, changeTime2, infoSource,
        "decommissioned", ZERO_POSITION, new ArrayList<>());

    chan_jnu_bhz_v3 = ReferenceChannel.create("BHZ",
        ChannelType.BROADBAND_HIGH_GAIN_VERTICAL, ChannelDataType.SEISMIC_3_COMPONENT,
        "0", 33.121667, 130.87833, 0.573, 0,
        0, -1, 40,
        changeTime3, changeTime3, infoSource,
        "decommissioned", ZERO_POSITION, new ArrayList<>());
    //////////////////////////////////////////////////////////////////////////////////////
    // sensors
    // bhe sensors
    sensor_bhe_v1 = ReferenceSensor.create(chan_jnu_bhe_v1.getEntityId(),
        "G. Streckeisen AG", "STS-2",
        "65345", 3, -1, 0.02, 5,
        changeTime1, changeTime1, infoSource, "");
    sensor_bhe_v2 = ReferenceSensor.create(chan_jnu_bhe_v2.getEntityId(),
        "-", "-",
        "-", -1, -1, -1, -1,
        changeTime2, changeTime2, infoSource, "decommissioned");
    sensor_bhe_v3 = ReferenceSensor.create(chan_jnu_bhe_v3.getEntityId(),
        "G. Streckeisen AG", "STS-2 1500 V/m/s",
        "65345-2", 3, -1, 0.003, 16,
        changeTime3, changeTime3, infoSource, "");
    // bhn sensors
    sensor_bhn_v1 = ReferenceSensor.create(chan_jnu_bhn_v1.getEntityId(),
        "G. Streckeisen AG", "STS-2",
        "65346", 3, -1, 0.02, 5,
        changeTime1, changeTime1, infoSource, "");
    sensor_bhn_v2 = ReferenceSensor.create(chan_jnu_bhn_v2.getEntityId(),
        "-", "-",
        "-", -1, -1, -1, -1,
        changeTime2, changeTime2, infoSource, "decommissioned");
    sensor_bhn_v3 = ReferenceSensor.create(chan_jnu_bhn_v3.getEntityId(),
        "G. Streckeisen AG", "STS-2 1500 V/m/s",
        "65346-2", 3, -1, 0.003, 16,
        changeTime3, changeTime3, infoSource, "");
    // bhz sensors
    sensor_bhz_v1 = ReferenceSensor.create(chan_jnu_bhz_v1.getEntityId(),
        "G. Streckeisen AG", "STS-2",
        "65347", 3, -1, 0.02, 5,
        changeTime1, changeTime1, infoSource, "");
    sensor_bhz_v2 = ReferenceSensor.create(chan_jnu_bhz_v2.getEntityId(),
        "-", "-",
        "-", -1, -1, -1, -1,
        changeTime2, changeTime2, infoSource, "decommissioned");
    sensor_bhz_v3 = ReferenceSensor.create(chan_jnu_bhz_v3.getEntityId(),
        "G. Streckeisen AG", "STS-2 1500 V/m/s",
        "65347-2", 3, -1, 0.003, 16,
        changeTime3, changeTime3, infoSource, "");
    //////////////////////////////////////////////////////////////////////////////////////
    // calibrations
    // bhe calibrations
    calibration_bhe_v1 = ReferenceCalibration.create(chan_jnu_bhe_v1.getEntityId(),
        NA_VALUE, 0.16, NA_VALUE, 1, 0,
        changeTime1, changeTime1, infoSource, "");
    calibration_bhe_v2 = ReferenceCalibration.create(chan_jnu_bhe_v2.getEntityId(),
        NA_VALUE, NA_VALUE, NA_VALUE, NA_VALUE, NA_VALUE,
        changeTime2, changeTime2, infoSource, "");
    calibration_bhe_v3 = ReferenceCalibration.create(chan_jnu_bhe_v3.getEntityId(),
        NA_VALUE, 0.0318, NA_VALUE, 1, 0,
        changeTime3, changeTime3, infoSource, "");
    // bhn calibrations
    calibration_bhn_v1 = ReferenceCalibration.create(chan_jnu_bhn_v1.getEntityId(),
        NA_VALUE, 0.16, NA_VALUE, 1, 0,
        changeTime1, changeTime1, infoSource, "");
    calibration_bhn_v2 = ReferenceCalibration.create(chan_jnu_bhn_v2.getEntityId(),
        NA_VALUE, NA_VALUE, NA_VALUE, NA_VALUE, NA_VALUE,
        changeTime2, changeTime2, infoSource, "");
    calibration_bhn_v3 = ReferenceCalibration.create(chan_jnu_bhn_v3.getEntityId(),
        NA_VALUE, 0.0318, NA_VALUE, 1, 0,
        changeTime3, changeTime3, infoSource, "");
    // bhz calibrations
    calibration_bhz_v1 = ReferenceCalibration.create(chan_jnu_bhz_v1.getEntityId(),
        NA_VALUE, 0.16, NA_VALUE, 1, 0,
        changeTime1, changeTime1, infoSource, "");
    calibration_bhz_v2 = ReferenceCalibration.create(chan_jnu_bhz_v2.getEntityId(),
        NA_VALUE, NA_VALUE, NA_VALUE, NA_VALUE, NA_VALUE,
        changeTime2, changeTime2, infoSource, "");
    calibration_bhz_v3 = ReferenceCalibration.create(chan_jnu_bhz_v3.getEntityId(),
        NA_VALUE, 0.0318, NA_VALUE, 1, 0,
        changeTime3, changeTime3, infoSource, "");
    //////////////////////////////////////////////////////////////////////////////////////
    // responses
    // bhe responses
    response_bhe_v1 = ReferenceResponse.create(chan_jnu_bhe_v1.getEntityId(),
        ResponseTypes.FAP, new byte[]{(byte) 0}, "nm/s", changeTime1, changeTime1, infoSource, "");
    response_bhe_v2 = ReferenceResponse.create(chan_jnu_bhe_v2.getEntityId(),
        ResponseTypes.FAP, new byte[]{(byte) 1}, "nm/s", changeTime2, changeTime2, infoSource, "");
    response_bhe_v3 = ReferenceResponse.create(chan_jnu_bhe_v3.getEntityId(),
        ResponseTypes.FAP, new byte[]{(byte) 2}, "nm/s", changeTime3, changeTime3, infoSource, "");
    // bhn responses
    response_bhn_v1 = ReferenceResponse.create(chan_jnu_bhn_v1.getEntityId(),
        ResponseTypes.FAP, new byte[]{(byte) 0}, "nm/s", changeTime1, changeTime1, infoSource, "");
    response_bhn_v2 = ReferenceResponse.create(chan_jnu_bhn_v2.getEntityId(),
        ResponseTypes.FAP, new byte[]{(byte) 1}, "nm/s", changeTime2, changeTime2, infoSource, "");
    response_bhn_v3 = ReferenceResponse.create(chan_jnu_bhn_v3.getEntityId(),
        ResponseTypes.FAP, new byte[]{(byte) 2}, "nm/s", changeTime3, changeTime3, infoSource, "");
    // bhz responses
    response_bhz_v1 = ReferenceResponse.create(chan_jnu_bhz_v1.getEntityId(),
        ResponseTypes.FAP, new byte[]{(byte) 0}, "nm/s", changeTime1, changeTime1, infoSource, "");
    response_bhz_v2 = ReferenceResponse.create(chan_jnu_bhz_v2.getEntityId(),
        ResponseTypes.FAP, new byte[]{(byte) 1}, "nm/s", changeTime2, changeTime2, infoSource, "");
    response_bhz_v3 = ReferenceResponse.create(chan_jnu_bhz_v3.getEntityId(),
        ResponseTypes.FAP, new byte[]{(byte) 2}, "nm/s", changeTime3, changeTime3, infoSource, "");
    //////////////////////////////////////////////////////////////////////////////////////
    addAliasesToChannels();
    addAliasesToSites();
    addAliasesToStations();

    associateStationsAndNetworks();
    associateSitesAndStations();
    associateDigitizersAndSites();
    associateChannelsAndDigitizers();

    jnuVersions = List.of(jnu_v1, jnu_v2, jnu_v3);
    jnuSiteVersions = List.of(jnu_site_v1, jnu_site_v2, jnu_site_v3);
    allChannels = List.of(chan_jnu_bhe_v1, chan_jnu_bhe_v2, chan_jnu_bhe_v3,
        chan_jnu_bhn_v1, chan_jnu_bhn_v2, chan_jnu_bhn_v3,
        chan_jnu_bhz_v1, chan_jnu_bhz_v2, chan_jnu_bhz_v3);
    allDigitizers = List.of(jnu_digitizer_v1, jnu_digitizer_v2, jnu_digitizer_v3);
    allSensors = List.of(sensor_bhe_v1, sensor_bhn_v1, sensor_bhz_v1,
        sensor_bhe_v2, sensor_bhn_v2, sensor_bhz_v2,
        sensor_bhe_v3, sensor_bhn_v3, sensor_bhz_v3);
    allResponses = List.of(response_bhe_v1, response_bhn_v1, response_bhz_v1,
        response_bhe_v2, response_bhn_v2, response_bhz_v2,
        response_bhe_v3, response_bhn_v3, response_bhz_v3);
    allCalibrations = List.of(calibration_bhe_v1, calibration_bhn_v1, calibration_bhz_v1,
        calibration_bhe_v2, calibration_bhn_v2, calibration_bhz_v2,
        calibration_bhe_v3, calibration_bhn_v3, calibration_bhz_v3);
  }

  private static void addAliasesToStations() {
    // TODO: fill in?
  }

  private static void addAliasesToSites() {
    // TODO: fill in?
  }

  private static void addAliasesToChannels() {
    // TODO: fill in?
  }

  private static void associateStationsAndNetworks() {
    // declare memberships
    UUID jnu_id = jnu_v1.getEntityId();
    ReferenceNetworkMembership ims_member_1 = ReferenceNetworkMembership.from(
        UUID.randomUUID(), "", changeTime1, changeTime1,
        net_ims_aux.getEntityId(), jnu_id, StatusType.ACTIVE);
    ReferenceNetworkMembership ims_member_2 = ReferenceNetworkMembership.from(
        UUID.randomUUID(), "", changeTime2, changeTime2,
        net_ims_aux.getEntityId(), jnu_id, StatusType.INACTIVE);
    ReferenceNetworkMembership ims_member_3 = ReferenceNetworkMembership.from(
        UUID.randomUUID(), "", changeTime3, changeTime3,
        net_ims_aux.getEntityId(), jnu_id, StatusType.ACTIVE);
    ReferenceNetworkMembership idc_member_1 = ReferenceNetworkMembership.from(
        UUID.randomUUID(), "", changeTime1, changeTime1,
        net_idc_da.getEntityId(), jnu_id, StatusType.ACTIVE);
    ReferenceNetworkMembership idc_member_2 = ReferenceNetworkMembership.from(
        UUID.randomUUID(), "", changeTime2, changeTime2,
        net_idc_da.getEntityId(), jnu_id, StatusType.INACTIVE);
    ReferenceNetworkMembership idc_member_3 = ReferenceNetworkMembership.from(
        UUID.randomUUID(), "", changeTime3, changeTime3,
        net_idc_da.getEntityId(), jnu_id, StatusType.ACTIVE);
    // set reference to all memberships
    networkMemberships = Set.of(ims_member_1, ims_member_2, ims_member_3,
        idc_member_1, idc_member_2, idc_member_3);
  }

  private static void associateSitesAndStations() {
    UUID jnu_id = jnu_v1.getEntityId();
    UUID jnu_site_id = jnu_site_v1.getEntityId();
    ReferenceStationMembership member1 = ReferenceStationMembership.from(
        UUID.randomUUID(), "", changeTime1, changeTime1,
        jnu_id, jnu_site_id, StatusType.ACTIVE);
    ReferenceStationMembership member2 = ReferenceStationMembership.from(
        UUID.randomUUID(), "", changeTime2, changeTime2,
        jnu_id, jnu_site_id, StatusType.INACTIVE);
    ReferenceStationMembership member3 = ReferenceStationMembership.from(
        UUID.randomUUID(), "", changeTime3, changeTime3,
        jnu_id, jnu_site_id, StatusType.ACTIVE);
    stationMemberships = Set.of(member1, member2, member3);
  }

  private static void associateDigitizersAndSites() {
    UUID jnu_site_id = jnu_site_v1.getEntityId();
    UUID digi_id = jnu_digitizer_v1.getEntityId();
    ReferenceSiteMembership member1 = ReferenceSiteMembership.from(
        UUID.randomUUID(), "", changeTime1, changeTime1,
        jnu_site_id, digi_id, StatusType.ACTIVE);
    ReferenceSiteMembership member2 = ReferenceSiteMembership.from(
        UUID.randomUUID(), "", changeTime2, changeTime2,
        jnu_site_id, digi_id, StatusType.INACTIVE);
    ReferenceSiteMembership member3 = ReferenceSiteMembership.from(
        UUID.randomUUID(), "", changeTime3, changeTime3,
        jnu_site_id, digi_id, StatusType.ACTIVE);
    siteMemberships = Set.of(member1, member2, member3);
  }

  private static void associateChannelsAndDigitizers() {
    UUID digi_id = jnu_digitizer_v1.getEntityId();
    Set<UUID> chanIds = Set.of(chan_jnu_bhe_v1.getEntityId(), chan_jnu_bhn_v1.getEntityId(),
        chan_jnu_bhz_v1.getEntityId());
    digitizerMemberships = new HashSet<>();
    for (UUID chanId : chanIds) {
      ReferenceDigitizerMembership membership1 = ReferenceDigitizerMembership.from(
          UUID.randomUUID(), "", changeTime1, changeTime1,
          digi_id, chanId, StatusType.ACTIVE);
      ReferenceDigitizerMembership membership2 = ReferenceDigitizerMembership.from(
          UUID.randomUUID(), "", changeTime2, changeTime2,
          digi_id, chanId, StatusType.INACTIVE);
      ReferenceDigitizerMembership membership3 = ReferenceDigitizerMembership.from(
          UUID.randomUUID(), "", changeTime3, changeTime3,
          digi_id, chanId, StatusType.ACTIVE);
      digitizerMemberships.add(membership1);
      digitizerMemberships.add(membership2);
      digitizerMemberships.add(membership3);
    }
  }
}
