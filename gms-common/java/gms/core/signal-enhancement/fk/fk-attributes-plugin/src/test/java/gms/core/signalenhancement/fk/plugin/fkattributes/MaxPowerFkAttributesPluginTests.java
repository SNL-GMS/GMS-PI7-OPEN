package gms.core.signalenhancement.fk.plugin.fkattributes;

import static org.junit.Assert.assertEquals;

import gms.core.signalenhancement.fk.plugin.util.FkSpectraInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkAttributes;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum;
import org.junit.Test;

public class MaxPowerFkAttributesPluginTests {
  private final FkSpectraInfo spectraInfo = FkSpectraInfo.create(0.0, 10.0, -0.1, 0.1, -0.1, 0.1);
  private final double[][] power = {
      {0, 0, 0},
      {0, 0, 0},
      {0, 1, 0}
  };

  private final double[][] fstat = {
      {0, 0, 0},
      {0, 0, 0},
      {0, 19.801980198019827, 0}
  };

  private final int quality = 4;
  

  @Test(expected = NullPointerException.class)
  public void testNonNullSpectrum() {
    MaxPowerFkAttributesPlugin dfkap = new MaxPowerFkAttributesPlugin();
    dfkap.generateFkAttributes(spectraInfo, null);
  }

  //TODO: fstat and quality
  @Test(expected = NullPointerException.class)
  public void testNonNullSpectraInfo() {
    MaxPowerFkAttributesPlugin dfkap = new MaxPowerFkAttributesPlugin();
    FkSpectrum spectrum = FkSpectrum.from(power, fstat, quality);
    dfkap.generateFkAttributes(null, spectrum);
  }

  @Test
  public void testGenerateFkAttributes() {
    MaxPowerFkAttributesPlugin dfkap = new MaxPowerFkAttributesPlugin();
    FkSpectrum spectrum = FkSpectrum.from(power, fstat, quality);
    //TODO: fix fstat
    double expectedAzimuth = 0.0;
    double expectedSlowness = 11.119492664455874;
    double expectedDelAz = 1.1786995569241125;
    double expectedDelSlow = 0.22874826155190894;
    double expectedFstat = 19.801980198019827;
    FkAttributes expectedAttributes = FkAttributes.from(expectedAzimuth, expectedSlowness,
        expectedDelAz, expectedDelSlow, expectedFstat);
    FkAttributes actualAttributes = dfkap.generateFkAttributes(spectraInfo, spectrum);

    assertEquals(expectedAttributes.getAzimuth(), actualAttributes.getAzimuth(), 0.0001);
    assertEquals(expectedAttributes.getSlowness(), actualAttributes.getSlowness(), 0.0001);
    assertEquals(expectedAttributes.getAzimuthUncertainty(), actualAttributes.getAzimuthUncertainty(), 0.0001);
    assertEquals(expectedAttributes.getSlownessUncertainty(), actualAttributes.getSlownessUncertainty(), 0.0001);
    assertEquals(expectedAttributes.getPeakFStat(), actualAttributes.getPeakFStat(), 0.0001);
  }
}
