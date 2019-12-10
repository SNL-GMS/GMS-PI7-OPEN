package gms.dataacquisition.stationreceiver.osdclient;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.*;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TestFixtures {

  static final ChannelSegment<Waveform> segment = ChannelSegment.create(
      UUID.randomUUID(), "segmentName", ChannelSegment.Type.ACQUIRED,
      List.of(Waveform.withValues(Instant.EPOCH, 40.0,
          new double[]{0, 1, 2, 3, 4, 5})), CreationInfo.DEFAULT);

  static final AcquiredChannelSohAnalog sohAnalog = AcquiredChannelSohAnalog.create(
      UUID.randomUUID(), AcquiredChannelSohType.STATION_POWER_VOLTAGE,
      Instant.EPOCH, Instant.EPOCH.plusSeconds(10), 1.0, CreationInfo.DEFAULT);

  static final AcquiredChannelSohBoolean sohBoolean = AcquiredChannelSohBoolean.create(
      UUID.randomUUID(), AcquiredChannelSohType.VAULT_DOOR_OPENED,
      Instant.EPOCH, Instant.EPOCH.plusSeconds(10), true, CreationInfo.DEFAULT);

  static final RawStationDataFrame frame = RawStationDataFrame.create(
          UUID.randomUUID(), Set.of(UUID.randomUUID()), AcquisitionProtocol.CD11,
      Instant.EPOCH, Instant.EPOCH.plusSeconds(10), Instant.EPOCH.plusSeconds(11),
      "foo".getBytes(), AuthenticationStatus.NOT_YET_AUTHENITCATED, CreationInfo.DEFAULT);

  static final String channelName = "chan", siteName = "site";

  static final Channel channel = Channel.create(
      siteName + "/" + channelName, ChannelType.BROADBAND_HIGH_GAIN_EAST_WEST,
      ChannelDataType.SEISMIC_ARRAY, 1.0, 2.0,
      3.0, 4.0, 5.0, 6.0, 7.0);

  static final Site site = Site.create(
      siteName, 1.0, 2.0, 3.0, Set.of(channel));

  static final Station station = Station.create(
      "station", "", StationType.Seismic1Component, 1.0, 2.0, 3.0,
      Set.of(site));
}
