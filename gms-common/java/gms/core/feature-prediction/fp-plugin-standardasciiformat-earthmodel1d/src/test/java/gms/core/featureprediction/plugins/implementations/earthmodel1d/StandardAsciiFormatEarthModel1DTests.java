package gms.core.featureprediction.plugins.implementations.earthmodel1d;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import gms.core.featureprediction.plugins.DepthDistance1dModelSet;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

public class StandardAsciiFormatEarthModel1DTests {

  private Set<String> earthModels = Set.of("ak135", "iasp91");

  @Test
  public void loadEarthModels() throws Exception {
    DepthDistance1dModelSet<double[], double[][]> models = new StandardAsciiTravelTime1dPlugin();
    models.initialize(this.earthModels);

    assertArrayEquals(TestFixtures.ak135LgReferenceDepthKm,
        models.getDepthsKm("ak135", PhaseType.Lg),
        0.0);

    assertArrayEquals(TestFixtures.ak135LgReferenceAngleDegrees,
        models.getDistancesDeg("ak135", PhaseType.Lg), 0.0);

    for (int i = 0; i < TestFixtures.ak135LgReferenceTravelTime.length; i++) {
      assertArrayEquals(TestFixtures.ak135LgReferenceTravelTime[i],
          models.getValues("ak135", PhaseType.Lg)[i], 0.0);
    }

    assertArrayEquals(TestFixtures.ak135LgModelingErrorDistances,
        models.getDistanceModelingErrors("ak135", PhaseType.Lg).get(), 0.0);

    assertFalse(models.getDepthModelingErrors("ak135", PhaseType.Lg).isPresent());

    for (int i = 0; i < TestFixtures.ak135LgModelingErrorValues.length; i++) {
      assertArrayEquals(TestFixtures.ak135LgModelingErrorValues[i],
          models.getValueModelingErrors("ak135", PhaseType.Lg).get()[i]
          , 0.0);
    }
  }

  @Test
  public void testGetModelingErrorNoErrorValues() throws Exception {
    DepthDistance1dModelSet<double[], double[][]> models = new StandardAsciiTravelTime1dPlugin();
    models.initialize(this.earthModels);
  }

  @Test
  public void testEarthModelEquals() throws Exception {
    StandardAsciiTravelTime1dFileReader m1 = StandardAsciiTravelTime1dFileReader
        .from("earthmodels1d/ak135");
    StandardAsciiTravelTime1dFileReader m2 = StandardAsciiTravelTime1dFileReader
        .from("earthmodels1d/ak135");
    assertEquals(m1, m2);
  }

  @Test
  public void testEarthModelNotEquals() throws Exception {
    StandardAsciiTravelTime1dFileReader m1 = StandardAsciiTravelTime1dFileReader
        .from("earthmodels1d/ak135");
    StandardAsciiTravelTime1dFileReader m2 = StandardAsciiTravelTime1dFileReader
        .from("earthmodels1d/iasp91");
    assertNotEquals(m1, m2);
  }

  @Test
  public void testGetModelNames() throws Exception {

    DepthDistance1dModelSet<double[], double[][]> models = new StandardAsciiTravelTime1dPlugin();
    models.initialize(earthModels);

    assertEquals(Set.of("ak135", "iasp91"), models.getEarthModelNames());
  }

  @Test
  public void testGetPhasesForModelNames() throws Exception {

    DepthDistance1dModelSet<double[], double[][]> models = new StandardAsciiTravelTime1dPlugin();
    models.initialize(earthModels);

    Set<PhaseType> availablePhaseTypes = models.getPhaseTypes("ak135");
    for (PhaseType p : availablePhaseTypes) {
      assertTrue(Arrays.stream(PhaseType.values()).collect(Collectors.toSet()).contains(p));
    }
  }
}
