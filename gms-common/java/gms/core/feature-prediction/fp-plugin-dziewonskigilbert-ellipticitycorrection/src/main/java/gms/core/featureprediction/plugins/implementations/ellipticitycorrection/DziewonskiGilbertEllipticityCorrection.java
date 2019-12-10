package gms.core.featureprediction.plugins.implementations.ellipticitycorrection;

import gms.core.featureprediction.plugins.EllipticityCorrectionPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionComponent;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.pluginregistry.Name;
import gms.shared.mechanisms.pluginregistry.Version;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Default implementation of {@link EllipticityCorrectionPlugin} plugin interface.  Holds
 * ellipticity correction values for multiple earth models and calculates ellipticity corrections
 * for available earth models and phases.
 */
@Name("dziewonskiGilbertEllipticityCorrection")
@Version("1.0.0")
public class DziewonskiGilbertEllipticityCorrection implements EllipticityCorrectionPlugin {

  private DziewonskiGilbertEllipticityCorrectionDelegate
      dziewonskiGilbertEllipticityCorrectionDelegate = null;

  /**
   * Creates a new UNINITIALIZED DziewonskiGilbertEllipticityCorrection object
   */
  public DziewonskiGilbertEllipticityCorrection() {
    /*corrections = new HashMap<>();*/
  }

  /**
   * Initializes the plugin via the provided {@link Map} representing the configuration values for
   * this plugin.  This function should be called before the plugin is used.
   *
   * @param earthModelNames Associates {@link String}s representing names of earth models with
   * {@link URL}s pointing to ellipticity correction files containing ellipticity correction values
   * for that earth model.
   */
  @Override
  public void initialize(Set<String> earthModelNames) {
    //TODO: Not sure we should be passing earth model names
    if (dziewonskiGilbertEllipticityCorrectionDelegate == null) {
      dziewonskiGilbertEllipticityCorrectionDelegate = new DziewonskiGilbertEllipticityCorrectionDelegate();
      dziewonskiGilbertEllipticityCorrectionDelegate.initialize(earthModelNames);
    } else {
      throw new IllegalStateException("Initialize() method has already been called.");
    }
  }

  /**
   * Calculates an ellipticity correction value
   *
   * @param modelName the name of the earth model for which to calculate an ellipticity correction
   * @param sourceLocation the {@link EventLocation} of the event
   * @param receiverLocation the {@link Location} of the receiver
   * @param phaseType the {@link PhaseType} for which to calculate an ellipticity correction
   * @return the ellipticity correction value
   */
  @Override
  public FeaturePredictionComponent correct(String modelName, EventLocation sourceLocation,
      Location receiverLocation,
      PhaseType phaseType) {

    Objects.requireNonNull(modelName,
        "DziewonskiGilbertEllipticityCorrection::correct() requires non-null modelName parameter");
    Objects.requireNonNull(sourceLocation,
        "DziewonskiGilbertEllipticityCorrection::correct() requires non-null sourceLocation parameter");
    Objects.requireNonNull(receiverLocation,
        "DziewonskiGilbertEllipticityCorrection::correct() requires non-null receiverLocation parameter");
    Objects.requireNonNull(phaseType,
        "DziewonskiGilbertEllipticityCorrection::correct() requires non-null phaseType parameter");

    return dziewonskiGilbertEllipticityCorrectionDelegate
        .correct(modelName, sourceLocation, receiverLocation, phaseType);
  }

}
