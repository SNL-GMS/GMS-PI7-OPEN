package gms.core.signaldetection.association.eventredundancy.plugins;

import gms.core.signaldetection.association.CandidateEvent;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import java.util.Collection;
import java.util.Set;

/**
 * Specifies redundancy removal functionality.
 */
public interface EventRedundancyRemoval {

  /**
   * initialize plugin
   *
   */
  void initialize();

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
  Set<CandidateEvent> reduce(Collection<CandidateEvent> events,
      EventRedundancyRemovalDefinition definition);

}
