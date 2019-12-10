package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.Test;

public class ReferenceResponseTest {

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.response, ReferenceResponse.class);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceResponse.class);
  }

  @Test
  public void testReferenceResponseCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceResponse.class, "create",
        TestFixtures.channel.getEntityId(),
        TestFixtures.responseType,
        TestFixtures.responseData,
        TestFixtures.responseUnits,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.comment);
  }

  @Test
  public void testReferenceResponseFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceResponse.class, "from",
        TestFixtures.responseId,
        TestFixtures.channel.getEntityId(),
        TestFixtures.responseType,
        TestFixtures.responseData,
        TestFixtures.responseUnits,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.comment);
  }


  /**
   * Test that arguments are saved correctly.
   */
  @Test
  public void testReferenceResponseCreate() {
    ReferenceResponse response = ReferenceResponse.create(
        TestFixtures.channel.getEntityId(),
        TestFixtures.responseType,
        TestFixtures.responseData,
        TestFixtures.responseUnits,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.comment);
    final UUID expectedId = UUID.nameUUIDFromBytes((
        response.getChannelId() + response.getResponseType()
            + new String(response.getResponseData())
            + response.getUnits() + response.getActualTime() + response.getSystemTime())
        .getBytes(StandardCharsets.UTF_16LE));
    assertEquals(expectedId, response.getId());
    assertEquals(TestFixtures.channel.getEntityId(), response.getChannelId());
    assertEquals(TestFixtures.responseType, response.getResponseType());
    assertEquals(TestFixtures.responseData, response.getResponseData());
    assertEquals(TestFixtures.responseUnits, response.getUnits());
    assertEquals(TestFixtures.actualTime, response.getActualTime());
    assertEquals(TestFixtures.systemTime, response.getSystemTime());
    assertEquals(TestFixtures.source, response.getInformationSource());
    assertEquals(TestFixtures.comment, response.getComment());
  }


  /**
   * Test that arguments are saved correctly.
   */
  @Test
  public void testReferenceResponseFrom() {
    ReferenceResponse response = ReferenceResponse.from(
        TestFixtures.responseId,
        TestFixtures.channel.getEntityId(),
        TestFixtures.responseType,
        TestFixtures.responseData,
        TestFixtures.responseUnits,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.source,
        TestFixtures.comment);
    assertEquals(TestFixtures.responseId, response.getId());
    assertEquals(TestFixtures.channel.getEntityId(), response.getChannelId());
    assertEquals(TestFixtures.responseType, response.getResponseType());
    assertEquals(TestFixtures.responseData, response.getResponseData());
    assertEquals(TestFixtures.responseUnits, response.getUnits());
    assertEquals(TestFixtures.actualTime, response.getActualTime());
    assertEquals(TestFixtures.systemTime, response.getSystemTime());
    assertEquals(TestFixtures.source, response.getInformationSource());
    assertEquals(TestFixtures.comment, response.getComment());
  }

}
