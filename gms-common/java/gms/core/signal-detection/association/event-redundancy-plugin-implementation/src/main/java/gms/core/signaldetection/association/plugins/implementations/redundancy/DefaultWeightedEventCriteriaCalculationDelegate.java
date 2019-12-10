package gms.core.signaldetection.association.plugins.implementations.redundancy;

import gms.core.signaldetection.association.CandidateEvent;
import gms.core.signaldetection.association.eventredundancy.plugins.WeightedEventCriteriaCalculationDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import java.util.Optional;

/**
 * Plugin delegate for default plugin for calculationg weighted criteria
 */
public class DefaultWeightedEventCriteriaCalculationDelegate {

  //
  //  Enum values that map directly to feature measurement types and return the
  //  proper weight given the definiion. Makes calculate code prettier and more compact
  //  and one can just add more types here.
  //
  private enum WeightType {

    ARRIVAL_TIME() {
      @Override
      double multiplier(WeightedEventCriteriaCalculationDefinition definition) {
        return definition.getPrimaryTimeWeight();
      }
    },

    SOURCE_TO_RECEIVER_AZIMUTH() {
      @Override
      double multiplier(WeightedEventCriteriaCalculationDefinition definition) {
        return definition.getArrayAzimuthWeight();
      }
    },

    SLOWNESS() {
      @Override
      double multiplier(WeightedEventCriteriaCalculationDefinition definition) {
        return definition.getArraySlowWeight();
      }
    };

    abstract double multiplier(
        WeightedEventCriteriaCalculationDefinition definition
        /* TODO: Paramter indicting 3-compoent stations and phase type will go here in the future */
    );

    static Optional<WeightType> optionalValueOf(String name) {
      try {
        return Optional.of(valueOf(name));
      } catch (IllegalArgumentException e) {
        return Optional.empty();
      }
    }

  }

  /**
   * Calculate quality of event based on some weighted creteria
   *
   * This implemention loops through the corroborating SDHs and does a count of slowness, azimuth
   * and arrival time measurements. The weighted some of counts of these measurments is the the
   * quality of the event.
   *
   * @param candidateEvent event to qualify
   * @param definition definition containing weights to apply to the different types of counts
   * @return quality of event
   */
  public DoubleValue calculate(CandidateEvent candidateEvent,
      WeightedEventCriteriaCalculationDefinition definition) {

    return DoubleValue.from(
        candidateEvent.getCorroboratingSet().stream().reduce(0.0, (previousOuter, sdh) ->
                previousOuter + sdh.getFeatureMeasurements().stream()
                    .reduce(0.0, (previousInner, featureMeasurement) ->
                            previousInner + WeightType
                                .optionalValueOf(featureMeasurement.getFeatureMeasurementTypeName())
                                .map(weightType -> weightType.multiplier(definition)).orElse(0.0),
                        (fmResult1, fmResult2) -> fmResult1 + fmResult2)
            , (sdhResult1, sdhResult2) -> sdhResult1 + sdhResult2), 0.0, Units.UNITLESS);

  }

}
