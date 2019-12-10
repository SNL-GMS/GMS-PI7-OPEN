package gms.core.signaldetection.association.plugins.implementations.globalgrid;

import gms.core.signaldetection.association.plugins.SdhStationAssociation;
import gms.core.signaldetection.association.plugins.SignalDetectionAssociatorDefinition;
import gms.core.signaldetection.association.plugins.SignalDetectionAssociatorPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.pluginregistry.Name;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import gms.shared.mechanisms.pluginregistry.Version;
import gms.shared.utilities.geotess.GeoTessException;
import gms.shared.utilities.signalfeaturepredictionutility.SignalFeaturePredictionUtility;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Name("globalGridSignalDetectionAssociatorPlugin")
@Version("1.0.0")
public class GlobalGridSignalDetectionAssociatorPlugin implements SignalDetectionAssociatorPlugin {

  private boolean initialized;
  static Logger logger = LoggerFactory.getLogger(GlobalGridSignalDetectionAssociatorPlugin.class);
  private GlobalGridSignalDetectionAssociatorDelegate delegate;
  private TesseractModelGA model;

  @Override
  public void initialize(
      String modelFilePath,
      Set<ReferenceStation> stations,
      List<SignalFeaturePredictionUtility> predictionUtilityList,
      SignalDetectionAssociatorDefinition definition) {
    this.delegate = GlobalGridSignalDetectionAssociatorDelegate.create(PluginRegistry.getRegistry(),
        PluginInfo.from("defaultRedundancyRemovalPlugin", "1.0.0"));
    String absFilePath = "tesseract-models/" + modelFilePath;
    try (InputStream is = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(absFilePath);
        BufferedInputStream bs = new BufferedInputStream(is);
        DataInputStream ds = new DataInputStream(bs)) {
      this.model = new TesseractModelGA(ds);
      initialized = true;
    } catch (GeoTessException | IOException | IllegalArgumentException ex) {
      logger.error(ex.getMessage(), ex);
      logger.info("Model in configuration may not have been populated attempting to populate model");
      try {
        this.model = TesseractModelPopulator.newBuilder()
            .gridFile(absFilePath)
            .phaseTypes(definition.getPhases().stream().collect(Collectors.toSet()))
            .gridCircleRadiusDegrees(definition.getGridCylinderRadiusDegrees())
            .minimumMagnitude(definition.getMinimumMagnitude())
            .stations(stations)
            .predictionUtilities(predictionUtilityList)
            .centerDepthKm(definition.getGridCylinderDepthKm())
            .gridCylinderHeightKm(definition.getGridCylinderHeightKm())
            .build()
            .call();
        initialized = true;
      } catch (TesseractModelPopulatorException e) {
        logger.error(ex.getMessage(), ex);
        initialized = false;
      }
    }
  }

  @Override
  public Pair<Set<SignalDetectionHypothesis>, Set<EventHypothesis>> associate(
      Collection<EventHypothesis> eventHypotheses,
      Collection<SdhStationAssociation> sdhStationAssociations,
      SignalDetectionAssociatorDefinition definition) {
    Objects.requireNonNull(eventHypotheses, "eventHypotheses must be a non-null object");
    Objects
        .requireNonNull(sdhStationAssociations, "sdhStationAssociations must be a non-null object");
    Objects.requireNonNull(definition, "definition to run algorithm is invalid");
    if (initialized) {
      return this.delegate
          .associate(eventHypotheses, sdhStationAssociations, definition, this.model);
    } else {
      throw new IllegalStateException(
          "Service may not have been properly initialized. Please restart and try again");
    }
  }
}
