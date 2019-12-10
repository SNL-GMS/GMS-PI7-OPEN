package gms.core.signalenhancement.fk.plugin.fkattributes;

import gms.core.signalenhancement.fk.plugin.util.FkSpectraInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * FkAttributesCalculator is a lazy calculator for the FK attributes.
 *
 * It resolves dependencies among measurements using lazy loading, to allow for order-agnostic calls
 * to the calculation methods. For example, azimuth uncertainty depends on slowness uncertainty, so
 * if a call to the azimuth method will first calulate the slowness uncertainty if it has not
 * already been calculated.
 */
public class FkAttributesCalculator {

  private static final Logger logger = LoggerFactory.getLogger(FkAttributesCalculator.class);

  private Double azimuth;
  private Double slowness;
  private Double azimuthUncertainty;
  private Double slownessUncertainty;
  private Double fStatistic;
  private FkSpectraInfo spectraInfo;
  private FkSpectrum spectrum;
  private Pair<Double, Double> fkMaxCoordinate;

  private final double DEFAULT_DB_DOWN_RADIUS = 0.04;

  public FkAttributesCalculator(
      FkSpectraInfo spectraInfo,
      FkSpectrum spectrum,
      Pair<Double, Double> fkMaxCoordinate) {
    this.spectraInfo = spectraInfo;
    this.spectrum = spectrum;
    this.fkMaxCoordinate = fkMaxCoordinate;
  }

  public static FkAttributesCalculator create(
      FkSpectraInfo spectraInfo,
      FkSpectrum spectrum,
      Pair<Double, Double> fkMaxCoordinate) {
    Objects.requireNonNull(spectraInfo, "Fk Spectra Info cannot be null.");
    Objects.requireNonNull(spectrum, "Fk Spectrum cannot be null.");
    Objects.requireNonNull(fkMaxCoordinate, "Fk Max Coordinate info cannot be null.");
    return new FkAttributesCalculator(spectraInfo, spectrum, fkMaxCoordinate);
  }

  public double azimuth() {
    if (this.azimuth != null) {
      return this.azimuth;
    }

    double azimuth = DefaultFkMeasurementsAlgorithms
        .azimuthOfIndex(spectraInfo.getEastSlowStart(),
            spectraInfo.getEastSlowDelta(),
            spectraInfo.getNorthSlowStart(),
            spectraInfo.getNorthSlowDelta(),
            fkMaxCoordinate.getLeft(),
            fkMaxCoordinate.getRight());

    this.azimuth = azimuth;

    return azimuth;
  }

  public double slowness() {
    if (this.slowness != null) {
      return this.slowness;
    }

    double slowness = DefaultFkMeasurementsAlgorithms
        .slownessOfIndex(spectraInfo.getEastSlowStart(),
            spectraInfo.getEastSlowDelta(),
            spectraInfo.getNorthSlowStart(),
            spectraInfo.getNorthSlowDelta(),
            fkMaxCoordinate.getLeft(),
            fkMaxCoordinate.getRight());

    this.slowness = slowness;

    return slowness;
  }

  public double fStatistic() {
    if (fStatistic == null) {
      //(x,y) => (column, row)
      fStatistic = spectrum.getFstat().getValue(fkMaxCoordinate.getRight().intValue(),
          fkMaxCoordinate.getLeft().intValue());
    }

    return fStatistic;
  }

  public double slownessUncertainty() {
    if (this.slownessUncertainty != null) {
      return this.slownessUncertainty;
    }

    double uncertainty = DefaultFkMeasurementsAlgorithms
        .slownessUncertainty(this.spectraInfo.getHighFrequency(),
            this.spectraInfo.getLowFrequency(),
            this.fStatistic(), DEFAULT_DB_DOWN_RADIUS);

    this.slownessUncertainty = uncertainty;

    return uncertainty;
  }

  public double azimuthUncertainty() {
    if (this.azimuthUncertainty != null) {
      return this.azimuthUncertainty;
    }

    double uncertainty = DefaultFkMeasurementsAlgorithms
        .azimuthUncertainty(this.slowness(), this.slownessUncertainty());

    this.azimuthUncertainty = uncertainty;

    return uncertainty;
  }
}