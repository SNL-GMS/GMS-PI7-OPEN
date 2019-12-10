package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.SignalDetectionTestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.FirstMotionMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link FeatureMeasurement} factory creation
 */
public class FeatureMeasurementTests {

  private final UUID id = UUID.randomUUID();
  private final UUID channelSegmentId = UUID.randomUUID();

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testSerializationPhaseMeasurement() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.phaseFeatureMeasurement, FeatureMeasurement.class);
  }

  @Test
  public void testSerializationFirstMotionMeasurement() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.firstMotionFeatureMeasurement, FeatureMeasurement.class);
  }

  @Test
  public void testSerializationNumericalMeasurement() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.arrivalTimeFeatureMeasurement, FeatureMeasurement.class);
  }

  @Test
  public void testSerializationAmplitudeMeasurement() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.amplitudeFeatureMeasurement, FeatureMeasurement.class);
  }

  @Test
  public void testSerializationInstantMeasurement() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.instantFeatureMeasurement, FeatureMeasurement.class);
  }
  
  @Test
  public void testSerializationBaseMeasurementValue() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.standardDoubleValue, DoubleValue.class);
    TestUtilities.testSerialization(SignalDetectionTestFixtures.arrivalTimeMeasurement, InstantValue.class);
    TestUtilities.testSerialization(SignalDetectionTestFixtures.phaseMeasurement, PhaseTypeMeasurementValue.class);
    TestUtilities.testSerialization(SignalDetectionTestFixtures.firstMotionMeasurement, FirstMotionMeasurementValue.class);
    TestUtilities.testSerialization(SignalDetectionTestFixtures.amplitudeMeasurement, AmplitudeMeasurementValue.class);
    TestUtilities.testSerialization(SignalDetectionTestFixtures.instantMeasurement, InstantValue.class);
  }
}
