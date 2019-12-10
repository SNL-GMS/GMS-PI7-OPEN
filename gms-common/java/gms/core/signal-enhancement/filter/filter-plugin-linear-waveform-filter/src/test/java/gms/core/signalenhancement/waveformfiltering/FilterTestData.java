package gms.core.signalenhancement.waveformfiltering;

import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterCausality;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterPassBandType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class FilterTestData {

  final static double[] FORWARD_COEFFS = new double[]{5.5, 4.4, 3.3, 2.2, 1.1, -6.6};

  private final static FilterDefinition FIR_FILTER_DEF = FilterDefinition
      .firBuilder()
      .setName("libTest")
      .setDescription("libTestDesc")
      .setFilterPassBandType(FilterPassBandType.BAND_PASS)
      .setLowFrequencyHz(1.0)
      .setHighFrequencyHz(3.0)
      .setOrder(4)
      .setFilterSource(FilterSource.USER)
      .setFilterCausality(FilterCausality.CAUSAL)
      .setZeroPhase(false)
      .setSampleRate(20.0)
      .setSampleRateTolerance(1.0)
      .setbCoefficients(FORWARD_COEFFS)
      .setGroupDelaySecs(3.0)
      .build();

  final static Map<String, Object> FIR_FILTER_DEF_FIELD_MAP = ObjectSerialization
      .toFieldMap(FIR_FILTER_DEF);

  private final static SortedSet<Waveform> WAVEFORMS = new TreeSet<>();
  private final static SortedSet<Waveform> WAVEFORMS2 = new TreeSet<>();
  private final static double[] DATA = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0};

  static {
    WAVEFORMS.add(Waveform.withValues(Instant.EPOCH, 20.0, DATA));
    WAVEFORMS2.add(Waveform.withValues(Instant.EPOCH, 20.0, DATA));
    WAVEFORMS2.add(Waveform
        .withValues(WAVEFORMS2.first().getEndTime().plusMillis((long) (1000.0 / 20.0)), 20.0,
            DATA));
  }

  final static ChannelSegment<Waveform> CHANNEL_SEGMENT = ChannelSegment
      .create(UUID.randomUUID(), "TEST", ChannelSegment.Type.FILTER, WAVEFORMS,
          CreationInfo.DEFAULT);

  final static ChannelSegment<Waveform> CHANNEL_SEGMENT2 = ChannelSegment
      .create(UUID.randomUUID(), "TEST2", ChannelSegment.Type.FILTER, WAVEFORMS2,
          CreationInfo.DEFAULT);
}
