package gms.core.signalenhancement.beam;

import gms.core.signalenhancement.beam.core.BeamDefinitionAndChannelIdPair;
import gms.core.signalenhancement.beam.core.BeamStreamingCommand;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroupType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ProcessingGroupDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamCreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Objects used in Signal Detector Control HTTP testing
 */
public class TestFixtures {

  public static final Waveform waveform = Waveform.from(
      Instant.now(),
      20,
      10,
      new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

  public static BeamStreamingCommand getBeamStreamingCommand() {
    return BeamStreamingCommand.create(UUID.randomUUID(),
        Set.of(
            channelSegmentFromWaveforms(
                List.of(
                    createWaveform(Instant.EPOCH, Instant.EPOCH.plusSeconds(10), 20.0))),
            channelSegmentFromWaveforms(
                List.of(
                    createWaveform(Instant.EPOCH.plusSeconds(11), Instant.EPOCH.plusSeconds(20),
                        20.0))
            )),
        getBeamDefinition());
  }

  public static BeamDefinition getBeamDefinition() {
    return BeamDefinition
        .from(PhaseType.P, 105.33864941019878, 0.06751015004210807, true, true, true, 40.0, 1,
            Location.from(0.0, 0.0, 0.0, 0.0),
            Map.of(UUID.randomUUID(), RelativePosition.from(0, 0, 0)),
            2);
  }

  public static BeamCreationInfo getBeamCreationInfo() {
    return BeamCreationInfo.builder()
        .generatedId()
        .setCreationTime(Instant.EPOCH)
        .setName(waveformChannelSegment().getName())
        .setProcessingGroupId(UUID.randomUUID())
        .setChannelId(UUID.randomUUID())
        .setChannelSegmentId(UUID.randomUUID())
        .setRequestedStartTime(Instant.EPOCH)
        .setRequestedEndTime(Instant.EPOCH.plusSeconds(5))
        .setBeamDefinition(getBeamDefinition())
        .setUsedInputChannelIds(Set.of(UUID.randomUUID()))
        .build();
  }

  public static ChannelProcessingGroup getChannelProcessingGroup() {
    return ChannelProcessingGroup.from(UUID.fromString("e191db75-b06a-4934-b19d-20554deecd7a"),
        ChannelProcessingGroupType.BEAM,
        Set.of(UUID.randomUUID()),
        Instant.EPOCH,
        Instant.EPOCH,
        "Status",
        "Test channel processing group");
  }

  public static ProcessingGroupDescriptor getProcessingGroupDescriptor() {
    Instant startTime = Instant.now();
    return ProcessingGroupDescriptor
        .create(UUID.fromString("e191db75-b06a-4934-b19d-20554deecd7a"),
            startTime,
            startTime.plusMillis(1000)
        );
  }

  public static ChannelSegment<Waveform> waveformChannelSegment() {

    final UUID channelSegmentId = UUID.randomUUID();

    return ChannelSegment.from(UUID.randomUUID(),
        UUID.randomUUID(),
        "MockSegment",
        ChannelSegment.Type.DETECTION_BEAM,
        List.of(waveform),
        CreationInfo.DEFAULT);
  }

  public static ChannelSegment<Waveform> randomWaveformChannelSegment() {
    final Waveform wf1 = Waveform
        .withValues(Instant.EPOCH, 40, randoms(80));

    return ChannelSegment.from(UUID.randomUUID(), UUID.randomUUID(), "ChannelName",
        ChannelSegment.Type.RAW, List.of(wf1), CreationInfo.DEFAULT);
  }

  private static ChannelSegment<Waveform> channelSegmentFromWaveforms(List<Waveform> waveforms) {
    return ChannelSegment.from(UUID.randomUUID(), UUID.randomUUID(), "ChannelName",
        ChannelSegment.Type.RAW, waveforms, CreationInfo.DEFAULT);
  }

  private static Waveform createWaveform(Instant start, Instant end, double samplesPerSec) {
    if (0 != Duration.between(start, end).getNano()) {
      throw new IllegalArgumentException(
          "Test can't create waveform where the sample rate does not evenly divide the duration");
    }

    int numSamples = (int) (Duration.between(start, end).getSeconds() * samplesPerSec) + 1;
    double[] values = new double[numSamples];
    Arrays.fill(values, 1.0);

    return Waveform.withValues(start, samplesPerSec, values);
  }

  private static double[] randoms(int length) {
    double[] random = new double[length];
    IntStream.range(0, length).forEach(i -> random[i] = Math.random());
    return random;
  }

  private static ProcessingContext getProcessingContext() {
    return ProcessingContext
        .createAutomatic(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            StorageVisibility.PRIVATE);
  }
}
