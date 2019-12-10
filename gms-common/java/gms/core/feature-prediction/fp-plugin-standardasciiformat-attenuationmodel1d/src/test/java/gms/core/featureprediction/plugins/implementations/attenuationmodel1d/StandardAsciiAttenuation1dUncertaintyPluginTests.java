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
public class StandardAsciiAttenuation1dUncertaintyPluginTests {

  private StandardAsciiAttenuation1dUncertaintyPlugin attenuation1dPlugin;

  private static final String EARTH_MODEL = "VeithClawson72";

  private static final Set<String> EARTH_MODELS = Set
      .of(StandardAsciiAttenuation1dUncertaintyPluginTests.EARTH_MODEL);


  @BeforeAll
  void init() throws IOException {

    this.attenuation1dPlugin = new StandardAsciiAttenuation1dUncertaintyPlugin();
    this.attenuation1dPlugin
        .initialize(StandardAsciiAttenuation1dUncertaintyPluginTests.EARTH_MODELS);
  }


  @Test
  void testGetEarthModelNames() {

    Set<String> earthModelNames = this.attenuation1dPlugin.getEarthModelNames();

    Assertions
        .assertEquals(StandardAsciiAttenuation1dUncertaintyPluginTests.EARTH_MODELS,
            earthModelNames);
  }


  @Test
  void testGetPhaseTypes() {

    Set<PhaseType> phaseTypes = this.attenuation1dPlugin
        .getPhaseTypes(StandardAsciiAttenuation1dUncertaintyPluginTests.EARTH_MODEL);

    Assertions.assertEquals(Set.of(PhaseType.P), phaseTypes);
  }


  @Test
  void testGetDepthsKm() {

    double[] depths = this.attenuation1dPlugin
        .getDepthsKm(StandardAsciiAttenuation1dUncertaintyPluginTests.EARTH_MODEL, PhaseType.P);

    Assertions.assertEquals(11, depths.length);
  }


  @Test
  void testGetDistancesDeg() {

    double[] distances = this.attenuation1dPlugin
        .getDistancesDeg(StandardAsciiAttenuation1dUncertaintyPluginTests.EARTH_MODEL, PhaseType.P);

    Assertions.assertEquals(19, distances.length);
  }


  @Test
  void testGetValues() {

    double[][] values = this.attenuation1dPlugin
        .getValues(StandardAsciiAttenuation1dUncertaintyPluginTests.EARTH_MODEL, PhaseType.P);

    Assertions.assertEquals(11, values.length);
    Assertions.assertEquals(19, values[0].length);
  }


  @Test
  void testGetDepthModelingErrors() {

    Optional<double[]> depthModelingErrorsOptional = this.attenuation1dPlugin
        .getDepthModelingErrors(StandardAsciiAttenuation1dUncertaintyPluginTests.EARTH_MODEL,
            PhaseType.P);

    Assertions.assertFalse(depthModelingErrorsOptional.isPresent());
  }


  @Test
  void testGetDistancehModelingErrors() {

    Optional<double[]> distanceModelingErrorsOptional = this.attenuation1dPlugin
        .getDistanceModelingErrors(StandardAsciiAttenuation1dUncertaintyPluginTests.EARTH_MODEL,
            PhaseType.P);

    Assertions.assertFalse(distanceModelingErrorsOptional.isPresent());
  }


  @Test
  void testGetValueModelingErrors() {

    Optional<double[][]> valueModelingErrorsOptional = this.attenuation1dPlugin
        .getValueModelingErrors(StandardAsciiAttenuation1dUncertaintyPluginTests.EARTH_MODEL,
            PhaseType.P);

    Assertions.assertFalse(valueModelingErrorsOptional.isPresent());
  }
}
