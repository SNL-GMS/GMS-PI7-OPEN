package gms.core.signalenhancement.fk.plugin.fkattributes;

import gms.core.signalenhancement.fk.plugin.util.FkSpectraInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkAttributes;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of the FK attributes plugin.
 */
public class MaxPowerFkAttributesPlugin implements FkAttributesPlugin {

  private static final Logger logger = LoggerFactory.getLogger(MaxPowerFkAttributesPlugin.class);

  private static final String PLUGIN_NAME = "maxPowerFkAttributesPlugin";

  private static final PluginVersion pluginVersion = PluginVersion.from(1, 0, 0);

  private FkAttributesPluginConfiguration pluginConfiguration;

  public MaxPowerFkAttributesPlugin() {
    pluginConfiguration = new FkAttributesPluginConfiguration();
  }

  /**
   * Obtains this plugin's name
   *
   * @return the name of the plugin
   */
  @Override
  public String getName() {
    return PLUGIN_NAME;
  }

  /**
   * Obtains this plugin's version number
   *
   * @return {@link PluginVersion}, not null
   */
  @Override
  public PluginVersion getVersion() {
    return pluginVersion;
  }

  /**
   * Default configuration used - no configuration necessary
   *
   * @param parameterFieldMap Parameter field map to be used for the plugin configuration, not null
   */
  @Override
  public void initialize(Map<String, Object> parameterFieldMap) {
    Objects.requireNonNull(parameterFieldMap, "Plugin parameter field map must not be null.");
    logger.info("Default configuration used - no configuration necessary at this time.");
    this.pluginConfiguration = new FkAttributesPluginConfiguration();
  }

  /**
   * Calculate various FK attributes from a given FK spectrum
   *
   * @param spectraInfo A {@link FkSpectraInfo} that contains fields from {@link FkSpectra} needed
   * to calculate the FK Attributes
   * @param spectrum {@link FkSpectrum} to generate attributes from
   * @return Calculated {@link FkAttributes} for the spectrum at an FK Max Coordinate
   */
  public FkAttributes generateFkAttributes(
      FkSpectraInfo spectraInfo,
      FkSpectrum spectrum) {
    Objects.requireNonNull(spectraInfo, "(FkSpectrainfo) spectra information cannot be null.");
    Objects.requireNonNull(spectrum, "(double[][]) spectrum values cannot be null.");

    logger.debug("Validating arguments");

    //All of the calculations will need the (x,y) coordinate of the FK max
    Pair<Double, Double> fMaxCoordinate = DefaultFkMeasurementsAlgorithms
        .indexOfFkMax(spectrum.getPower().copyOf());

    logger.debug("FK max at {}", fMaxCoordinate);

    FkAttributesCalculator calc = FkAttributesCalculator
        .create(spectraInfo, spectrum, fMaxCoordinate);

    return FkAttributes.from(calc.azimuth(), calc.slowness(), calc.azimuthUncertainty(),
        calc.slownessUncertainty(), calc.fStatistic());
  }

  /**
   * Calculate various FK attributes from a given FK spectrum and custom point representing a peak
   *
   * @param spectraInfo A {@link FkSpectraInfo} that contains fields from {@link FkSpectra} needed
   * to calculate the FK Attributes
   * @param spectrum {@link FkSpectrum} to generate attributes from
   * @param customPoint {@link Pair} of {@link Double}s representing a custom point on the FK
   * Spectrum
   * @return Calculated {@link FkAttributes} for the spectrum at an FK Max Coordinate
   */
  public FkAttributes generateFkAttributes(
      FkSpectraInfo spectraInfo,
      FkSpectrum spectrum,
      Pair<Double, Double> customPoint) {
    logger.debug("Validating arguments");
    Objects.requireNonNull(spectrum, "(FkSpectrum) spectrum cannot be null.");
    Objects.requireNonNull(customPoint, "(Pair<Double, Double>) customPoint cannot be null.");

    logger.info(String.format("Generating FkAttributes for spectrum %s", spectrum.toString()));

    logger.debug("Generating FK Attributes for spectrum: {}", spectrum);

    logger.debug("Custom FK Max at {} read from definition.", customPoint);

    FkAttributesCalculator calc = FkAttributesCalculator.create(spectraInfo, spectrum, customPoint);

    return FkAttributes.from(calc.azimuth(), calc.slowness(), calc.azimuthUncertainty(),
        calc.slownessUncertainty(), calc.fStatistic());
  }
}
