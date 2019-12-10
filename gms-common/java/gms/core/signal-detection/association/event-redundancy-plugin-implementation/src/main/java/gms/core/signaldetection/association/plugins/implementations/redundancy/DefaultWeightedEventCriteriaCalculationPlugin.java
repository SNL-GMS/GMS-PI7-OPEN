package gms.core.signaldetection.association.plugins.implementations.redundancy;

import gms.core.signaldetection.association.CandidateEvent;
import gms.core.signaldetection.association.eventredundancy.plugins.WeightedEventCriteriaCalculationDefinition;
import gms.core.signaldetection.association.eventredundancy.plugins.WeightedEventCriteriaCalculationPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.pluginregistry.Name;
import gms.shared.mechanisms.pluginregistry.Version;

@Name("defaultWeightedEventCriteriaCalculationPlugin")
@Version("1.0.0")
public class DefaultWeightedEventCriteriaCalculationPlugin implements
    WeightedEventCriteriaCalculationPlugin {

  DefaultWeightedEventCriteriaCalculationDelegate delegate = new DefaultWeightedEventCriteriaCalculationDelegate();

  @Override
  public DoubleValue calculate(CandidateEvent candidateEvent,
      WeightedEventCriteriaCalculationDefinition definition) {

    return delegate.calculate(candidateEvent, definition);
  }
}
