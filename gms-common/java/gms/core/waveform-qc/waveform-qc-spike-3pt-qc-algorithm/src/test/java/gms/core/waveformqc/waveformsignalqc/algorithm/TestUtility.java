package gms.core.waveformqc.waveformsignalqc.algorithm;


import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TestUtility {

  public static Waveform createWaveform(Instant start, Instant end, double samplesPerSec) {
    if (0 != Duration.between(start, end).getNano()) {
      throw new IllegalArgumentException(
          "Test can't create waveform where the sample rate does not evenly divide the duration");
    }

    int numSamples = (int) (Duration.between(start, end).getSeconds() * samplesPerSec) + 1;
    double[] values = new double[numSamples];
    Arrays.fill(values, 1.0);

    return Waveform.withValues(start, samplesPerSec, values);
  }

  public static ChannelSegment<Waveform> createChannelSegment(List<Waveform> waveforms) {
    final Instant start = waveforms.get(0).getStartTime();
    final Instant end = waveforms.get(waveforms.size() - 1).getEndTime();

    return ChannelSegment.create(
        UUID.randomUUID(),
        "segmentName",
        ChannelSegment.Type.RAW,
        waveforms,
        new CreationInfo("test", Instant.now(), new SoftwareComponentInfo("test", "test")));
  }
}
