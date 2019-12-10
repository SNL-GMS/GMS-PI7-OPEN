package gms.core.signaldetection.association.plugins.implementations.redundancy;

import gms.core.signaldetection.association.CandidateEvent;
import gms.core.signaldetection.association.eventredundancy.plugins.EventRedundancyRemoval;
import gms.core.signaldetection.association.eventredundancy.plugins.EventRedundancyRemovalDefinition;
import gms.core.signaldetection.association.eventredundancy.plugins.EventRedundancyRemovalPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.pluginregistry.Name;
import gms.shared.mechanisms.pluginregistry.Version;
import java.util.Collection;
import java.util.Set;

@Name("defaultRedundancyRemovalPlugin")
@Version("1.0.0")
public class DefaultRedundancyRemovalPlugin implements EventRedundancyRemovalPlugin {

  DefaultRedundancyRemovalDelegate delegate = new DefaultRedundancyRemovalDelegate();

  @Override
  public void initialize() {
    this.delegate.initialize();
  }

  @Override
  public Set<CandidateEvent> reduce(Collection<CandidateEvent> events,
      EventRedundancyRemovalDefinition definition) {

    //TODO: Implement whatever else is needed to call the delegate.

    return delegate.reduce(events, definition);
  }
}
