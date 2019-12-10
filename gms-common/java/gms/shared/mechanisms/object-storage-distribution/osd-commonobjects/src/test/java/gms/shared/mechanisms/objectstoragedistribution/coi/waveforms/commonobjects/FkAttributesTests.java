package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import static junit.framework.TestCase.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.TestFixtures;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FkAttributesTests {

  //TODO: More sane defaults.
  private final double azimuth = 1;
  private final double slowness = 1;
  private final double azimuthUncertainty = 1;
  private final double slownessUncertainty = 1;
  private final double peakFStat = 0.04;

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.fkAttributes(), FkAttributes.class);
  }

  @Test
  public void testCreate() throws Exception {

    FkAttributes fkAttributes = FkAttributes
        .from(azimuth, slowness, azimuthUncertainty, slownessUncertainty, peakFStat);

    assertTrue(fkAttributes.getAzimuth() == azimuth &&
        fkAttributes.getSlowness() == slowness &&
        fkAttributes.getAzimuthUncertainty() == azimuthUncertainty &&
        fkAttributes.getSlownessUncertainty() == slownessUncertainty &&
        fkAttributes.getPeakFStat() == peakFStat);
  }

  @Test
  public void testFrom() throws Exception {

    FkAttributes fkAttributes = FkAttributes
        .from(azimuth, slowness, azimuthUncertainty, slownessUncertainty, peakFStat);

    assertTrue(fkAttributes.getAzimuth() == azimuth &&
        fkAttributes.getSlowness() == slowness &&
        fkAttributes.getAzimuthUncertainty() == azimuthUncertainty &&
        fkAttributes.getSlownessUncertainty() == slownessUncertainty &&
        fkAttributes.getPeakFStat() == peakFStat);
  }

  @Test
  public void testFieldBadBounds() throws Exception {

    exception.expect(IllegalArgumentException.class);
    FkAttributes fkAttributesBadDelAz = FkAttributes
        .from(azimuth, slowness, -1, slownessUncertainty, peakFStat);

    exception.expect(IllegalArgumentException.class);
    FkAttributes fkAttributesBadDelSlow = FkAttributes
        .from(azimuth, slowness, azimuthUncertainty, -1, peakFStat);
  }
}
