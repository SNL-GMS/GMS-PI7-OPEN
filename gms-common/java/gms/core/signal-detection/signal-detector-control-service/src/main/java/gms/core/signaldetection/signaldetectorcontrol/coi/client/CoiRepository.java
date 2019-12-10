package gms.core.signaldetection.signaldetectorcontrol.coi.client;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CoiRepository {

  List<ChannelSegment<Waveform>> getChannelSegments(Collection<UUID> channelIds,
      Instant startTime,
      Instant endTime);

  List<UUID> storeSignalDetections(Collection<SignalDetection> signalDetections);

  void storeChannelSegments(Collection<ChannelSegment<Waveform>> channelSegments);

  // TODO: storeSignalDetections CreationInformation after settling CreationInformation vs. CreationInfo
  // TODO: handle private vs public StorageVisibility
}
