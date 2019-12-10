package gms.core.signalenhancement.planewavebeam.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestFixtures {

  public static BeamValidationData loadData(String geometryFile,
      String waveform,
      boolean twoDimensional,
      boolean coherent,
      ValidationBeam beam)
      throws IOException {
    return loadData(geometryFile, waveform, twoDimensional, coherent, beam.getBeam());
  }

  /**
   * Generates a BeamValidationData set from the provided json files.
   * @param geometryFile The path to the resource containing the
   * @param waveformFile The file containing the waveforms
   * @param twoDimensional Whether the beam should be calculated using two dimensions
   * @param coherent Whether the beam should be coherent
   * @return The BeamValidationData containing the algorithm parameters, ChannelSegments from which
   * the beam should be calculated, and the expected output.
   * @throws IOException If there is an error reading the files containing the geometry and
   * waveforms
   */
  public static BeamValidationData loadData(String geometryFile,
      String waveformFile,
      boolean twoDimensional,
      boolean coherent,
      double[] beam)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> array = mapper.readValue(
        TestFixtures.class.getClassLoader().getResourceAsStream(geometryFile),
        new TypeReference<Map<String, Object>>() {
        });
    Map<String, Map<String, List<Double>>> geometry = mapper.convertValue(array.get("geometry"),
        new TypeReference<Map<String, Map<String, List<Double>>>>() {
        });

    Map<String, Object> waveforms =
        mapper.readValue(TestFixtures.class.getClassLoader().getResourceAsStream(waveformFile),
            new TypeReference<Map<String, Object>>() {
            });
    Map<String, Object> arrival = mapper.convertValue(waveforms.get("arrival"),
        new TypeReference<Map<String, Object>>() {
        });
    double slowness = (Double) arrival.get("slowness");
    double backAzimuth = (Double) arrival.get("back_azimuth");
    double horizontalSlowness;

    if (arrival.containsKey("horizontal_slowness")) {
      horizontalSlowness = (Double) arrival.get("horizontal_slowness");
    } else {
      horizontalSlowness = slowness
          * Math.sin(Math.toRadians((Double) arrival.get("incidence_angle")));
    }

    double mediumVelocity = 1.0 / slowness;
    Map<UUID, RelativePosition> relativePositionsByUuid = new HashMap<>();
    List<ChannelSegment<Waveform>> channelSegments = new ArrayList<>();

    for (Map.Entry<String, Map<String, List<Double>>> entry : geometry.entrySet()) {
      UUID channelId = UUID.randomUUID();
      String name = entry.getKey();

      List<Double> cartesianCoordinates = entry.getValue().get("XYZ");

      RelativePosition position = RelativePosition.from(cartesianCoordinates.get(1),
          cartesianCoordinates.get(0),
          cartesianCoordinates.get(2)
      );

      relativePositionsByUuid.put(channelId, position);

      Map<String, Map<String, Object>> waveformsBySegment =
          mapper.convertValue(waveforms.get("waveforms"),
              new TypeReference<Map<String, Map<String, Object>>>() {
              });
      List<Double> segmentWaveform =
          mapper.convertValue(waveformsBySegment.get(name).get("waveform"),
              new TypeReference<List<Double>>() {
              });

      Waveform waveform = Waveform.withValues(Instant.EPOCH,
          (double) waveforms.get("sps"),
          segmentWaveform.stream().mapToDouble(Double::doubleValue).toArray());
      ChannelSegment<Waveform> channelSegment = ChannelSegment.create(channelId,
          name,          //(List<Double>) waveformsBySegment.get(name).get("waveform");
          ChannelSegment.Type.ACQUIRED,
          Collections.singleton(waveform),
          CreationInfo.DEFAULT);

      channelSegments.add(channelSegment);
    }

    Waveform beamWaveform = Waveform.withValues(Instant.now(),
        40.0,
        beam);

    return new BeamValidationData(40.0,
        0.01,
        backAzimuth,
        horizontalSlowness,
        mediumVelocity,
        true,
        coherent,
        twoDimensional,
        Instant.EPOCH,
        PhaseType.P,
        relativePositionsByUuid,
        channelSegments,
        beamWaveform);
  }

}
