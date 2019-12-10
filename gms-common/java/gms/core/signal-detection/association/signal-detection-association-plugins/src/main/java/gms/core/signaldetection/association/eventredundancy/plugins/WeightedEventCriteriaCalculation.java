package gms.core.signaldetection.association.eventredundancy.plugins;

import gms.core.signaldetection.association.CandidateEvent;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;

/**
 * Specify functionality for weighted event criteria calculation
 */
public interface WeightedEventCriteriaCalculation {

  /**
   * Calculate quality of event based on some weighted creteria
   *
   * @param candidateEvent event to qualify
   * @return quality of event
   */
  DoubleValue calculate(CandidateEvent candidateEvent,
      WeightedEventCriteriaCalculationDefinition definition);

}
