package gms.core.signalenhancement.fk.plugin.algorithms;

import static org.junit.Assert.assertNotNull;

import gms.core.signalenhancement.fk.plugin.fkspectra.FkSpectraPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CaponFkSpectraPluginTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private FkSpectraPlugin plugin = new CaponFkSpectraPlugin();


  @Test
  public void testGenerateNullChannelSegmentExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "caponFkSpectraPlugin cannot generate FK spectra from null channel segments");
    plugin.generateFk(null, DefaultFkSpectrumTestData.FK_SPECTRUM_DEFINITION_CHANS_1_AND_2);
  }

  @Test
  public void testGenerateNullFkSpectrumDefinitionExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "caponFkSpectraPlugin cannot generate FK spectra from null FK spectra definition");
    plugin.generateFk(DefaultFkSpectrumTestData.CHANNEL_SEGMENTS, null);
  }

  @Test
  public void testGenerateFk() {
    List<FkSpectra> fkSpectra = plugin
        .generateFk(DefaultFkSpectrumTestData.CHANNEL_SEGMENTS,
            DefaultFkSpectrumTestData.FK_SPECTRUM_DEFINITION_CHANS_1_AND_2);
    assertNotNull(fkSpectra);
  }

  @Test
  public void testGenerateFkMissingRelativePositionExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "RelativePositionMap does not include a UUID key for each channel segment");
    plugin.generateFk(DefaultFkSpectrumTestData.CHANNEL_SEGMENTS,
        DefaultFkSpectrumTestData.FK_SPECTRUM_DEFINITION_CHANS_1_AND_3);
  }
}