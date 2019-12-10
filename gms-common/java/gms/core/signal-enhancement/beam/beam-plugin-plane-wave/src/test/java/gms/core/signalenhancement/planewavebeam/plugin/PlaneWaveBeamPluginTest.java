package gms.core.signalenhancement.planewavebeam.plugin;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PlaneWaveBeamPluginTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testGetName() {
    assertEquals("planeWaveBeamPlugin", new PlaneWaveBeamPlugin().getName());
  }

  @Test
  public void testGetVersion() {
    assertEquals(PluginVersion.from(1, 0, 0), new PlaneWaveBeamPlugin().getVersion());
  }

  @Test
  public void testGenerateNullChannelSegmentsExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("PlaneWaveBeam cannot calculate beam from null channel segments");

    BeamDefinition definition = BeamDefinition
        .from(PhaseType.P, 1.0, 2.0, true, true, true, 6.0,
            7.0, Location.from(0, 0, 0, 0), Collections.emptyMap(), 2);
    PlaneWaveBeamPlugin plugin = new PlaneWaveBeamPlugin();
    plugin.beam(null, definition);
  }

  @Test
  public void testGenerateNullBeamDefinitionExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("PlaneWaveBeam cannot calculate beam from null definition");

    PlaneWaveBeamPlugin plugin = new PlaneWaveBeamPlugin();
    plugin.beam(Collections.emptyList(), null);
  }

}
