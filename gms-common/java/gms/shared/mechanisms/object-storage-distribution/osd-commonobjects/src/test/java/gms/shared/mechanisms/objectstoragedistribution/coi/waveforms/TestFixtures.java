package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.*;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Defines static objects used in unit tests
 */
public class TestFixtures {

  public static final Instant SEGMENT_START = Instant.parse("1970-01-02T03:04:05.123Z");
  public static final Instant SEGMENT_END = SEGMENT_START.plusMillis(2000);

  // AcquiredChannelSohBoolean
  public static final UUID SOH_BOOLEAN_ID = UUID.fromString("5f1a3629-ffaf-4190-b59d-5ca6f0646fd6");
  public static final UUID PROCESSING_CHANNEL_1_ID = UUID
      .fromString("46947cc2-8c86-4fa1-a764-c9b9944614b7");
  public static final AcquiredChannelSohBoolean channelSohBoolean = AcquiredChannelSohBoolean.from(
      SOH_BOOLEAN_ID, PROCESSING_CHANNEL_1_ID,
      AcquiredChannelSoh.AcquiredChannelSohType.DEAD_SENSOR_CHANNEL, SEGMENT_START, SEGMENT_END,
      true, CreationInfo.DEFAULT);

  // AcquiredChannelSohAnalog
  public static final UUID SOH_ANALOG_ID = UUID.fromString("b12c0b3a-4681-4ee3-82fc-4fcc292aa59f");
  public static final UUID PROCESSING_CHANNEL_2_ID = UUID
      .fromString("2bc8381f-8443-443a-83c8-cbbbe29ed796");
  public static final AcquiredChannelSohAnalog channelSohAnalog = AcquiredChannelSohAnalog.from(
      SOH_ANALOG_ID, PROCESSING_CHANNEL_2_ID,
      AcquiredChannelSoh.AcquiredChannelSohType.STATION_POWER_VOLTAGE, SEGMENT_START, SEGMENT_END,
      1.5, CreationInfo.DEFAULT);

  // Waveform
  public static final double SAMPLE_RATE = 2.0;
  public static final double[] WAVEFORM_POINTS = new double[]{1.1, 2.2, 3.3, 4.4, 5.5};
  public static final Waveform waveform1 = Waveform.withValues(SEGMENT_START, SAMPLE_RATE,
      WAVEFORM_POINTS);

  // ChannelSegment
  public static final Collection<Waveform> waveforms = Collections.singleton(waveform1);
  public static final UUID CHANNEL_SEGMENT_ID = UUID
      .fromString("57015315-f7b2-4487-b3e7-8780fbcfb413");
  public static final ChannelSegment<Waveform> channelSegment = ChannelSegment
      .from(CHANNEL_SEGMENT_ID, PROCESSING_CHANNEL_1_ID, "segmentName",
          ChannelSegment.Type.RAW, waveforms, CreationInfo.DEFAULT);

