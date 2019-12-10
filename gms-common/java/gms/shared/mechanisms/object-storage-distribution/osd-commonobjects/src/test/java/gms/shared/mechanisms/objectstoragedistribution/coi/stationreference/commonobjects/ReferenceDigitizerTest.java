package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.Test;

public class ReferenceDigitizerTest {

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.digitizer, ReferenceDigitizer.class);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceDigitizer.class);
  }

  @Test
  public void testReferenceDigitizerCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceDigitizer.class, "create",
        TestFixtures.digitName,
        TestFixtures.digitManufacturer,
        TestFixtures.digitModel,
        TestFixtures.digitSerial,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.digitComment, "desc");
  }

  @Test
  public void testReferenceDigitizerFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceDigitizer.class, "from",
        TestFixtures.digitizerId, UUID.randomUUID(),
        TestFixtures.digitName,
        TestFixtures.digitManufacturer,
        TestFixtures.digitModel,
        TestFixtures.digitSerial,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source, "desc",
        TestFixtures.digitComment);
  }

  @Test
  public void testReferenceDigitizerCreate() {
    String desc = "description";
    ReferenceDigitizer d = ReferenceDigitizer.create(
        TestFixtures.digitName,
        TestFixtures.digitManufacturer,
        TestFixtures.digitModel,
        TestFixtures.digitSerial,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.digitComment, desc);
    UUID expectedEntityId = UUID.nameUUIDFromBytes(
        (d.getManufacturer() + d.getModel() + d.getSerialNumber())
        .getBytes(StandardCharsets.UTF_16LE));
    assertEquals(expectedEntityId, d.getEntityId());
    UUID expectedVersionId = UUID.nameUUIDFromBytes(
        (d.getName() + d.getManufacturer() + d.getModel() + d.getSerialNumber()
            + d.getActualChangeTime()).getBytes(StandardCharsets.UTF_16LE));
    assertEquals(expectedVersionId, d.getVersionId());
    assertEquals(TestFixtures.digitName, d.getName());
    assertEquals(TestFixtures.digitManufacturer, d.getManufacturer());
    assertEquals(TestFixtures.digitModel, d.getModel());
    assertEquals(TestFixtures.digitSerial, d.getSerialNumber());
    assertEquals(TestFixtures.actualTime, d.getActualChangeTime());
    assertEquals(TestFixtures.systemTime, d.getSystemChangeTime());
    assertEquals(TestFixtures.source, d.getInformationSource());
    assertEquals(TestFixtures.digitComment, d.getComment());
    assertEquals(desc, d.getDescription());
  }

  @Test
  public void testReferenceDigitizerFrom() {
    UUID versionId = UUID.randomUUID();
    String desc = "description";
    ReferenceDigitizer digitizer = ReferenceDigitizer.from(
        TestFixtures.digitizerId, versionId,
        TestFixtures.digitName,
        TestFixtures.digitManufacturer,
        TestFixtures.digitModel,
        TestFixtures.digitSerial,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.digitComment, desc);
    assertEquals(TestFixtures.digitizerId, digitizer.getEntityId());
    assertEquals(versionId, digitizer.getVersionId());
    assertEquals(TestFixtures.digitName, digitizer.getName());
    assertEquals(TestFixtures.digitManufacturer, digitizer.getManufacturer());
    assertEquals(TestFixtures.digitModel, digitizer.getModel());
    assertEquals(TestFixtures.digitSerial, digitizer.getSerialNumber());
    assertEquals(TestFixtures.actualTime, digitizer.getActualChangeTime());
    assertEquals(TestFixtures.systemTime, digitizer.getSystemChangeTime());
    assertEquals(TestFixtures.source, digitizer.getInformationSource());
    assertEquals(TestFixtures.digitComment, digitizer.getComment());
    assertEquals(desc, digitizer.getDescription());
  }
}
