package gms.core.signalenhancement.beamcontrol.plugin;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.Plugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Collection;
import java.util.List;

public interface BeamPlugin extends Plugin {

  /**
   * Beam, yo. JUST BEAM IT
   *
   * @param wfs the waveforms to beam
   * @param def the definition of the beam
   * @return the beamed waveform
   */
  List<Waveform> beam(Collection<ChannelSegment<Waveform>> wfs,
      BeamDefinition def);
}
