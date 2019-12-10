package gms.core.signaldetection.association.plugins.implementations.redundancy;

import gms.core.signaldetection.association.CandidateEvent;
import gms.core.signaldetection.association.eventredundancy.plugins.ArrivalQualityEventCriterionDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DefaultArrivalQualityEventCriterionDelegateTests {

  @Test
  void testReturnsValue() {
    CandidateEvent candidateEvent = Mockito.mock(CandidateEvent.class);

    DefaultArrivalQualityEventCriterionDelegate delegate = new DefaultArrivalQualityEventCriterionDelegate();

    DoubleValue doubleValue = delegate.calculate(candidateEvent,
        ArrivalQualityEventCriterionDefinition.create(
            1.0,
            2.0,
            3.0,
            4.0
        ));

    //TODO: Actual tests for implementation

    Assertions.assertEquals(doubleValue.getValue(), 0.0);
    Assertions.assertEquals(doubleValue.getStandardDeviation(), 0.0);
    Assertions.assertEquals(doubleValue.getUnits(), Units.UNITLESS);

  }

}
