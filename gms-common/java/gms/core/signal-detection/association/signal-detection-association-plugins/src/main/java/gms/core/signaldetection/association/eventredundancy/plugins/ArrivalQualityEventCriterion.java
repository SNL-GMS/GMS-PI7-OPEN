package gms.core.signaldetection.association.eventredundancy.plugins;

import gms.core.signaldetection.association.CandidateEvent;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;

/**
 * Perform arrivale quality calculation
 */
public interface ArrivalQualityEventCriterion {

  DoubleValue calculate(CandidateEvent candidateEvent,
      ArrivalQualityEventCriterionDefinition definition);

}
