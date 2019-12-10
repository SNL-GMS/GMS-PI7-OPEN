package gms.core.signaldetection.association.eventredundancy.plugins;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class WeightedEventCriteriaCalculationDefinitionTests {

  @Test
  void testSerialization() throws IOException {
    WeightedEventCriteriaCalculationDefinition definition = WeightedEventCriteriaCalculationDefinition
        .create(
            1.0,
            2.0,
            3.0,
            4.0,
            5.0,
            6.0,
            7.0
        );

    TestUtilities.testSerialization(definition, WeightedEventCriteriaCalculationDefinition.class);
  }

}
