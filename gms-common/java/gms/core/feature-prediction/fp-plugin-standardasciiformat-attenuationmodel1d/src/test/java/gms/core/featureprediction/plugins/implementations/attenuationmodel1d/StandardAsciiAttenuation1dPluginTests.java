package gms.core.featureprediction.plugins.implementations.attenuationmodel1d;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class StandardAsciiAttenuation1dPluginTests {

  private StandardAsciiAttenuation1dPlugin attenuation1dPlugin;

  private static final String EARTH_MODEL = "VeithClawson72";

  private static final Set<String> EARTH_MODELS = Set
      .of(StandardAsciiAttenuation1dPluginTests.EARTH_MODEL);


  @BeforeAll
  void init() throws IOException {

    this.attenuation1dPlugin = new StandardAsciiAttenuation1dPlugin();
    this.attenuation1dPlugin.initialize(StandardAsciiAttenuation1dPluginTests.EARTH_MODELS);
  }


  @Test
  void testGetEarthModelNames() {

    Set<String> earthModelNames = this.attenuation1dPlugin.getEarthModelNames();

    Assertions
        .assertEquals(StandardAsciiAttenuation1dPluginTests.EARTH_MODELS, earthModelNames);
  }


  @Test
  void testGetPhaseTypes() {

    Set<PhaseType> phaseTypes = this.attenuation1dPlugin
        .getPhaseTypes(StandardAsciiAttenuation1dPluginTests.EARTH_MODEL);

    Assertions.assertEquals(Set.of(PhaseType.P), phaseTypes);
  }


  @Test
  void testGetDepthsKm() {

    double[] depths = this.attenuation1dPlugin
        .getDepthsKm(StandardAsciiAttenuation1dPluginTests.EARTH_MODEL, PhaseType.P);

    Assertions.assertEquals(11, depths.length);
  }


  @Test
  void testGetDistancesDeg() {

    double[] distances = this.attenuation1dPlugin
        .getDistancesDeg(StandardAsciiAttenuation1dPluginTests.EARTH_MODEL, PhaseType.P);

    Assertions.assertEquals(101, distances.length);
  }


  @Test
  void testGetValues() {

    double[][] values = this.attenuation1dPlugin
        .getValues(StandardAsciiAttenuation1dPluginTests.EARTH_MODEL, PhaseType.P);

    Assertions.assertEquals(11, values.length);
    Assertions.assertEquals(101, values[0].length);
  }


  @Test
  void testGetDepthModelingErrors() {

    Optional<double[]> depthModelingErrorsOptional = this.attenuation1dPlugin
        .getDepthModelingErrors(StandardAsciiAttenuation1dPluginTests.EARTH_MODEL, PhaseType.P);

    Assertions.assertTrue(depthModelingErrorsOptional.isPresent());
    Assertions.assertEquals(11, depthModelingErrorsOptional.get().length);
  }


  @Test
  void testGetDistancehModelingErrors() {

    Optional<double[]> distanceModelingErrorsOptional = this.attenuation1dPlugin
        .getDistanceModelingErrors(StandardAsciiAttenuation1dPluginTests.EARTH_MODEL, PhaseType.P);

    Assertions.assertTrue(distanceModelingErrorsOptional.isPresent());
    Assertions.assertEquals(19, distanceModelingErrorsOptional.get().length);
  }


  @Test
  void testGetValueModelingErrors() {

    Optional<double[][]> valueModelingErrorsOptional = this.attenuation1dPlugin
        .getValueModelingErrors(StandardAsciiAttenuation1dPluginTests.EARTH_MODEL, PhaseType.P);

    Assertions.assertTrue(valueModelingErrorsOptional.isPresent());
    Assertions.assertEquals(11, valueModelingErrorsOptional.get().length);
    Assertions.assertEquals(19, valueModelingErrorsOptional.get()[0].length);
  }
}
