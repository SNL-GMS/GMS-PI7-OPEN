package gms.core.featureprediction.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class StreamingFeaturePredictionsForLocationSolutionAndChannelTests {

  @Test
  void testSerialization() throws IOException {
    TestUtilities.testSerialization(
        TestFixtures.streamingFeaturePredictionsForLocationSolutionAndChannel,
        StreamingFeaturePredictionsForLocationSolutionAndChannel.class);
  }

}
