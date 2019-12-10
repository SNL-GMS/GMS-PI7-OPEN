package gms.core.eventlocation.control;

import gms.core.eventlocation.plugins.EventLocationDefinition;
import gms.core.eventlocation.plugins.definitions.EventLocationDefinitionApacheLm;
import gms.core.eventlocation.plugins.definitions.EventLocationDefinitionGeigers;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// TODO: refactor this entire class once configuration exists
public class EventLocationConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(EventLocationConfiguration.class);

  private Map<PluginInfo, EventLocationDefinition> eventLocationDefinitionMap;


  private EventLocationConfiguration(
      Map<PluginInfo, EventLocationDefinition> eventLocationDefinitionMap) {

    this.eventLocationDefinitionMap = eventLocationDefinitionMap;
  }


  public static EventLocationConfiguration create() {

    EventLocationDefinition eventLocationDefinitionGeigers;
    EventLocationDefinition eventLocationDefinitionApacheLm;

    try {

      eventLocationDefinitionGeigers = EventLocationConfiguration
          .loadEventLocationDefinitionGeigers();
      eventLocationDefinitionApacheLm = EventLocationConfiguration
          .loadEventLocationDefinitionApacheLm();
    } catch (ConfigurationException e) {

      String errMsg = "Failed to load EventLocationDefinition";
      EventLocationConfiguration.logger.error(errMsg);
      throw new IllegalStateException(errMsg, e);
    }

    Map<PluginInfo, EventLocationDefinition> eventLocationDefinitionMap = Map.ofEntries(
        Map.entry(PluginInfo.from("eventLocationGeigersPlugin", "1.0.0"),
            eventLocationDefinitionGeigers),
        Map.entry(PluginInfo.from("eventLocationApacheLmPlugin", "1.0.0"),
            eventLocationDefinitionApacheLm)
    );

    return new EventLocationConfiguration(eventLocationDefinitionMap);
  }


  public EventLocationControlParameters getParametersForPlugin(PluginInfo pluginInfo) {

    EventLocationDefinition eventLocationDefinition = this.eventLocationDefinitionMap
        .get(pluginInfo);

    if (Objects.isNull(eventLocationDefinition)) {

      String errMsg = String.format(
          "EventLocationConfiguration does not contain EventLocationControlParameters for the specified PluginInfo: %s",
          pluginInfo.toString());
      EventLocationConfiguration.logger.error(errMsg);
      throw new IllegalArgumentException(errMsg);
    }

    return EventLocationControlParameters.create(
        pluginInfo,
        eventLocationDefinition
    );
  }


  private static EventLocationDefinitionGeigers loadEventLocationDefinitionGeigers()
      throws ConfigurationException {

    Configuration config = new PropertiesConfiguration(
        "gms/core/eventlocation/control/eventLocationConfigurationGeigers.properties");

    int maximumIterationCount = config.getInt("maximumIterationCount");
    double convergenceThreshold = config.getDouble("convergenceThreshold");
    double uncertaintyProbabilityPercentile = config.getDouble("uncertaintyProbabilityPercentile");
    String earthModel = config.getString("earthModel");
    boolean applyTravelTimeCorrections = config.getBoolean("applyTravelTimeCorrections");
    ScalingFactorType scalingFactorType = ScalingFactorType
        .valueOf(config.getString("scalingFactorType"));
    int kWeight = config.getInt("kWeight");
    double aprioriVariance = config.getDouble("aprioriVariance");
    int minimumNumberOfObservations = config.getInt("minimumNumberOfObservations");
    int convergenceCount = config.getInt("convergenceCount");
    boolean levenbergMarquardtEnabled = config.getBoolean("levenbergMarquardtEnabled");
    double lambda0 = config.getDouble("lambda0");
    double lambdaX = config.getDouble("lambdaX");
    double deltaNormThreshold = config.getDouble("deltaNormThreshold");
    double singularValueWFactor = config.getDouble("singularValueWFactor");
    double maximumWeightedPartialDerivative = config.getDouble("maximumWeightedPartialDerivative");
    boolean constrainLatitudeParameter = config.getBoolean("constrainLatitudeParameter");
    boolean constrainLongitudeParameter = config.getBoolean("constrainLongitudeParameter");
    boolean constrainDepthParameter = config.getBoolean("constrainDepthParameter");
    boolean constrainTimeParameter = config.getBoolean("constrainTimeParameter");

    //TODO: Is this being used??
    double dampingFactorStep = config.getDouble("dampingFactorStep");

    //TODO: Is this being used??
    double deltamThreshold = config.getDouble("deltamThreshold");

    //TODO: Is this being used??
    int depthFixedIterationCount = config.getInt("depthFixedIterationCount");

    Objects.requireNonNull(earthModel,
        "Cannot create EventLocationConfigurationGeigers with null earthModel");

    LocationRestraint.Builder locationRestraintBuilder = new LocationRestraint.Builder();
    if (constrainLatitudeParameter) {
      locationRestraintBuilder.setLatitudeRestraint(config.getDouble("constrainLatitudeValue"));
    }
    if (constrainLongitudeParameter) {
      locationRestraintBuilder.setLongitudeRestraint(config.getDouble("constrainLongitudeValue"));
    }
    if (constrainDepthParameter) {
      if (config.containsKey("constrainDepthToSurface")) {
        locationRestraintBuilder.setDepthRestraintAtSurface();
      } else {
        locationRestraintBuilder.setDepthRestraint(config.getDouble("constrainDepthValue"));
      }
    }
    if (constrainTimeParameter) {
      locationRestraintBuilder
          .setTimeRestraint(Instant.parse(config.getString("constrainTimeValue")));
    }

    return EventLocationDefinitionGeigers.create(
        maximumIterationCount,
        convergenceThreshold,
        uncertaintyProbabilityPercentile,
        earthModel,
        applyTravelTimeCorrections,
        scalingFactorType,
        kWeight,
        aprioriVariance,
        minimumNumberOfObservations,
        convergenceCount,
        levenbergMarquardtEnabled,
        lambda0,
        lambdaX,
        deltaNormThreshold,
        singularValueWFactor,
        maximumWeightedPartialDerivative,
        dampingFactorStep,
        deltamThreshold,
        depthFixedIterationCount,
        List.of(locationRestraintBuilder.build()));
  }

  private static EventLocationDefinitionApacheLm loadEventLocationDefinitionApacheLm()
      throws ConfigurationException {

    Configuration config = new PropertiesConfiguration(
        "gms/core/eventlocation/control/eventLocationConfigurationApacheLm.properties");

    int maximumIterationCount = config.getInt("maximumIterationCount");
    double convergenceThreshold = config.getDouble("convergenceThreshold");
    double uncertaintyProbabilityPercentile = config.getDouble("uncertaintyProbabilityPercentile");
    ScalingFactorType scalingFactorType = ScalingFactorType
        .valueOf(config.getString("scalingFactorType"));
    int kWeight = config.getInt("kWeight");
    double aprioriVariance = config.getDouble("aprioriVariance");
    int minimumNumberOfObservations = config.getInt("minimumNumberOfObservations");
    boolean constrainLatitudeParameter = config.getBoolean("constrainLatitudeParameter");
    boolean constrainLongitudeParameter = config.getBoolean("constrainLongitudeParameter");
    boolean constrainDepthParameter = config.getBoolean("constrainDepthParameter");
    boolean constrainTimeParameter = config.getBoolean("constrainTimeParameter");

    String earthModel = config.getString("earthModel");

    Objects.requireNonNull(earthModel,
        "Cannot create EventLocationConfigurationGeigers with null earthModel");

    boolean applyTravelTimeCorrections = config.getBoolean("applyTravelTimeCorrections");
    LocationRestraint.Builder locationRestraintBuilder = new LocationRestraint.Builder();

    if (constrainLatitudeParameter) {
      locationRestraintBuilder.setLatitudeRestraint(config.getDouble("constrainLatitudeValue"));
    }
    if (constrainLongitudeParameter) {
      locationRestraintBuilder.setLongitudeRestraint(config.getDouble("constrainLongitudeValue"));
    }
    if (constrainDepthParameter) {
      if (config.containsKey("constrainDepthToSurface")) {
        locationRestraintBuilder.setDepthRestraintAtSurface();
      } else {
        locationRestraintBuilder.setDepthRestraint(config.getDouble("constrainDepthValue"));
      }
    }
    if (constrainTimeParameter) {
      locationRestraintBuilder
          .setTimeRestraint(Instant.parse(config.getString("constrainTimeValue")));
    }
    return EventLocationDefinitionApacheLm.create(
        maximumIterationCount,
        convergenceThreshold,
        uncertaintyProbabilityPercentile,
        earthModel,
        applyTravelTimeCorrections,
        scalingFactorType,
        kWeight,
        aprioriVariance,
        minimumNumberOfObservations,
        List.of(locationRestraintBuilder.build()));
  }
}
