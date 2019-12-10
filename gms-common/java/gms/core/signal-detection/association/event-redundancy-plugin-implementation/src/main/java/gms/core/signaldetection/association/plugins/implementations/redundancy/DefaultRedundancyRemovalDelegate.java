package gms.core.signaldetection.association.plugins.implementations.redundancy;

import gms.core.signaldetection.association.CandidateEvent;
import gms.core.signaldetection.association.eventredundancy.plugins.EventRedundancyRemovalDefinition;
import gms.core.signaldetection.association.eventredundancy.plugins.WeightedEventCriteriaCalculation;
import gms.core.signaldetection.association.eventredundancy.plugins.WeightedEventCriteriaCalculationDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import java.util.Collection;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Plugin delegate for default plugin for removing redundant candidate events
 */
public class DefaultRedundancyRemovalDelegate {

  private WeightedEventCriteriaCalculation weightedEventCriteriaCalculation;

  private PluginRegistry pluginRegistry = PluginRegistry.getRegistry();

  /**
   * Load weighted event criteria calculation plugin. Configure the weighted calculation plugin.
   */
  public void initialize() {

    pluginRegistry.loadAndRegister();

    PluginInfo pluginInfo = getPluginInfoFromConfiguration();

    weightedEventCriteriaCalculation = pluginRegistry.lookup(
        pluginInfo,
        WeightedEventCriteriaCalculation.class
    ).orElseThrow(() -> new IllegalStateException(
        "Plugin " + pluginInfo.getName() + "(v" + pluginInfo.getVersion() + ") not found!"));

  }

  /**
   * Given a collection of events, returns a Set of candidate events such that "reducdant" candidate
   * events are removed for some definition of "redundant.
   *
   * In other words, given some "set" E of events, ensure that all equivalence classes over some
   * equivalence relation contain only one member. The resulting set is "reduced".
   *
   * @param events collection of events to reduce
   * @return Set of events with no redundancy.
   */
  public Set<CandidateEvent> reduce(Collection<CandidateEvent> events,
      EventRedundancyRemovalDefinition definition) {

    HashMap<SignalDetectionHypothesis, PriorityQueue<CandidateEvent>> sdhCandidateEventMap = new HashMap<>();

    //
    // Go through the list of events and add them to a the sdhCandidateMap, keyed by their driver SDHs
    //
    events.forEach(candidateEvent -> {
      sdhCandidateEventMap
          .putIfAbsent(candidateEvent.getDriverSdh(), new PriorityQueue<>(
              //
              // Comparator gets the value calculated from the weightedEventCriteriaCalculation
              // so that candidate events are sorted (in reverse of natural order) in the
              // PriorityQueue.
              //
              (candidate1, candidate2) -> -Double.compare(
                  weightedEventCriteriaCalculation
                      .calculate(candidate1,
                          definition.getWeightedEventCriteriaCalculationDefinition()).getValue(),
                  weightedEventCriteriaCalculation
                      .calculate(candidate2,
                          definition.getWeightedEventCriteriaCalculationDefinition()).getValue())));
      sdhCandidateEventMap.get(candidateEvent.getDriverSdh()).add(candidateEvent);
    });

    //
    // Go through each entry set in the map and find the first candidate event in the list in the
    // entry set.
    //
    return sdhCandidateEventMap.entrySet().stream().map(entrySet -> entrySet.getValue().peek())
        .collect(Collectors.toSet());

  }

  /**
   * Load plugin name and version for weighted calculation plugin.
   */
  private PluginInfo getPluginInfoFromConfiguration() {

    Configuration config;

    try {
      config = new PropertiesConfiguration(
          "gms/core/signaldetection/association/plugins/implementations/redundancy/application.properties");
    } catch (ConfigurationException e) {
      throw new RuntimeException(e);
    }

    return PluginInfo.from(
        config.getString("pluginName"),
        config.getString("pluginVersion")
    );

  }

}
