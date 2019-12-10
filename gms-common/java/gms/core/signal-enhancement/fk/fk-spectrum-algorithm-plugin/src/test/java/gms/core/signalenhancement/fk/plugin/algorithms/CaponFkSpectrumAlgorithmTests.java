package gms.core.signalenhancement.fk.plugin.algorithms;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.Type;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Immutable2dDoubleArray;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CaponFkSpectrumAlgorithmTests {

  private static final double[] TEST_FREQ_BINS_EVEN = new double[] {0, 1, 2, 3, 4, -5, -4, -3, -2, -1};
  private static final double[] TEST_FREQ_BINS_ODD = new double[] {0, 1, 2, 3, 4, -4, -3, -2, -1};

  private static final double WF_SAMPLE_RATE = 40;
  private static final long WF_SAMPLE_COUNT = 10;

  private static final double LOW_FREQ = 0;
  private static final double HIGH_FREQ = 20;

  private static final double FK_SAMPLE_RATE = 1.0;

  private static final double MULTIPLE_FK_LOW_FREQUENCY = 1.0;
  private static final double MULTIPLE_FK_HIGH_FREQUENCY = 4.0;

  private static final double EAST_SLOW_START = -0.36;
  private static final double EAST_SLOW_DELTA = 0.009;
  private static final int EAST_SLOW_COUNT = 81;

  private static final double NORTH_SLOW_START = -0.36;
  private static final double NORTH_SLOW_DELTA = 0.009;
  private static final int NORTH_SLOW_COUNT = 81;

  private static final Duration WINDOW_LEAD = Duration.ofSeconds(1);
  private static final Duration WINDOW_LENGTH = Duration.ofSeconds(2);

  private static final int MIN_WAVEFORMS = 2;

  private static final double MAX_UNCERTAINTY = .00000001;

  private static final List<Waveform> WAVEFORMS = Arrays.asList(
      Waveform.from(Instant.EPOCH, 40, WF_SAMPLE_COUNT, new double[] {
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 10.0, 0.0, 0.0, 0.0
      }),
      Waveform.from(Instant.EPOCH, 40, WF_SAMPLE_COUNT, new double[] {
          0.0, 0.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
      }),
      Waveform.from(Instant.EPOCH, 40, WF_SAMPLE_COUNT, new double[] {
          0.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
      })
  );

  private static List<double[]> baseWaveforms;
  private static double[] optionalWaveform;
  private static List<RelativePosition> relativePositions = new ArrayList<>();
  private static RelativePosition optionalPosition;
  private static List<FkSpectrum> baseFks;
  private static List<FkSpectrum> optionalFks;
  private static List<FkSpectrum> normalizedFk;

  @BeforeAll
  public static void setup() throws IOException {
    ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();
    TypeFactory factory = mapper.getTypeFactory();
    JavaType mapType = factory.constructMapType(HashMap.class, String.class, Object.class);
    Map<String, Object> validationData =
        mapper.readValue(CaponFkSpectrumAlgorithmTests.class.getClassLoader().getResourceAsStream("multipleFkData.json"), mapType);

    Map<String, Object> fkInputs = mapper.convertValue(validationData.get("input"), mapType);
    baseWaveforms = mapper.convertValue(fkInputs.get("waveforms"),
        factory.constructCollectionType(List.class, double[].class));
    optionalWaveform = mapper.convertValue(fkInputs.get("optionalWaveform"), double[].class);

    List<double[]> inputPositions = mapper.convertValue(fkInputs.get("relativePositions"),
        factory.constructCollectionType(List.class, double[].class));
    for (double[] position : inputPositions) {
      RelativePosition relativePosition = RelativePosition.from(position[0], position[1], 0);
      relativePositions.add(relativePosition);
    }

    double[] optionalInputPosition = mapper.convertValue(fkInputs.get("optionalPosition"),
        double[].class);
    optionalPosition = RelativePosition.from(optionalInputPosition[0], optionalInputPosition[1], 0);

    JavaType fkListType = factory.constructCollectionType(List.class, FkSpectrum.class);
    baseFks = mapper.convertValue(validationData.get("4WaveformFks"), fkListType);
    optionalFks = mapper.convertValue(validationData.get("5WaveformFks"), fkListType);
    normalizedFk = mapper.convertValue(validationData.get("normalizedFks"), fkListType);
  }

  @Test
  public void testBuildValidation() {
    Location origin = Location.from(0.0, 0.0, 0.0, 0.0);

    Map<UUID, RelativePosition> relativePositionMap = Map.of(
        UUID.randomUUID(),
        RelativePosition.from(0.0, 0.0, 0.0),
        UUID.randomUUID(),
        RelativePosition.from(1.0, 1.0, 1.0),
        UUID.randomUUID(),
        RelativePosition.from(2.0, 2.0, 2.0));

    CaponFkSpectrumAlgorithm baseAlgorithm = new CaponFkSpectrumAlgorithm.Builder()
        .withSampleRate(WF_SAMPLE_RATE)
        .withLowFrequency(LOW_FREQ)
        .withHighFrequency(HIGH_FREQ)
        .withWindowLead(Duration.ZERO)
        .withWindowLength(Duration.ofSeconds(1))
        .withMediumVelocityKmPerSec(2.5)
        .withRelativePositionMap(relativePositionMap)
        .withWaveformSampleRateHz(WF_SAMPLE_RATE)
        .withEastSlowStart(1.0)
        .withEastSlowDelta(0.1)
        .withEastSlowCount(20)
        .withNorthSlowStart(1.0)
        .withNorthSlowDelta(0.1)
        .withNorthSlowCount(20)
        .withMinimumWaveformsForSpectra(2)
        .build();

    CaponFkSpectrumAlgorithm.Builder nullRelativePositions = baseAlgorithm.toBuilder()
        .withRelativePositionMap(null);

    CaponFkSpectrumAlgorithm.Builder emptyRelativePositions = baseAlgorithm.toBuilder()
        .withRelativePositionMap(Collections.emptyMap());

    CaponFkSpectrumAlgorithm.Builder nullWindowLead = baseAlgorithm.toBuilder()
        .withWindowLead(null);

    CaponFkSpectrumAlgorithm.Builder nullWindowLength = baseAlgorithm.toBuilder()
        .withWindowLength(null);

    CaponFkSpectrumAlgorithm.Builder shortWindowLength = baseAlgorithm.toBuilder()
        .withWindowLead(Duration.ofSeconds(5))
        .withWindowLength(Duration.ofSeconds(2));

    CaponFkSpectrumAlgorithm.Builder negativeWindowLength = baseAlgorithm.toBuilder()
        .withWindowLead(Duration.ZERO)
        .withWindowLength(Duration.ofSeconds(-5));

    CaponFkSpectrumAlgorithm.Builder negativeWindowLead = baseAlgorithm.toBuilder()
        .withWindowLead(Duration.ofSeconds(-5))
        .withWindowLength(Duration.ofSeconds(10));

    CaponFkSpectrumAlgorithm.Builder negativeLowFrequency = baseAlgorithm.toBuilder()
        .withLowFrequency(-1.0);

    CaponFkSpectrumAlgorithm.Builder lowHighFrequency = baseAlgorithm.toBuilder()
        .withLowFrequency(10.0)
        .withHighFrequency(0.0);

    CaponFkSpectrumAlgorithm.Builder tooHighFrequency = baseAlgorithm.toBuilder()
        .withHighFrequency(21.0);

    assertAll("builder",
        () -> assertThrows(NullPointerException.class, nullRelativePositions::build),
        () -> assertThrows(IllegalArgumentException.class, emptyRelativePositions::build),
        () -> assertThrows(NullPointerException.class, nullWindowLead::build),
        () -> assertThrows(NullPointerException.class, nullWindowLength::build),
        () -> assertThrows(IllegalArgumentException.class, shortWindowLength::build),
        () -> assertThrows(IllegalArgumentException.class, negativeWindowLength::build),
        () -> assertThrows(IllegalArgumentException.class, negativeWindowLead::build),
        () -> assertThrows(IllegalArgumentException.class, negativeLowFrequency::build),
        () -> assertThrows(IllegalArgumentException.class, lowHighFrequency::build),
        () -> assertThrows(IllegalArgumentException.class, tooHighFrequency::build));
  }

  @Test
  public void testGenerateFkValidation() {
    Map<UUID, RelativePosition> relativePositionsByChannelId = Map.of(
        UUID.fromString("b5cd3cf8-633f-4700-943f-38dd454ccdfa"),
        RelativePosition.from(0.0, 0.0, 0.0),
        UUID.fromString("3d89e386-ebae-4ec9-8575-c87c57beda4e"),
        RelativePosition.from(1.0, 1.0, 1.0),
        UUID.fromString("a1ed3b46-2527-41c7-96ce-191c41a42765"),
        RelativePosition.from(2.0, 2.0, 2.0),
        UUID.fromString("1b8ed174-27cd-4fd8-92e8-7c75c46887a0"),
        RelativePosition.from(3.0, 3.0, 3.0),
        UUID.fromString("a8dabab5-9686-424c-ba3c-49d75cb3100d"),
        RelativePosition.from(4.0, 4.0, 4.0),
        UUID.fromString("b751187f-5c51-41df-97ad-58fa60cc1c69"),
        RelativePosition.from(5.0, 5.0, 5.0),
        UUID.fromString("edec194d-5cee-4022-829c-f8b7cbc8762a"),
        RelativePosition.from(6.0, 6.0, 6.0));

    List<UUID> availableChannelIds = new ArrayList<>(relativePositionsByChannelId.keySet());

    CaponFkSpectrumAlgorithm algorithm = new CaponFkSpectrumAlgorithm.Builder()
        .useChannelVerticalOffsets(false)
        .normalizeWaveforms(false)
        .withRelativePositionMap(relativePositionsByChannelId)
        .withWaveformSampleRateHz(40)
        .withMediumVelocityKmPerSec(3.7)
        .withLowFrequency(0)
        .withHighFrequency(20)
        .withEastSlowCount(6)
        .withEastSlowStart(1.5)
        .withEastSlowDelta(.3)
        .withNorthSlowCount(6)
        .withNorthSlowStart(1.5)
        .withNorthSlowDelta(.3)
        .withWindowLead(Duration.ZERO)
        .withWindowLength(Duration.ofSeconds(10))
        .withSampleRate(40)
        .withMinimumWaveformsForSpectra(2)
        .build();

    List<ChannelSegment<Waveform>> baseChannelSegments = new ArrayList<>();
    int i = 0;
    for (; i < WAVEFORMS.size() && i < availableChannelIds.size(); i++) {
      baseChannelSegments.add(ChannelSegment.create(availableChannelIds.get(i),
          "Test basic channel segment " + i,
          ChannelSegment.Type.RAW,
          List.of(WAVEFORMS.get(i)),
          CreationInfo.DEFAULT));
    }

    List<ChannelSegment<Waveform>> duplicateChannelSegments = new ArrayList<>(baseChannelSegments);
    duplicateChannelSegments.addAll(baseChannelSegments);

    ChannelSegment<Waveform> mismatchedChannelSegment = ChannelSegment.create(
        UUID.fromString("58cd1ecf-b247-46db-bc79-67786b2977f8"),
        "Test channel segment",
        ChannelSegment.Type.RAW,
        List.of(WAVEFORMS.get(0)),
        CreationInfo.DEFAULT);

    Waveform offsetWaveform = Waveform.from(Instant.EPOCH.plusSeconds(1), 40, WF_SAMPLE_COUNT,
        new double[] {
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 10.0, 0.0, 0.0, 0.0
        });

    ChannelSegment<Waveform> offsetChannelSegment = ChannelSegment.create(
        availableChannelIds.get(i++),
        "Test late start channel segment",
        ChannelSegment.Type.RAW,
        List.of(offsetWaveform),
        CreationInfo.DEFAULT);

    List<ChannelSegment<Waveform>> multipleStartTimesSegments =
        new ArrayList<>(baseChannelSegments);
    multipleStartTimesSegments.add(offsetChannelSegment);

    Waveform longWaveform = Waveform.from(Instant.EPOCH, 40, WF_SAMPLE_COUNT + 1,
        new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 10.0, 0.0, 0.0, 0.0, 0.0});

    ChannelSegment<Waveform> longChannelSegment = ChannelSegment.create(
        availableChannelIds.get(i++),
        "Test late end channel segment",
        ChannelSegment.Type.RAW,
        List.of(longWaveform),
        CreationInfo.DEFAULT);

    List<ChannelSegment<Waveform>> multipleEndTimesSegments = new ArrayList<>(baseChannelSegments);
    multipleEndTimesSegments.add(longChannelSegment);

    Waveform firstSegmentWaveform = Waveform.withValues(Instant.EPOCH,
        40,
        new double[] {0.0, 0.0, 0.0, 0.0, 0.0});

    Waveform secondSegmentWaveform =
        Waveform.withValues(firstSegmentWaveform.getEndTime().plusMillis(50),
            40,
            new double[] {0.0, 0.0, 0.0, 0.0});

    ChannelSegment<Waveform> segmentedChannelSegment = ChannelSegment.create(
        availableChannelIds.get(i++),
        "Test segmented channel segment",
        ChannelSegment.Type.RAW,
        List.of(firstSegmentWaveform, secondSegmentWaveform),
        CreationInfo.DEFAULT);

    Waveform slowWaveform = Waveform.withValues(Instant.EPOCH,
        20,
        new double[] {0.0, 0.0, 1.0, 0.0, 0.0});

    ChannelSegment<Waveform> slowChannelSegment = ChannelSegment.create(
        availableChannelIds.get(i),
        "Test slow channel segment",
        ChannelSegment.Type.RAW,
        List.of(slowWaveform),
        CreationInfo.DEFAULT);

    List<ChannelSegment<Waveform>> mixedSampleSizeChannelSegments =
        new ArrayList<>(baseChannelSegments);
    mixedSampleSizeChannelSegments.add(slowChannelSegment);

    assertAll("GenerateFk",
        () -> assertThrows(NullPointerException.class, () -> algorithm.generateFk(null)),
        () -> assertThrows(IllegalArgumentException.class,
            () -> algorithm.generateFk(Collections.emptyList())),
        () -> assertThrows(IllegalArgumentException.class,
            () -> algorithm.generateFk(duplicateChannelSegments)),
        () -> assertThrows(IllegalArgumentException.class,
            () -> algorithm.generateFk(List.of(mismatchedChannelSegment))),
        () -> assertThrows(IllegalArgumentException.class,
            () -> algorithm.generateFk(multipleStartTimesSegments)),
        () -> assertThrows(IllegalArgumentException.class,
            () -> algorithm.generateFk(multipleEndTimesSegments)),
        () -> assertThrows(IllegalArgumentException.class,
            () -> algorithm.generateFk(List.of(segmentedChannelSegment))),
        () -> assertThrows(IllegalArgumentException.class,
            () -> algorithm.generateFk(mixedSampleSizeChannelSegments)),
        () -> assertThrows(IllegalArgumentException.class,
            () -> algorithm.generateFk(baseChannelSegments)));
  }

  @Test
  void testFftFreq() {
    int oddSampleCount = 9;
    int evenSampleCount = 10;
    int deltaFrequency = 1;

    double[] evenFreqBins = CaponFkSpectrumAlgorithm.fftFreq(evenSampleCount, deltaFrequency);
    assertArrayEquals(TEST_FREQ_BINS_EVEN, evenFreqBins);

    double[] oddFreqBins = CaponFkSpectrumAlgorithm.fftFreq(oddSampleCount, deltaFrequency);
    assertArrayEquals(TEST_FREQ_BINS_ODD, oddFreqBins);
  }

  @Test
  void testFindFreqBinIndices() {
    //Overlapping bins
    double lowFrequency = 2;
    double highFrequency = 5;

    int[] expectedEvenIndices = new int[] {2, 3, 4, 5, 6, 7, 8};
    int[] evenFreqBinIndices = CaponFkSpectrumAlgorithm.findBinIndices(
        TEST_FREQ_BINS_EVEN, lowFrequency, highFrequency);

    assertArrayEquals(expectedEvenIndices, evenFreqBinIndices);

    int[] expectedOddIndices = new int[] {2, 3, 4, 5, 6, 7};
    int[] oddFreqBinIndices = CaponFkSpectrumAlgorithm.findBinIndices(
        TEST_FREQ_BINS_ODD, lowFrequency, highFrequency);

    assertArrayEquals(expectedOddIndices, oddFreqBinIndices);

    //Non-overlapping bins
    lowFrequency = 1;
    highFrequency = 3;

    expectedEvenIndices = new int[] {1, 2, 3, 7, 8, 9};
    evenFreqBinIndices = CaponFkSpectrumAlgorithm.findBinIndices(
        TEST_FREQ_BINS_EVEN, lowFrequency, highFrequency);

    assertArrayEquals(expectedEvenIndices, evenFreqBinIndices);

    expectedOddIndices = new int[] {1, 2, 3, 6, 7, 8};
    oddFreqBinIndices = CaponFkSpectrumAlgorithm.findBinIndices(
        TEST_FREQ_BINS_ODD, lowFrequency, highFrequency);

    assertArrayEquals(expectedOddIndices, oddFreqBinIndices);
  }

  @Test
  public void testGenerateSingleFk() {
    List<ChannelSegment<Waveform>> channelSegments = new ArrayList<>();
    Map<UUID, RelativePosition> relativePositionMap = new HashMap<>();

    for (int i = 0; i < baseWaveforms.size() && i < relativePositions.size(); i++) {
      double[] data = baseWaveforms.get(i);
      // split into the first waveform set
      double[] sizedData = new double[81];
      System.arraycopy(data, 0, sizedData, 0, 81);
      Waveform waveform = Waveform.withValues(Instant.EPOCH, 40.0, sizedData);

      UUID uuid = UUID.randomUUID();
      channelSegments.add(ChannelSegment.from(UUID.randomUUID(),
          uuid,
          "channel" + i,
          Type.RAW,
          List.of(waveform),
          CreationInfo.DEFAULT));

      relativePositionMap.put(uuid, relativePositions.get(i));
    }

    CaponFkSpectrumAlgorithm algorithm = new CaponFkSpectrumAlgorithm.Builder()
        .useChannelVerticalOffsets(false)
        .normalizeWaveforms(false)
        .withLowFrequency(MULTIPLE_FK_LOW_FREQUENCY)
        .withHighFrequency(MULTIPLE_FK_HIGH_FREQUENCY)
        .withWaveformSampleRateHz(WF_SAMPLE_RATE)
        .withSampleRate(FK_SAMPLE_RATE)
        .withEastSlowStart(EAST_SLOW_START)
        .withEastSlowDelta(EAST_SLOW_DELTA)
        .withEastSlowCount(EAST_SLOW_COUNT)
        .withNorthSlowStart(NORTH_SLOW_START)
        .withNorthSlowDelta(NORTH_SLOW_DELTA)
        .withNorthSlowCount(NORTH_SLOW_COUNT)
        .withWindowLead(WINDOW_LEAD)
        .withWindowLength(WINDOW_LENGTH)
        .withRelativePositionMap(relativePositionMap)
        .withMinimumWaveformsForSpectra(MIN_WAVEFORMS)
        .build();

    Optional<FkSpectrum> possibleSpectrum = algorithm.generateSingleFk(channelSegments,
        relativePositions,
        Instant.EPOCH,
        Instant.EPOCH);

    assertTrue(possibleSpectrum.isPresent());

    FkSpectrum expectedSpectrum = baseFks.get(0);
    FkSpectrum actualSpectrum = possibleSpectrum.get();

    compareArrays(expectedSpectrum.getPower(), actualSpectrum.getPower());
    compareArrays(expectedSpectrum.getFstat(), actualSpectrum.getFstat());
    assertEquals(expectedSpectrum.getQuality(), actualSpectrum.getQuality());
  }

  @Test
  public void testGenerateNormalizedSingleFk() {
    List<ChannelSegment<Waveform>> channelSegments = new ArrayList<>();
    Map<UUID, RelativePosition> relativePositionMap = new HashMap<>();
    for (int i = 0; i < baseWaveforms.size() && i < relativePositions.size(); i++) {
      double[] data = baseWaveforms.get(i);
      Waveform waveform = Waveform.withValues(Instant.EPOCH, 40.0, data);

      UUID uuid = UUID.randomUUID();
      channelSegments.add(ChannelSegment.from(UUID.randomUUID(),
          uuid,
          "channel" + i,
          Type.RAW,
          List.of(waveform),
          CreationInfo.DEFAULT));

      relativePositionMap.put(uuid, relativePositions.get(i));
    }

    CaponFkSpectrumAlgorithm algorithm = new CaponFkSpectrumAlgorithm.Builder()
        .useChannelVerticalOffsets(false)
        .normalizeWaveforms(true)
        .withLowFrequency(MULTIPLE_FK_LOW_FREQUENCY)
        .withHighFrequency(MULTIPLE_FK_HIGH_FREQUENCY)
        .withWaveformSampleRateHz(WF_SAMPLE_RATE)
        .withSampleRate(FK_SAMPLE_RATE)
        .withEastSlowStart(EAST_SLOW_START)
        .withEastSlowDelta(EAST_SLOW_DELTA)
        .withEastSlowCount(EAST_SLOW_COUNT)
        .withNorthSlowStart(NORTH_SLOW_START)
        .withNorthSlowDelta(NORTH_SLOW_DELTA)
        .withNorthSlowCount(NORTH_SLOW_COUNT)
        .withWindowLead(WINDOW_LEAD)
        .withWindowLength(WINDOW_LENGTH)
        .withRelativePositionMap(relativePositionMap)
        .withMinimumWaveformsForSpectra(MIN_WAVEFORMS)
        .build();

    List<FkSpectrum> fkSpectrumList = algorithm.generateFk(channelSegments);
    assertEquals(normalizedFk.size(), fkSpectrumList.size());

    for (int i = 0; i < normalizedFk.size(); i++) {
      FkSpectrum expected = normalizedFk.get(i);
      FkSpectrum actual = fkSpectrumList.get(i);

      compareArrays(expected.getPower(), actual.getPower());
      compareArrays(expected.getFstat(), actual.getFstat());
      assertEquals(expected.getQuality(), actual.getQuality());
    }
  }

  @Test
  public void testGenerateMultipleFks() {
    List<ChannelSegment<Waveform>> channelSegments = new ArrayList<>();
    Map<UUID, RelativePosition> relativePositionMap = new HashMap<>();
    for (int i = 0; i < baseWaveforms.size() && i < relativePositions.size(); i++) {
      double[] data = baseWaveforms.get(i);
      Waveform waveform = Waveform.withValues(Instant.EPOCH, 40.0, data);

      UUID uuid = UUID.randomUUID();
      channelSegments.add(ChannelSegment.from(UUID.randomUUID(),
          uuid,
          "channel" + i,
          Type.RAW,
          List.of(waveform),
          CreationInfo.DEFAULT));

      relativePositionMap.put(uuid, relativePositions.get(i));
    }

    CaponFkSpectrumAlgorithm algorithm = new CaponFkSpectrumAlgorithm.Builder()
        .useChannelVerticalOffsets(false)
        .normalizeWaveforms(false)
        .withLowFrequency(MULTIPLE_FK_LOW_FREQUENCY)
        .withHighFrequency(MULTIPLE_FK_HIGH_FREQUENCY)
        .withWaveformSampleRateHz(WF_SAMPLE_RATE)
        .withSampleRate(FK_SAMPLE_RATE)
        .withEastSlowStart(EAST_SLOW_START)
        .withEastSlowDelta(EAST_SLOW_DELTA)
        .withEastSlowCount(EAST_SLOW_COUNT)
        .withNorthSlowStart(NORTH_SLOW_START)
        .withNorthSlowDelta(NORTH_SLOW_DELTA)
        .withNorthSlowCount(NORTH_SLOW_COUNT)
        .withWindowLead(WINDOW_LEAD)
        .withWindowLength(WINDOW_LENGTH)
        .withRelativePositionMap(relativePositionMap)
        .withMinimumWaveformsForSpectra(MIN_WAVEFORMS)
        .build();

    List<FkSpectrum> fkSpectrumList = algorithm.generateFk(channelSegments);
    assertEquals(baseFks.size(), fkSpectrumList.size());

    for (int i = 0; i < baseFks.size(); i++) {
      FkSpectrum expected = baseFks.get(i);
      FkSpectrum actual = fkSpectrumList.get(i);

      compareArrays(expected.getPower(), actual.getPower());
      compareArrays(expected.getFstat(), actual.getFstat());
      assertEquals(expected.getQuality(), actual.getQuality());
    }
  }

  @Test
  public void testMultipleFkFromWaveformWithGaps() {
    List<ChannelSegment<Waveform>> channelSegments = new ArrayList<>();
    Map<UUID, RelativePosition> relativePositionMap = new HashMap<>();
    for (int i = 0; i < baseWaveforms.size() && i < relativePositions.size(); i++) {
      double[] data = baseWaveforms.get(i);
      Waveform waveform = Waveform.withValues(Instant.EPOCH, 40.0, data);

      UUID uuid = UUID.randomUUID();
      channelSegments.add(ChannelSegment.from(UUID.randomUUID(),
          uuid,
          "channel" + i,
          Type.RAW,
          List.of(waveform),
          CreationInfo.DEFAULT));

      relativePositionMap.put(uuid, relativePositions.get(i));
    }

    Waveform optionalBase = Waveform.withValues(Instant.EPOCH, 40, optionalWaveform);

    Waveform firstOptional = optionalBase.window(Instant.EPOCH, Instant.EPOCH.plusMillis(2500));
    Waveform secondOptional = optionalBase.window(Instant.EPOCH.plusMillis(2600),
        optionalBase.getEndTime());

    UUID uuid = UUID.randomUUID();
    channelSegments.add(ChannelSegment.from(UUID.randomUUID(),
        uuid,
        "optional channel",
        Type.RAW,
        List.of(firstOptional, secondOptional),
        CreationInfo.DEFAULT));

    relativePositionMap.put(uuid, optionalPosition);

    CaponFkSpectrumAlgorithm algorithm = new CaponFkSpectrumAlgorithm.Builder()
        .useChannelVerticalOffsets(false)
        .normalizeWaveforms(false)
        .withLowFrequency(MULTIPLE_FK_LOW_FREQUENCY)
        .withHighFrequency(MULTIPLE_FK_HIGH_FREQUENCY)
        .withWaveformSampleRateHz(WF_SAMPLE_RATE)
        .withSampleRate(FK_SAMPLE_RATE)
        .withEastSlowStart(EAST_SLOW_START)
        .withEastSlowDelta(EAST_SLOW_DELTA)
        .withEastSlowCount(EAST_SLOW_COUNT)
        .withNorthSlowStart(NORTH_SLOW_START)
        .withNorthSlowDelta(NORTH_SLOW_DELTA)
        .withNorthSlowCount(NORTH_SLOW_COUNT)
        .withWindowLead(WINDOW_LEAD)
        .withWindowLength(WINDOW_LENGTH)
        .withRelativePositionMap(relativePositionMap)
        .withMinimumWaveformsForSpectra(MIN_WAVEFORMS)
        .build();

    List<FkSpectrum> spectrums = algorithm.generateFk(channelSegments);

    assertEquals(optionalFks.size(), spectrums.size());

    for (int i = 0; i < optionalFks.size(); i++) {
      FkSpectrum expected;
      FkSpectrum actual = spectrums.get(i);
      if (i == 0) {
        expected = optionalFks.get(i);
      } else {
        expected = baseFks.get(i);
      }

      compareArrays(expected.getPower(), actual.getPower());
      compareArrays(expected.getFstat(), actual.getFstat());
      assertEquals(expected.getQuality(), actual.getQuality());
    }
  }

  @Test
  public void testMultipleFkFromWaveformWithJitter() {
    List<ChannelSegment<Waveform>> channelSegments = new ArrayList<>();
    Map<UUID, RelativePosition> relativePositionMap = new HashMap<>();
    for (int i = 0; i < baseWaveforms.size() && i < relativePositions.size(); i++) {
      double[] data = baseWaveforms.get(i);
      Waveform waveform = Waveform.withValues(Instant.EPOCH, 40.0, data);

      UUID uuid = UUID.randomUUID();
      channelSegments.add(ChannelSegment.from(UUID.randomUUID(),
          uuid,
          "channel" + i,
          Type.RAW,
          List.of(waveform),
          CreationInfo.DEFAULT));

      relativePositionMap.put(uuid, relativePositions.get(i));
    }

    Duration samplePeriod = Duration.ofNanos((long) ((1 / 40) * 1E9));
    Duration jitter = samplePeriod.dividedBy(2).minusNanos(10);

    Waveform optional = Waveform.withValues(Instant.EPOCH.plus(jitter), 40, optionalWaveform);

    UUID uuid = UUID.randomUUID();
    channelSegments.add(ChannelSegment.from(UUID.randomUUID(),
        uuid,
        "optional channel",
        Type.RAW,
        List.of(optional),
        CreationInfo.DEFAULT));

    relativePositionMap.put(uuid, optionalPosition);

    CaponFkSpectrumAlgorithm algorithm = new CaponFkSpectrumAlgorithm.Builder()
        .useChannelVerticalOffsets(false)
        .normalizeWaveforms(false)
        .withLowFrequency(MULTIPLE_FK_LOW_FREQUENCY)
        .withHighFrequency(MULTIPLE_FK_HIGH_FREQUENCY)
        .withWaveformSampleRateHz(WF_SAMPLE_RATE)
        .withSampleRate(FK_SAMPLE_RATE)
        .withEastSlowStart(EAST_SLOW_START)
        .withEastSlowDelta(EAST_SLOW_DELTA)
        .withEastSlowCount(EAST_SLOW_COUNT)
        .withNorthSlowStart(NORTH_SLOW_START)
        .withNorthSlowDelta(NORTH_SLOW_DELTA)
        .withNorthSlowCount(NORTH_SLOW_COUNT)
        .withWindowLead(WINDOW_LEAD)
        .withWindowLength(WINDOW_LENGTH)
        .withRelativePositionMap(relativePositionMap)
        .withMinimumWaveformsForSpectra(MIN_WAVEFORMS)
        .build();

    List<FkSpectrum> spectrumList = algorithm.generateFk(channelSegments);

    assertEquals(optionalFks.size(), spectrumList.size());
    for (int i = 0; i < optionalFks.size(); i++) {
      FkSpectrum expected = optionalFks.get(i);
      FkSpectrum actual = spectrumList.get(i);

      compareArrays(expected.getPower(), actual.getPower());
      compareArrays(expected.getFstat(), actual.getFstat());
      assertEquals(expected.getQuality(), actual.getQuality());
    }
  }

  @Test
  public void testMultipleFkFromWaveformOutOfSampleRateTolerance() {
    List<ChannelSegment<Waveform>> channelSegments = new ArrayList<>();
    Map<UUID, RelativePosition> relativePositionMap = new HashMap<>();
    for (int i = 0; i < baseWaveforms.size() && i < relativePositions.size(); i++) {
      double[] data = baseWaveforms.get(i);
      Waveform waveform = Waveform.withValues(Instant.EPOCH, 40.0, data);

      UUID uuid = UUID.randomUUID();
      channelSegments.add(ChannelSegment.from(UUID.randomUUID(),
          uuid,
          "channel" + i,
          Type.RAW,
          List.of(waveform),
          CreationInfo.DEFAULT));

      relativePositionMap.put(uuid, relativePositions.get(i));
    }

    Waveform optional = Waveform.withValues(Instant.EPOCH, 39, optionalWaveform);

    UUID uuid = UUID.randomUUID();
    channelSegments.add(ChannelSegment.from(UUID.randomUUID(),
        uuid,
        "optional channeL",
        Type.RAW,
        List.of(optional),
        CreationInfo.DEFAULT));

    relativePositionMap.put(uuid, optionalPosition);

    CaponFkSpectrumAlgorithm algorithm = new CaponFkSpectrumAlgorithm.Builder()
        .useChannelVerticalOffsets(false)
        .normalizeWaveforms(false)
        .withLowFrequency(MULTIPLE_FK_LOW_FREQUENCY)
        .withHighFrequency(MULTIPLE_FK_HIGH_FREQUENCY)
        .withWaveformSampleRateHz(WF_SAMPLE_RATE)
        .withSampleRate(FK_SAMPLE_RATE)
        .withEastSlowStart(EAST_SLOW_START)
        .withEastSlowDelta(EAST_SLOW_DELTA)
        .withEastSlowCount(EAST_SLOW_COUNT)
        .withNorthSlowStart(NORTH_SLOW_START)
        .withNorthSlowDelta(NORTH_SLOW_DELTA)
        .withNorthSlowCount(NORTH_SLOW_COUNT)
        .withWindowLead(WINDOW_LEAD)
        .withWindowLength(WINDOW_LENGTH)
        .withRelativePositionMap(relativePositionMap)
        .withMinimumWaveformsForSpectra(MIN_WAVEFORMS)
        .build();

    List<FkSpectrum> spectrumList = algorithm.generateFk(channelSegments);

    assertEquals(baseFks.size(), spectrumList.size());

    for (int i = 0; i < baseFks.size(); i++) {
      FkSpectrum expected = baseFks.get(i);
      FkSpectrum actual = spectrumList.get(i);

      compareArrays(expected.getPower(), actual.getPower());
      compareArrays(expected.getFstat(), actual.getFstat());
      assertEquals(expected.getQuality(), actual.getQuality());
    }
  }

  private void compareArrays(Immutable2dDoubleArray expected, Immutable2dDoubleArray actual) {
    assertEquals(expected.rowCount(), actual.rowCount());
    assertEquals(expected.columnCount(), actual.columnCount());

    for (int i = 0; i < expected.rowCount(); i++) {
      for (int j = 0; j < expected.columnCount(); j++) {
        assertEquals(expected.getValue(i, j), actual.getValue(i, j), MAX_UNCERTAINTY);
      }
    }
  }

}
