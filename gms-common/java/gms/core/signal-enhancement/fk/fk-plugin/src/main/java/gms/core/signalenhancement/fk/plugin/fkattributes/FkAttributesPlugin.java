package gms.core.signalenhancement.fk.plugin.fkattributes;

import gms.core.signalenhancement.fk.plugin.util.FkSpectraInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Plugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkAttributes;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum;
import org.apache.commons.lang3.tuple.Pair;

public interface FkAttributesPlugin extends Plugin {

  /**
   * Generates a list of FeatureMeasurement objects given a spectra and measurement definition.
   *
   * @param spectraInfo
   * @param spectrum
   */
  FkAttributes generateFkAttributes(
      FkSpectraInfo spectraInfo,
      FkSpectrum spectrum);

  /**
   * Generates a list of FeatureMeasurement objects given a spectra and measurement definition.
   *
   * @param spectraInfo
   * @param spectrum    @return                Spectrum for which to calculate attributes on
   * @param customPoint Point to calculate FK Attributes with
   */
  FkAttributes generateFkAttributes(
      FkSpectraInfo spectraInfo,
      FkSpectrum spectrum,
      Pair<Double, Double> customPoint);
}
