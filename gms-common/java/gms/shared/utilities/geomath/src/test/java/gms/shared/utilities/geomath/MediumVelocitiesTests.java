package gms.shared.utilities.geomath;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MediumVelocitiesTests {

  private final String mediumVelocitiesEarthModelName = "ak135";
  private final double expectedMediumVelocityP = 5.8;
  private final double expectedMediumVelocityS = 3.46;

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testMediumVelocitiesInitialize() throws IOException {

    MediumVelocities m = new MediumVelocities();
    m.initialize(mediumVelocitiesEarthModelName);

    Assert.assertEquals(m.getMediumVelocity(PhaseType.P), expectedMediumVelocityP, 0.0);
    Assert.assertEquals(m.getMediumVelocity(PhaseType.S), expectedMediumVelocityS, 0.0);
  }

  @Test
  public void testMediumVelocitiesInitializeNullModelNameThrowsNullPointerException()
      throws IOException {
    exception.expect(NullPointerException.class);
    exception
        .expectMessage("modelName parameter cannot be null for MediumVelocities::initialize()");

    MediumVelocities m = new MediumVelocities();
    m.initialize(null);
  }

  @Test
  public void testMediumVelocitiesGetMediumVelocityNullPhaseTypeThrowsNullPointerException()
      throws IOException {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "PhaseType parameter cannot be null in MediumVelocities::getMediumVelocity()");

    MediumVelocities m = new MediumVelocities();
    m.initialize(mediumVelocitiesEarthModelName);

    m.getMediumVelocity(null);
  }

  @Test
  public void testMediumVelocitiesGetMediumVelocityMapPhaseTypeIntoP()
      throws IllegalArgumentException, IOException {
    PhaseType p = PhaseType.PcP;

    MediumVelocities m = new MediumVelocities();
    m.initialize(mediumVelocitiesEarthModelName);

    Double mediumVelocity = m.getMediumVelocity(p);
    Assert.assertEquals(mediumVelocity, expectedMediumVelocityP, 0.0);
  }

  @Test
  public void testMediumVelocitiesGetMediumVelocityMapPhaseTypeIntoS()
      throws IllegalArgumentException, IOException {
    PhaseType p = PhaseType.PcS;

    MediumVelocities m = new MediumVelocities();
    m.initialize(mediumVelocitiesEarthModelName);

    Double mediumVelocity = m.getMediumVelocity(p);
    Assert.assertEquals(mediumVelocity, expectedMediumVelocityS, 0.0);
  }
}
