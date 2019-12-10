package gms.core.signaldetection.association.plugins.implementations.globalgrid;

import gms.core.signaldetection.association.plugins.SignalDetectionAssociatorDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class GlobalGridSignalDetectionAssociatorDelegateTests {

  GlobalGridSignalDetectionAssociatorDelegate delegate;
  TesseractModelGA modelGA;
  SignalDetectionAssociatorDefinition definition;

//  @BeforeEach
//  void setUp() {
//    delegate = new GlobalGridSignalDetectionAssociatorDelegate();
//    modelGA = Mockito.mock(TesseractModelGA.class);
//    definition = Mockito.mock(SignalDetectionAssociatorDefinition.class);
//  }

//  @Test
//  void testPassingEmptyListOfSDHsReturnEmptyLists() {
//    Pair<Set<SignalDetectionHypothesis>, Set<EventHypothesis>> results = delegate.associate(
//        Set.of(),
//        Set.of(),
//        definition, modelGA);
//
//    Assertions.assertEquals(List.of().size(), results.getKey().size());
//    Assertions.assertEquals(List.of().size(), results.getValue().size());
//  }
}
