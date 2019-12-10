package gms.core.signalenhancement.fk.plugin.fkspectra;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.Plugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Collection;
import java.util.List;

public interface FkSpectraPlugin extends Plugin {

  /**
   * Generates the results of an Fk Spectrum
   *
   * @param channelSegments Collection of {@link ChannelSegment} containing waveforms for an Fk
   * Spectrum, not null
   * @param definition The Fk Spectrum definition identifying window lead and length, low/high
   * frequencies, sample rate, etc. used by the Fk Spectrum plugin
   * @return Fk Spectrum results
   */
  List<FkSpectra> generateFk(Collection<ChannelSegment<Waveform>> channelSegments,
      FkSpectraDefinition definition);

}
