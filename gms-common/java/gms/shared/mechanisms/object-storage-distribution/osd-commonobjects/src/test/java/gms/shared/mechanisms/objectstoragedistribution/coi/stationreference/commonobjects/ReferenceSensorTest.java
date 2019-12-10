package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.Test;

public class ReferenceSensorTest {

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.sensor, ReferenceSensor.class);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceSensor.class);
  }

  @Test
  public void testReferenceSensorCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceSensor.class, "create",
        TestFixtures.channel.getEntityId(),
        TestFixtures.instrumentManufacturer,
        TestFixtures.instrumentModel,
        TestFixtures.serialNumber,
        TestFixtures.numberOfComponents,
        TestFixtures.cornerPeriod,
        TestFixtures.lowPassband,
        TestFixtures.highPassband,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.comment);
  }

  @Test
  public void testReferenceSensorFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceSensor.class, "from",
        TestFixtures.sensorId,
        TestFixtures.channel.getEntityId(),
        TestFixtures.instrumentManufacturer,
        TestFixtures.instrumentModel,
        TestFixtures.serialNumber,
        TestFixtures.numberOfComponents,
        TestFixtures.cornerPeriod,
        TestFixtures.lowPassband,
        TestFixtures.highPassband,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.comment);
  }


  /**
   * Test that arguments are saved correctly.
   */
  @Test
  public void testReferenceSensorCreate() {
    ReferenceSensor sensor = ReferenceSensor.create(
        TestFixtures.channel.getEntityId(),
        TestFixtures.instrumentManufacturer,
        TestFixtures.instrumentModel,
        TestFixtures.serialNumber,
        TestFixtures.numberOfComponents,
        TestFixtures.cornerPeriod,
        TestFixtures.lowPassband,
        TestFixtures.highPassband,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.comment);
    final UUID expectedId = UUID.nameUUIDFromBytes(
        (sensor.getChannelId() + sensor.getInstrumentManufacturer()
            + sensor.getInstrumentModel() + sensor.getSerialNumber()
            + sensor.getNumberOfComponents() + sensor.getCornerPeriod()
            + sensor.getLowPassband() + sensor.getHighPassband()
            + sensor.getActualTime() + sensor.getSystemTime())
            .getBytes(StandardCharsets.UTF_16LE));
    assertEquals(expectedId, sensor.getId());
    assertEquals(TestFixtures.channel.getEntityId(), sensor.getChannelId());
    assertEquals(TestFixtures.instrumentManufacturer, sensor.getInstrumentManufacturer());
    assertEquals(TestFixtures.instrumentModel, sensor.getInstrumentModel());
    assertEquals(TestFixtures.serialNumber, sensor.getSerialNumber());
    assertEquals(TestFixtures.numberOfComponents, sensor.getNumberOfComponents());
    assertEquals(TestFixtures.cornerPeriod, sensor.getCornerPeriod(), TestFixtures.precision);
    assertEquals(TestFixtures.lowPassband, sensor.getLowPassband(), TestFixtures.precision);
    assertEquals(TestFixtures.highPassband, sensor.getHighPassband(), TestFixtures.precision);
    assertEquals(TestFixtures.actualTime, sensor.getActualTime());
    assertEquals(TestFixtures.systemTime, sensor.getSystemTime());
    assertEquals(TestFixtures.source, sensor.getInformationSource());
    assertEquals(TestFixtures.comment, sensor.getComment());
  }


  /**
   * Test that arguments are saved correctly.
   */
  @Test
  public void testReferenceSensorFrom() {
    ReferenceSensor sensor = ReferenceSensor.from(
        TestFixtures.sensorId,
        TestFixtures.channel.getEntityId(),
        TestFixtures.instrumentManufacturer,
        TestFixtures.instrumentModel,
        TestFixtures.serialNumber,
        TestFixtures.numberOfComponents,
        TestFixtures.cornerPeriod,
        TestFixtures.lowPassband,
        TestFixtures.highPassband,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.comment);
    assertEquals(TestFixtures.sensorId, sensor.getId());
    assertEquals(TestFixtures.channel.getEntityId(), sensor.getChannelId());
    assertEquals(TestFixtures.instrumentManufacturer, sensor.getInstrumentManufacturer());
    assertEquals(TestFixtures.instrumentModel, sensor.getInstrumentModel());
    assertEquals(TestFixtures.serialNumber, sensor.getSerialNumber());
    assertEquals(TestFixtures.numberOfComponents, sensor.getNumberOfComponents());
    assertEquals(TestFixtures.cornerPeriod, sensor.getCornerPeriod(), TestFixtures.precision);
    assertEquals(TestFixtures.lowPassband, sensor.getLowPassband(), TestFixtures.precision);
    assertEquals(TestFixtures.highPassband, sensor.getHighPassband(), TestFixtures.precision);
    assertEquals(TestFixtures.actualTime, sensor.getActualTime());
    assertEquals(TestFixtures.systemTime, sensor.getSystemTime());
    assertEquals(TestFixtures.source, sensor.getInformationSource());
    assertEquals(TestFixtures.comment, sensor.getComment());
  }

}
