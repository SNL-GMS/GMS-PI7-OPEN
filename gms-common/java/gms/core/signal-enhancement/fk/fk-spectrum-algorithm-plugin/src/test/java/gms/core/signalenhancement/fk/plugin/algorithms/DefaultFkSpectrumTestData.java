package gms.core.signalenhancement.fk.plugin.algorithms;

import static java.time.temporal.ChronoUnit.SECONDS;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class DefaultFkSpectrumTestData {

  private static final Waveform wf = Waveform
      .withValues(Instant.EPOCH, 4, new double[] {
          1, 2, 3, 4, 5,
          1, 2, 3, 4, 5,
          1, 2, 3, 4, 5,
          1, 2, 3, 4, 5});

  private static final UUID channelId1 = UUID.fromString("465bb3c9-45be-4163-8dc7-9d90bf7b6fb9");
  private static final UUID channelId2 = UUID.fromString("c3649d2b-421b-4aa9-a4a9-c026b794c4b3");
  private static final UUID channelId3 = UUID.fromString("bb8203f9-f186-492d-b519-2ecbfe02b36b");

  private static final ChannelSegment<Waveform> channelSegment1 = ChannelSegment
      .from(UUID.randomUUID(), channelId1, "MockSegment1",
          ChannelSegment.Type.RAW, List.of(wf), CreationInfo.DEFAULT);

  private static final ChannelSegment<Waveform> channelSegment2 = ChannelSegment
      .from(UUID.randomUUID(), channelId2, "MockSegment2",
          ChannelSegment.Type.RAW, List.of(wf), CreationInfo.DEFAULT);

  public static final List<ChannelSegment<Waveform>> CHANNEL_SEGMENTS = List
      .of(channelSegment1, channelSegment2);

  private static final Location beamPoint = Location.from(0.0, 0.0, 0.0, 0.0);

  private static Map<UUID, RelativePosition> relativePositionMapChan1Chan2 = Map
      .of(channelId1, RelativePosition.from(0, 0, 0),
          channelId2, RelativePosition.from(0, 0, 0));

  private static Map<UUID, RelativePosition> relativePositionMapChan1Chan3 = Map
      .of(channelId1, RelativePosition.from(0, 0, 0),
          channelId3, RelativePosition.from(0, 0, 0));

  public static final FkSpectraDefinition FK_SPECTRUM_DEFINITION_CHANS_1_AND_2 =
      FkSpectraDefinition.builder()
          .setWindowLead(Duration.ZERO)
          .setWindowLength(Duration.of(4, SECONDS))
          .setSampleRateHz(0.5)
          .setLowFrequencyHz(1)
          .setHighFrequencyHz(2)
          .setUseChannelVerticalOffsets(true)
          .setNormalizeWaveforms(false)
          .setPhaseType(PhaseType.P)
          .setSlowStartXSecPerKm(22)
          .setSlowDeltaXSecPerKm(23)
          .setSlowCountX(24)
          .setSlowStartYSecPerKm(66)
          .setSlowDeltaYSecPerKm(68)
          .setSlowCountY(69)
          .setWaveformSampleRateHz(4)
          .setWaveformSampleRateToleranceHz(.01)
          .setBeamPoint(beamPoint)
          .setRelativePositionsByChannelId(relativePositionMapChan1Chan2)
          .setMinimumWaveformsForSpectra(2)
          .build();

  public static final FkSpectraDefinition FK_SPECTRUM_DEFINITION_CHANS_1_AND_3 =
      FkSpectraDefinition.builder()
          .setWindowLead(Duration.ZERO)
          .setWindowLength(Duration.of(4, SECONDS))
          .setSampleRateHz(0.5)
          .setLowFrequencyHz(1)
          .setHighFrequencyHz(2)
          .setUseChannelVerticalOffsets(true)
          .setNormalizeWaveforms(false)
          .setPhaseType(PhaseType.P)
          .setSlowStartXSecPerKm(22)
          .setSlowDeltaXSecPerKm(23)
          .setSlowCountX(24)
          .setSlowStartYSecPerKm(66)
          .setSlowDeltaYSecPerKm(68)
          .setSlowCountY(69)
          .setWaveformSampleRateHz(4)
          .setWaveformSampleRateToleranceHz(.01)
          .setRelativePositionsByChannelId(relativePositionMapChan1Chan3)
          .setBeamPoint(beamPoint)
          .setMinimumWaveformsForSpectra(2)
          .build();
}