  // RawStationDataFrame
  public static final UUID FRAME_ID = UUID.fromString("12347cc2-8c86-4fa1-a764-c9b9944614b7"),
      STATION_ID = UUID.fromString("46947cc2-8c86-4fa1-a764-c9b9944614b7");
  public static final Set<UUID> CHANNEL_IDS = Set.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));
  public static final RawStationDataFrame rawStationDataFrame = RawStationDataFrame.from(
          FRAME_ID, STATION_ID, CHANNEL_IDS, AcquisitionProtocol.CD11,
          SEGMENT_START, SEGMENT_END,
      SEGMENT_END.plusSeconds(10), new byte[50],
      AuthenticationStatus.AUTHENTICATION_SUCCEEDED, CreationInfo.DEFAULT
  );

  public static final ObjectMapper objMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  /////////////////////////////////////////
  // FK
  /////////////////////////////////////////
  public static final Duration windowLead = Duration.ofMinutes(3);
  public static final Duration windowLength = Duration.ofMinutes(2);

  public static final PhaseType phaseType = PhaseType.P;

  public static final double slowStartX = 5;
  public static final double slowDeltaX = 10;
  public static final int slowCountX = 25;
  public static final double slowStartY = 5;
  public static final double slowDeltaY = 10;
  public static final int slowCountY = 25;

  public static final Duration sampleTimeStep = Duration.ofMinutes(1);
  public static double samplePeriod = sampleTimeStep.toNanos() / 1.0e9;
  public static double sampleRate = (1.0 / samplePeriod);

  public static FkSpectrum fkSpectrum = fkSpectrum(10, 1, 1);
  public static FkSpectrum fkSpectrum2 = fkSpectrum(11, 2, 2);
  public static FkSpectrum fkSpectrum3 = fkSpectrum(12, 3, 3);
  public static FkSpectrum fkSpectrum4 = fkSpectrum(13, 4, 4);
  public static FkSpectrum fkSpectrum5 = fkSpectrum(14, 5, 1);

  //collection of fkspectra sequential in time based on the sampleTimeStep
  public static FkSpectra fkSpectra = fkSpectra(SEGMENT_START, fkSpectrum);
  public static FkSpectra fkSpectra2 = fkSpectra(SEGMENT_START.plus(sampleTimeStep), fkSpectrum2);
  public static FkSpectra fkSpectra3 = fkSpectra(SEGMENT_START.plus(sampleTimeStep.multipliedBy(2)),
      fkSpectrum3);
  public static FkSpectra fkSpectra4 = fkSpectra(SEGMENT_START.plus(sampleTimeStep.multipliedBy(3)),
      fkSpectrum4);
  public static FkSpectra fkSpectra5 = fkSpectra(SEGMENT_START.plus(sampleTimeStep.multipliedBy(4)),
      fkSpectrum5);

  public static FkSpectra fkSpectra(Instant start, FkSpectrum... spectrums) {
    return FkSpectra.builder()
        .setStartTime(start)
        .setSampleRate(sampleRate)
        .withValues(List.of(spectrums))
        .setMetadata(fkMetadata())
        .build();
  }

  //TODO: fstat
  public static FkSpectrum fkSpectrum(int icoeff, int jcoeff, int quality) {
    return FkSpectrum.from(fkPower(icoeff, jcoeff), fkPower(icoeff + 1, jcoeff + 1), quality,
        List.of(fkAttributes()));
  }

  public static FkAttributes fkAttributes() {
    return FkAttributes.from(10, 10, 0.1, 0.2,
        0.5);
  }

  public static FkSpectra withoutValues(FkSpectra spectra) {
    return spectra.toBuilder().withoutValues(spectra.getSampleCount()).build();
  }

  /**
   * Generates fk power data based off of the input coefficients. Power = (i * icoeff) + (j +
   * jcoeff)
   */
  public static double[][] fkPower(int icoeff, int jcoeff) {
    double[][] fkData = new double[slowCountY][slowCountX];

    for (int i = 0; i < slowCountY; i++) {
      for (int j = 0; j < slowCountX; j++) {
        if (j == 0) {
          fkData[i][j] = 0.0;
        } else {
          fkData[i][j] = (i * icoeff) + (j + jcoeff);
        }
      }
    }

    return fkData;
  }

  public static FkSpectra.Metadata fkMetadata() {
    return FkSpectra.Metadata.builder()
        .setPhaseType(phaseType)
        .setSlowStartX(slowStartX)
        .setSlowDeltaX(slowDeltaX)
        .setSlowStartY(slowStartY)
        .setSlowDeltaY(slowDeltaY)
        .build();
  }

  public static List<ChannelSegment<FkSpectra>> createFkChannelSegments(
      FkSpectra... fks) {
    return Arrays.stream(fks).map(fk ->
        ChannelSegment.create(
            UUID.fromString("5f1a3629-ffaf-4190-b59d-5ca6f0646fd8"),
            "Test",
            ChannelSegment.Type.FK_BEAM,
            List.of(fk),
            CreationInfo.DEFAULT
        )).collect(Collectors.toList());
  }
}
