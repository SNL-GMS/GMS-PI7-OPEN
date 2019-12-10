package gms.core.signaldetection.association.eventredundancy.plugins;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class ArrivalQualityEventCriterionDefinitionTests {

  @Test
  void testSerialization() throws IOException {

    ArrivalQualityEventCriterionDefinition definition = ArrivalQualityEventCriterionDefinition.create(
        1.0,
        2.0,
        3.0,
        4.0
    );

    TestUtilities.testSerialization(definition, ArrivalQualityEventCriterionDefinition.class);

  }

}
