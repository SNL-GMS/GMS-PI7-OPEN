package gms.core.signaldetection.association.plugins.implementations.redundancy;

import gms.core.signaldetection.association.CandidateEvent;
import gms.core.signaldetection.association.eventredundancy.plugins.ArrivalQualityEventCriterionDefinition;
import gms.core.signaldetection.association.eventredundancy.plugins.ArrivalQualityEventCriterionPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.pluginregistry.Name;
import gms.shared.mechanisms.pluginregistry.Version;

@Name("defaultArrivalQualityCalculationPlugin")
@Version("1.0.0")
public class DefaultArrivalQualityEventCriterionPlugin implements
    ArrivalQualityEventCriterionPlugin {

  @Override
  public DoubleValue calculate(CandidateEvent candidateEvent,
      ArrivalQualityEventCriterionDefinition definition) {
    return null;
  }
}
