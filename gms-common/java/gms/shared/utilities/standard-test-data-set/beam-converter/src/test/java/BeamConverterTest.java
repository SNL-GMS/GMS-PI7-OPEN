import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.utilities.standardtestdataset.beamconverter.BeamConverter;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeamConverterTest {

  private static final Logger logger = LoggerFactory.getLogger(BeamConverterTest.class);
  private static BeamConverter beamConverter;
  // Currently, the number of beam definitions is determined by the unique number of Arid's in the input file
  private static final int NUMBER_BEAM_DEFINITIONS = 30;

  @BeforeClass
  public static void setup() throws Exception {
    // setup the BeamConverter
    final String resourcesDir = "src/test/resources/input-json-files/";
    final String beamDefinitionFile = resourcesDir + "BeamDefinition.json";
    final JsonNode beamJsonNode = new ObjectMapper().readTree(new File(beamDefinitionFile));

    beamConverter = new BeamConverter(beamJsonNode);
    logger.info("Setup complete, beginning tests...");
  }

  /**
   * Read the BeamDefinition file, pick a specific beam definition, and assert it has the values we
   * expect.
   */
  @Test
  public void testConversion() {
    final boolean coherent = true;
    final boolean twoDimensional = true;
    final boolean snappedSampling = true;
    final PhaseType phaseType = PhaseType.UNKNOWN;
    final double azimuth = 76.02789;
    final double slowness = 2.5544875;
    final double nominalSampleRate = 40.0;
    final double sampleRateTolerance = 0.01;

    final List<BeamDefinition> beamDefinitions = beamConverter.getConvertedBeams();

    assertEquals(NUMBER_BEAM_DEFINITIONS, beamDefinitions.size());

    // Currently, this finds the first (and only, in this data file) matching azimuth.
    // In the future, it should match on a specific ID, but that linkage does not exist right now
    final Optional<BeamDefinition> beamDefinitionOptional = beamDefinitions.stream()
        .filter(x -> Objects.equals(x.getAzimuth(), azimuth)).findFirst();
    assertTrue(beamDefinitionOptional.isPresent());
    final BeamDefinition beamDef = beamDefinitionOptional.get();

    assertTrue(beamDef.isCoherent());
    assertTrue(beamDef.isTwoDimensional());
    assertTrue(beamDef.isSnappedSampling());
    assertEquals(phaseType, PhaseType.UNKNOWN);
    assertEquals(azimuth, beamDef.getAzimuth(), 0.0);
    assertEquals(slowness, beamDef.getSlowness(), 0.0);
    assertEquals(nominalSampleRate, beamDef.getNominalWaveformSampleRate(), 0.0);
    assertEquals(sampleRateTolerance, beamDef.getWaveformSampleRateTolerance(), 0.0);
  }

}
