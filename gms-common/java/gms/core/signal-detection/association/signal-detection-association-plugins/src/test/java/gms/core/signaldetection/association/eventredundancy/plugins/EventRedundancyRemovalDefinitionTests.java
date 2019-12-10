package gms.core.signaldetection.association.eventredundancy.plugins;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;

public class EventRedundancyRemovalDefinitionTests {

  private static WeightedEventCriteriaCalculationDefinition weightDefinition = WeightedEventCriteriaCalculationDefinition
      .create(
          15,
          15,
          15,
          15,
          15,
          25,
          0
      );

  private static ArrivalQualityEventCriterionDefinition arrivalDefinition = ArrivalQualityEventCriterionDefinition
      .create(
          1.0,
          2.0,
          3.0,
          4.0
      );

  private static EventRedundancyRemovalDefinition definition = EventRedundancyRemovalDefinition
      .create(
          weightDefinition,
          arrivalDefinition
      );

  @Test
  void testSerialization() throws IOException {
    TestUtilities.testSerialization(definition, EventRedundancyRemovalDefinition.class);
  }

}
