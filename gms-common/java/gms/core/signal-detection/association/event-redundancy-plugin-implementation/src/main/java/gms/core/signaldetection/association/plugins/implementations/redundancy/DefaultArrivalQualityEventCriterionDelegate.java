package gms.core.signaldetection.association.plugins.implementations.redundancy;

import gms.core.signaldetection.association.CandidateEvent;
import gms.core.signaldetection.association.eventredundancy.plugins.ArrivalQualityEventCriterionDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;

public class DefaultArrivalQualityEventCriterionDelegate {

  public DoubleValue calculate(CandidateEvent candidateEvent,
      ArrivalQualityEventCriterionDefinition definition) {

    return DoubleValue.from(0.0, 0.0, Units.UNITLESS);
  }

}
