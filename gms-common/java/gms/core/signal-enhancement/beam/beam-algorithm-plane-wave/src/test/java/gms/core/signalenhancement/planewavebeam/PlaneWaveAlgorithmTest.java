package gms.core.signalenhancement.planewavebeam;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import gms.core.signalenhancement.planewavebeam.util.BeamValidationData;
import gms.core.signalenhancement.planewavebeam.util.TestFixtures;
import gms.core.signalenhancement.planewavebeam.util.ValidationBeam;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlaneWaveAlgorithmTest {

  private void testBeam(BeamValidationData data) {
    PlaneWaveAlgorithm algorithm = new PlaneWaveAlgorithm.Builder()
        .withNominalSampleRate(data.getNominalSampleRate())
        .withSampleRateTolerance(data.getSampleRateTolerance())
        .withAzimuth(data.getAzimuth())
        .withHorizontalSlowness(data.getHorizontalSlowness())
        .withMediumVelocity(data.getMediumVelocity())
        .withSnappedSampling(data.isSnappedSampling())
        .withCoherence(data.isCoherent())
        .withDimensionality(data.isTwoDimensional())
        .withPhaseType(data.getPhaseType())
        .withRelativePositions(data.getRelativePositionsByChannelId())
        .withMinimumWaveformsForBeam(1)
        .build();

    List<Waveform> beams = algorithm.generateBeam(data.getChannelSegments());
    assertNotNull(beams);
    assertEquals(1, beams.size());

    Waveform beam = beams.get(0);

    assertNotNull(beam);
    assertArrayEquals(data.getBeam().getValues(),
        beam.getValues(),
        0.01);
    assertEquals(data.getStartTime(), beam.getStartTime());
  }

  @Test
  public void testGappedInput() throws IOException {
    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    TypeFactory typeFactory = objectMapper.getTypeFactory();
    JavaType channelSegmentType = typeFactory.constructParametricType(ChannelSegment.class, Waveform.class);
    JavaType channelSegmentListType = typeFactory.constructCollectionType(List.class, channelSegmentType);

    List<ChannelSegment<Waveform>> channelSegments = objectMapper.readValue(
        getClass().getClassLoader().getResourceAsStream("channelSegments.json"),
        channelSegmentListType);

    double slowness = 0.2857142857142857;
    double horizontalSlowness = slowness * Math.sin(Math.toRadians(15.0));

    Map<UUID, RelativePosition> relativePositions = new HashMap<>();

    JavaType mapType = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
    Map<String, Object> geomData = objectMapper.readValue(
        getClass().getClassLoader().getResourceAsStream("ASARgeom.json"),
        mapType);

    JavaType coordinateMapType = typeFactory.constructMapType(HashMap.class, String.class, double[].class);
    JavaType positionMapType = typeFactory.constructMapType(HashMap.class, typeFactory.constructType(String.class), coordinateMapType);
    Map<String, Map<String, double[]>> geometry = objectMapper.convertValue(geomData.get("geometry"), positionMapType);

    for (ChannelSegment channelSegment : channelSegments) {
      Map<String, double[]> coordinates = geometry.get(channelSegment.getName());
      double[] cartesian = coordinates.get("XYZ");
      relativePositions.put(channelSegment.getChannelId(),
          RelativePosition.from(cartesian[1], cartesian[0], cartesian[2]));
    }

    PlaneWaveAlgorithm algorithm = new PlaneWaveAlgorithm.Builder()
        .withNominalSampleRate(40.0)
        .withSampleRateTolerance(0.001)
        .withAzimuth(200)
        .withHorizontalSlowness(horizontalSlowness)
        .withMediumVelocity(1 / slowness)
        .withSnappedSampling(true)
        .withCoherence(true)
        .withDimensionality(false)
        .withPhaseType(PhaseType.P)
        .withRelativePositions(relativePositions)
        .withMinimumWaveformsForBeam(1)
        .build();

    Waveform expectedUnified = Waveform.withValues(Instant.EPOCH, 40.0, ValidationBeam.COHERENT_3D.getBeam());

    List<Waveform> beams = algorithm.generateBeam(channelSegments);
    assertEquals(5, beams.size());

    Waveform beam1 = beams.get(0);
    assertEquals(Instant.EPOCH, beam1.getStartTime());
    assertEquals(Instant.parse("1970-01-01T00:00:00.975Z"), beam1.getEndTime());

    Waveform expectedBeam1 = expectedUnified.window(Instant.EPOCH, beam1.getEndTime());
    assertArrayEquals(expectedBeam1.getValues(), beam1.getValues(), 0.001);

    Waveform beam2 = beams.get(1);
    assertEquals(Instant.parse("1970-01-01T00:00:01.000Z"), beam2.getStartTime());
    assertEquals(Instant.parse("1970-01-01T00:00:01.475Z"), beam2.getEndTime());

    Waveform expectedBeam2 = expectedUnified.window(beam2.getStartTime(), beam2.getEndTime());
    assertArrayEquals(expectedBeam2.getValues(), beam2.getValues(), 0.001);

    Waveform beam3 = beams.get(2);
    assertEquals(Instant.parse("1970-01-01T00:00:01.500Z"), beam3.getStartTime());
    assertEquals(Instant.parse("1970-01-01T00:00:02.975Z"), beam3.getEndTime());

    Waveform expectedBeam3 = expectedUnified.window(beam3.getStartTime(), beam3.getEndTime());
    assertArrayEquals(expectedBeam3.getValues(), beam3.getValues(), 0.001);

    Waveform beam4 = beams.get(3);
    assertEquals(Instant.parse("1970-01-01T00:00:03.000Z"), beam4.getStartTime());
    assertEquals(Instant.parse("1970-01-01T00:00:03.225Z"), beam4.getEndTime());

    Waveform expectedBeam4 = expectedUnified.window(beam4.getStartTime(), beam4.getEndTime());
    assertArrayEquals(expectedBeam4.getValues(), beam4.getValues(), 0.001);

    Waveform beam5 = beams.get(4);
    assertEquals(Instant.parse("1970-01-01T00:00:03.250Z"), beam5.getStartTime());
    assertEquals(Instant.parse("1970-01-01T00:00:04.975Z"), beam5.getEndTime());

    Waveform expectedBeam5 = beams.get(4);
    assertArrayEquals(expectedBeam5.getValues(), beam5.getValues(), 0.001);
  }

  @Test
  public void testSingleSampleGap() throws IOException {
    Map<UUID, RelativePosition> relativePositions = new HashMap<>();
    List<ChannelSegment<Waveform>> channelSegments = new ArrayList<>();
    Instant startTime = Instant.EPOCH;
    for (int i = 0; i < 2; i++) {
      RelativePosition position = RelativePosition.from(0, 0, 0);
      UUID channelId = UUID.randomUUID();
      relativePositions.put(channelId, position);

      channelSegments.add(ChannelSegment.create(channelId,
          "Test",
          ChannelSegment.Type.RAW,
          List.of(Waveform.withValues(startTime, 40, new double[10])),
          CreationInfo.DEFAULT));
    }

    RelativePosition position = RelativePosition.from(0, 0, 0);
    UUID channelId = UUID.randomUUID();
    relativePositions.put(channelId, position);

    channelSegments.add(ChannelSegment.create(channelId,
        "Test",
        ChannelSegment.Type.RAW,
        List.of(
            Waveform.withValues(startTime, 1, new double[3]),
            Waveform.withValues(startTime.plusSeconds(5), 1, new double[6])),
        CreationInfo.DEFAULT));

    double slowness = 0.2857142857142857;
    double horizontalSlowness = slowness * Math.sin(Math.toRadians(15.0));

    PlaneWaveAlgorithm algorithm = new PlaneWaveAlgorithm.Builder()
        .withNominalSampleRate(1)
        .withSampleRateTolerance(0.001)
        .withAzimuth(200)
        .withHorizontalSlowness(horizontalSlowness)
        .withMediumVelocity(1 / slowness)
        .withSnappedSampling(true)
        .withCoherence(true)
        .withDimensionality(false)
        .withPhaseType(PhaseType.P)
        .withRelativePositions(relativePositions)
        .withMinimumWaveformsForBeam(1)
        .build();

    List<Waveform> beams = algorithm.generateBeam(channelSegments);

    assertEquals(2, beams.size());

    Waveform beam1 = beams.get(0);
    assertEquals(startTime, beam1.getStartTime());
    assertEquals(startTime.plusSeconds(2), beam1.getEndTime());
    assertEquals(3, beam1.getValues().length);
    assertEquals(1.0, beam1.getSampleRate(), 0.0000001);

    Waveform beam2 = beams.get(1);
    assertEquals(startTime.plusSeconds(5), beam2.getStartTime());
    assertEquals(startTime.plusSeconds(10), beam2.getEndTime());
    assertEquals(6, beam2.getValues().length);
    assertEquals(1.0, beam2.getSampleRate(), 0.0000001);
  }

  @Test
  public void testCoherent2D() throws IOException {
    BeamValidationData data =
        TestFixtures.loadData("ASARgeom.json",
            "ASAR_spiketest.json",
            true,
            true,
            ValidationBeam.COHERENT_2D);
    testBeam(data);
  }

  @Test
  public void testCoherent3D() throws IOException {
    BeamValidationData data =
        TestFixtures.loadData("ASARgeom.json",
            "ASAR_spiketest.json",
            false,
            true,
            ValidationBeam.COHERENT_3D);
    testBeam(data);
  }

  @Test
  public void testIncoherent2D() throws IOException {
    BeamValidationData data =
        TestFixtures.loadData("ASARgeom.json",
            "ASAR_spiketest.json",
            true,
            false,
            ValidationBeam.INCOHERENT_2D);
    testBeam(data);
  }

  @Test
  public void testIncoherent3D() throws IOException {
    BeamValidationData data =
        TestFixtures.loadData("ASARgeom.json",
            "ASAR_spiketest.json",
            false,
            false,
            ValidationBeam.INCOHERENT_3D);
    testBeam(data);
  }

  @Test
  public void testBaz0Inc02d() throws IOException {
    double[] beam = new double[200];
    for (int i = 0; i < 200; i++) {
      if (i == 100) {
        beam[i] = 16.0 / 21.0;
      } else if (i == 101) {
        beam[i] = -11.0 / 21.0;
      } else if (i == 102) {
        beam[i] = -5.0 / 21.0;
      } else {
        beam[i] = 0.0;
      }
    }

    BeamValidationData data =
        TestFixtures.loadData("ASARgeom.json",
            "ASARspike_baz0_inc0_2D.json",
            true,
            true,
            beam);
    testBeam(data);
  }

  @Test
  public void testBaz0Inc90() throws IOException {
    BeamValidationData data =
        TestFixtures.loadData("ASARgeom.json",
            "ASARspike_baz0_inc90.json",
            false,
            true,
            ValidationBeam.COHERENT_3D);
    testBeam(data);
  }

  @Test
  public void testBaz90Inc90() throws IOException {
    double[] beam = new double[200];
    for (int i = 0; i < 200; i++) {
      if (i == 100) {
        beam[i] = 20.0 / 21.0;
      } else if (i == 101) {
        beam[i] = -20.0 / 21.0;
      }
    }

    BeamValidationData data =
        TestFixtures.loadData("ASARgeom.json",
            "ASARspike_baz90_inc90.json",
            false,
            true,
            beam);
    testBeam(data);
  }

  @Test
  public void testBaz180Inc90() throws IOException {
    BeamValidationData data =
        TestFixtures.loadData("ASARgeom.json",
            "ASARspike_baz180_inc90.json",
            false,
            true,
            ValidationBeam.COHERENT_3D);
    testBeam(data);
  }

  @Test
  public void testBaz270Inc90() throws IOException {
    BeamValidationData data =
        TestFixtures.loadData("ASARgeom.json",
            "ASARspike_baz270_inc90.json",
            false,
            false,
            ValidationBeam.COHERENT_3D);

    assertThrows(IllegalArgumentException.class, () ->testBeam(data));
  }

  @Test
  public void testBaz275Inc90() throws IOException {
    double[] beam = new double[200];
    for (int i = 0; i < 200; i++) {
      if (i == 100) {
        beam[i] = 20.0 / 21.0;
      } else if (i == 101) {
        beam[i] = -20.0 / 21.0;
      } else {
        beam[i] = 0.0;
      }
    }

    BeamValidationData data =
        TestFixtures.loadData("ASARgeom.json",
            "ASARspike_baz275_inc90.json",
            false,
            true,
            beam);
    testBeam(data);
  }

  @Test
  public void testNullChannelSegments() throws IOException {
    BeamValidationData data =
        TestFixtures.loadData("ASARgeom.json",
            "ASAR_spiketest.json",
            false,
            true,
            ValidationBeam.COHERENT_3D);
    PlaneWaveAlgorithm algorithm = new PlaneWaveAlgorithm.Builder()
        .withNominalSampleRate(data.getNominalSampleRate())
        .withSampleRateTolerance(data.getSampleRateTolerance())
        .withAzimuth(data.getAzimuth())
        .withHorizontalSlowness(data.getHorizontalSlowness())
        .withMediumVelocity(data.getMediumVelocity())
        .withSnappedSampling(data.isSnappedSampling())
        .withCoherence(data.isCoherent())
        .withDimensionality(data.isTwoDimensional())
        .withPhaseType(data.getPhaseType())
        .withRelativePositions(data.getRelativePositionsByChannelId())
        .withMinimumWaveformsForBeam(1)
        .build();

    assertThrows(NullPointerException.class, () -> algorithm.generateBeam(null));
  }

  @Test
  public void testNullRelativePositions() throws IOException {
    BeamValidationData data =
        TestFixtures.loadData("ASARgeom.json",
            "ASAR_spiketest.json",
            false,
            true,
            ValidationBeam.COHERENT_3D);
    assertThrows(NullPointerException.class,
        () -> new PlaneWaveAlgorithm.Builder()
            .withNominalSampleRate(data.getNominalSampleRate())
            .withSampleRateTolerance(data.getSampleRateTolerance())
            .withAzimuth(data.getAzimuth())
            .withHorizontalSlowness(data.getHorizontalSlowness())
            .withMediumVelocity(data.getMediumVelocity())
            .withSnappedSampling(data.isSnappedSampling())
            .withCoherence(data.isCoherent())
            .withDimensionality(data.isTwoDimensional())
            .withPhaseType(data.getPhaseType())
            .withRelativePositions(null)
            .withMinimumWaveformsForBeam(1)
            .build());
  }

  @Test
  public void testRelativePositionChannelSegmentMismatch() throws IOException {
    BeamValidationData data =
        TestFixtures.loadData("ASARgeom.json",
            "ASAR_spiketest.json",
            false,
            true,
            ValidationBeam.COHERENT_3D);
    PlaneWaveAlgorithm algorithm = new PlaneWaveAlgorithm.Builder()
        .withNominalSampleRate(data.getNominalSampleRate())
        .withSampleRateTolerance(data.getSampleRateTolerance())
        .withAzimuth(data.getAzimuth())
        .withHorizontalSlowness(data.getHorizontalSlowness())
        .withMediumVelocity(data.getMediumVelocity())
        .withSnappedSampling(data.isSnappedSampling())
        .withCoherence(data.isCoherent())
        .withDimensionality(data.isTwoDimensional())
        .withPhaseType(data.getPhaseType())
        .withRelativePositions(data.getRelativePositionsByChannelId())
        .withMinimumWaveformsForBeam(1)
        .build();

    Waveform waveform = Waveform.withValues(Instant.now(), 40.0, new double[200]);
    ChannelSegment<Waveform> channelSegment = ChannelSegment.create(UUID.randomUUID(), "Test name",
        ChannelSegment.Type.ACQUIRED,
        Collections.singleton(waveform),
        CreationInfo.DEFAULT);

    data.getChannelSegments().add(channelSegment);

    assertThrows(IllegalArgumentException.class,
        () -> algorithm.generateBeam(data.getChannelSegments()));
  }

  @Test
  public void testFalseSnappedSampling() throws IOException {
    BeamValidationData data =
        TestFixtures.loadData("ASARgeom.json",
            "ASAR_spiketest.json",
            false,
            true,
            ValidationBeam.COHERENT_3D);
    assertThrows(IllegalArgumentException.class,
        () -> new PlaneWaveAlgorithm.Builder()
            .withNominalSampleRate(data.getNominalSampleRate())
            .withSampleRateTolerance(data.getSampleRateTolerance())
            .withAzimuth(data.getAzimuth())
            .withHorizontalSlowness(data.getHorizontalSlowness())
            .withMediumVelocity(data.getMediumVelocity())
            .withSnappedSampling(false)
            .withCoherence(data.isCoherent())
            .withDimensionality(data.isTwoDimensional())
            .withPhaseType(data.getPhaseType())
            .withRelativePositions(data.getRelativePositionsByChannelId())
            .withMinimumWaveformsForBeam(1)
            .build());
  }

  @Test
  public void testNullPhaseType() throws IOException {
    BeamValidationData data =
        TestFixtures.loadData("ASARgeom.json",
            "ASAR_spiketest.json",
            false,
            true,
            ValidationBeam.COHERENT_3D);
    assertThrows(NullPointerException.class,
        () -> new PlaneWaveAlgorithm.Builder()
            .withNominalSampleRate(data.getNominalSampleRate())
            .withSampleRateTolerance(data.getSampleRateTolerance())
            .withAzimuth(data.getAzimuth())
            .withHorizontalSlowness(data.getHorizontalSlowness())
            .withMediumVelocity(data.getMediumVelocity())
            .withSnappedSampling(data.isSnappedSampling())
            .withCoherence(data.isCoherent())
            .withDimensionality(data.isTwoDimensional())
            .withPhaseType(null)
            .withRelativePositions(data.getRelativePositionsByChannelId())
            .withMinimumWaveformsForBeam(1)
            .build());
  }
}
