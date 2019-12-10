package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.Test;

public class ReferenceCalibrationTest {

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.calibration, ReferenceCalibration.class);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceCalibration.class);
  }

  @Test
  public void testReferenceCalibrationCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceCalibration.class, "create",
        TestFixtures.channel.getEntityId(),
        TestFixtures.calibrationInterval,
        TestFixtures.calibrationFactor,
        TestFixtures.calibrationFactorError,
        TestFixtures.calibrationPeriod,
        TestFixtures.calibrationTimeShift,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.comment);
  }

  @Test
  public void testReferenceCalibrationFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceCalibration.class, "from",
        TestFixtures.calibrationId,
        TestFixtures.channel.getEntityId(),
        TestFixtures.calibrationInterval,
        TestFixtures.calibrationFactor,
        TestFixtures.calibrationFactorError,
        TestFixtures.calibrationPeriod,
        TestFixtures.calibrationTimeShift,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.comment);
  }

  /**
   * Test that arguments are saved correctly.
   */
  @Test
  public void testReferenceCalibrationCreate() {
    ReferenceCalibration calib = ReferenceCalibration.create(
        TestFixtures.channel.getEntityId(),
        TestFixtures.calibrationInterval,
        TestFixtures.calibrationFactor,
        TestFixtures.calibrationFactorError,
        TestFixtures.calibrationPeriod,
        TestFixtures.calibrationTimeShift,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.comment);
    final UUID expectedId = UUID.nameUUIDFromBytes(
        (calib.getChannelId().toString() + calib.getCalibrationInterval()
            + calib.getCalibrationFactor() + calib.getCalibrationFactorError()
            + calib.getCalibrationPeriod() + calib.getTimeShift()
            + calib.getActualTime() + calib.getSystemTime())
            .getBytes(StandardCharsets.UTF_16LE));
    assertEquals(expectedId, calib.getId());
    assertEquals(TestFixtures.channel.getEntityId(), calib.getChannelId());
    assertEquals(TestFixtures.calibrationInterval, calib.getCalibrationInterval(),
        TestFixtures.precision);
    assertEquals(TestFixtures.calibrationFactor, calib.getCalibrationFactor(),
        TestFixtures.precision);
    assertEquals(TestFixtures.calibrationFactorError, calib.getCalibrationFactorError(),
        TestFixtures.precision);
    assertEquals(TestFixtures.calibrationPeriod, calib.getCalibrationPeriod(),
        TestFixtures.precision);
    assertEquals(TestFixtures.calibrationTimeShift, calib.getTimeShift(), TestFixtures.precision);
    assertEquals(TestFixtures.actualTime, calib.getActualTime());
    assertEquals(TestFixtures.systemTime, calib.getSystemTime());
    assertEquals(TestFixtures.source, calib.getInformationSource());
  }


  /**
   * Test that arguments are saved correctly.
   */
  @Test
  public void testReferenceCalibrationFrom() {
    ReferenceCalibration calib = ReferenceCalibration.from(
        TestFixtures.calibrationId,
        TestFixtures.channel.getEntityId(),
        TestFixtures.calibrationInterval,
        TestFixtures.calibrationFactor,
        TestFixtures.calibrationFactorError,
        TestFixtures.calibrationPeriod,
        TestFixtures.calibrationTimeShift,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.comment);
    assertEquals(TestFixtures.calibrationId, calib.getId());
    assertEquals(TestFixtures.channel.getEntityId(), calib.getChannelId());
    assertEquals(TestFixtures.calibrationInterval, calib.getCalibrationInterval(),
        TestFixtures.precision);
    assertEquals(TestFixtures.calibrationFactor, calib.getCalibrationFactor(),
        TestFixtures.precision);
    assertEquals(TestFixtures.calibrationFactorError, calib.getCalibrationFactorError(),
        TestFixtures.precision);
    assertEquals(TestFixtures.calibrationPeriod, calib.getCalibrationPeriod(),
        TestFixtures.precision);
    assertEquals(TestFixtures.calibrationTimeShift, calib.getTimeShift(), TestFixtures.precision);
    assertEquals(TestFixtures.actualTime, calib.getActualTime());
    assertEquals(TestFixtures.systemTime, calib.getSystemTime());
    assertEquals(TestFixtures.source, calib.getInformationSource());
  }

}
