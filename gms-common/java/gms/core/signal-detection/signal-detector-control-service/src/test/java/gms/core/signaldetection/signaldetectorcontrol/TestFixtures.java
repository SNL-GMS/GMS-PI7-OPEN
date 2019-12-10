package gms.core.signaldetection.signaldetectorcontrol;

import gms.core.signaldetection.signaldetectorcontrol.http.StreamingDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Objects used in Signal Detector Control HTTP testing
 */
public class TestFixtures {

  public static final Waveform WAVEFORM;
  public static final Instant ARRIVAL_TIME1;
  public static final Instant ARRIVAL_TIME2;
  public static final Instant REFINED_ARRIVAL_TIME1;
  public static final Instant REFINED_ARRIVAL_TIME2;

  static {
    WAVEFORM = Waveform.withoutValues(Instant.EPOCH, 2, 280);
    ARRIVAL_TIME1 = WAVEFORM.computeSampleTime(130);
    ARRIVAL_TIME2 = WAVEFORM.computeSampleTime(170);
    REFINED_ARRIVAL_TIME1 = WAVEFORM.computeSampleTime(131); //TODO: Less arbitrary refined times?
    REFINED_ARRIVAL_TIME2 = WAVEFORM.computeSampleTime(171);
  }

  public static StreamingDto getStreamingDto() {
    StreamingDto dto = new StreamingDto();

    dto.setChannelSegment(TestFixtures.randomChannelSegment());
    dto.setStartTime(Instant.EPOCH);
    dto.setEndTime(Instant.EPOCH.plusSeconds(100));
    //TODO: to be included when SignalDetectorParameters is implemented
    //dto.setSignalDetectorParameters(getSignalDetectorParameters());
    dto.setProcessingContext(randomProcessingContext());

    return dto;
  }

  public static ChannelSegmentDescriptor randomChannelSegmentDescriptor() {
    return ChannelSegmentDescriptor
        .from(UUID.randomUUID(), (Instant.EPOCH), Instant.EPOCH.plusSeconds(100));
  }

  public static ChannelSegment<Waveform> randomChannelSegment() {
    return randomChannelSegment(UUID.randomUUID(), Instant.EPOCH);
  }

  public static ChannelSegment<Waveform> randomChannelSegment(UUID channelId, Instant start) {
    return ChannelSegment.create(channelId, "ChannelName",
        ChannelSegment.Type.RAW, List.of(WAVEFORM), CreationInfo.DEFAULT);
  }

  public static SignalDetection randomSignalDetection() {
    final Instant t = Instant.ofEpochMilli(1);
    final FeatureMeasurement<PhaseTypeMeasurementValue> phase = FeatureMeasurement.create(
        UUID.randomUUID(), FeatureMeasurementTypes.PHASE,
        PhaseTypeMeasurementValue.from(PhaseType.UNKNOWN, 1.0));
    final FeatureMeasurement<InstantValue> arrivalTime = FeatureMeasurement
        .create(UUID.randomUUID(),
            FeatureMeasurementTypes.ARRIVAL_TIME,
            InstantValue.from(t, Duration.ofMillis(1)));
    return SignalDetection.create("monitoringOrganization", UUID.randomUUID(),
        List.of(arrivalTime, phase), UUID.randomUUID());
  }

  public static ProcessingContext randomProcessingContext() {
    return ProcessingContext
        .createAutomatic(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            StorageVisibility.PRIVATE);
  }
}
