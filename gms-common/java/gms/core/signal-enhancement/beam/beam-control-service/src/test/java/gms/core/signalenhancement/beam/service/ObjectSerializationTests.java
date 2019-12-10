package gms.core.signalenhancement.beam.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.core.signalenhancement.beam.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ProcessingGroupDescriptor;
import gms.core.signalenhancement.beam.core.BeamStreamingCommand;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ObjectSerializationTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  /**
   * A class that Jackson can't serialize.  Used to test exception conditions.
   */
  private class Foo {

    private final int param;

    private Foo(int param) {
      this.param = param;
    }
  }
  
  @Test
  public void testBeamStreamingCommandJsonSerialization() {
    final BeamStreamingCommand cmd = TestFixtures.getBeamStreamingCommand();

    byte[] messagePack = ObjectSerialization.writeMessagePack(cmd);
    assertNotNull(messagePack);
    assertTrue(messagePack.length > 0);

    BeamStreamingCommand deserialized = ObjectSerialization
        .readMessagePack(messagePack, BeamStreamingCommand.class);
    assertNotNull(deserialized);
    assertEquals(cmd, deserialized);
  }

  @Test
  public void testBeamStreamingCommandMessagePackSerialization() {
    final BeamStreamingCommand cmd = TestFixtures.getBeamStreamingCommand();

    byte[] messagePack = ObjectSerialization.writeMessagePack(cmd);
    assertNotNull(messagePack);
    assertTrue(messagePack.length > 0);

    BeamStreamingCommand deserialized = ObjectSerialization
        .readMessagePack(messagePack, BeamStreamingCommand.class);
    assertNotNull(deserialized);
    assertEquals(cmd, deserialized);
  }

  @Test
  public void testBeamClaimCheckDtoJsonSerialization() {
    // ProcessingGroupDescriptor has the same fields as the claim check.  Just
    // use the BeamClaimCheckCommandDto mixin to serialize the claim check.
    ProcessingGroupDescriptor command = TestFixtures.getProcessingGroupDescriptor();
    byte[] json = ObjectSerialization.writeJson(command);
    assertNotNull(json);
    assertTrue(json.length > 0);

    ProcessingGroupDescriptor deserialized = ObjectSerialization
        .readJson(json, ProcessingGroupDescriptor.class);
    assertNotNull(deserialized);
    assertEquals(command, deserialized);
  }

  @Test
  public void testJsonSerializationErrorExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unable to serialize object to json");
    ObjectSerialization.writeJson(new Foo(99));
  }

  @Test
  public void testJsonSerializeNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to serialize null to json");
    ObjectSerialization.writeJson(null);
  }

  @Test
  public void testJsonParseErrorExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unable to deserialize object from json");
    ObjectSerialization.readJson("{bad json}".getBytes(), Foo.class);
  }

  @Test
  public void testJsonParseNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize null json");
    ObjectSerialization.readJson((byte[])null, Foo.class);
  }

  @Test
  public void testJsonParseNullTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize to null class type");
    ObjectSerialization.readJson("{-100}".getBytes(), null);
  }

  @Test
  public void testJsonParseStringErrorExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unable to deserialize object from json");
    ObjectSerialization.readJson("{bad json}", Foo.class);
  }

  @Test
  public void testJsonParseStringNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize null json");
    ObjectSerialization.readJson((String)null, Foo.class);
  }

  @Test
  public void testJsonParseStringNullTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize to null class type");
    ObjectSerialization.readJson("{-100}", null);
  }

  @Test
  public void testJsonCollectionParseErrorExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unable to deserialize object from json");
    ObjectSerialization
        .readJsonCollection("{}", List.class, ChannelSegment.class, Waveform.class);
  }

  @Test
  public void testJsonCollectionParseNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize null json");
    ObjectSerialization.readJsonCollection(null, List.class, Foo.class, Foo.class);
  }

  @Test
  public void testJsonCollectionParseNullCollectionTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize to null collectionClass type");
    ObjectSerialization.readJsonCollection("{}", null, Foo.class, Foo.class);
  }

  @Test
  public void testJsonCollectionParseNullRawTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize to null rawClass type");
    ObjectSerialization.readJsonCollection("{}", List.class, null, Foo.class);
  }

  @Test
  public void testJsonCollectionParseNullParameterTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize to null parameterClass type");
    ObjectSerialization.readJsonCollection("{}", List.class, Foo.class,
        (Class<?>) null);
  }

  @Test
  public void testJsonCollectionParseNullParameterTypeArrayExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize to null parameterClass type");
    ObjectSerialization.readJsonCollection("{}", List.class, Foo.class,
        (Class<?>[]) null);
  }

  @Test
  public void testJsonCollectionParseNoParameterTypeExpectNullPointerException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unable to deserialize to empty parameterClass type");
    ObjectSerialization.readJsonCollection("{}", List.class, Foo.class);
  }

  @Test
  public void testMessagePackSerializationErrorExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unable to serialize object to MessagePack");
    ObjectSerialization.writeMessagePack(new Foo(99));
  }

  @Test
  public void testMessagePackSerializeNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to serialize null to MessagePack");
    ObjectSerialization.writeMessagePack(null);
  }

  @Test
  public void testMessagePackParseErrorExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unable to deserialize object from MessagePack");
    ObjectSerialization.readMessagePack(new byte[]{1}, Foo.class);
  }

  @Test
  public void testMessagePackParseNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize null MessagePack");
    ObjectSerialization.readMessagePack(null, Foo.class);
  }

  @Test
  public void testMessagePackParseNullTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize to null class type");
    ObjectSerialization.readMessagePack(new byte[]{1}, null);
  }

  @Test
  public void testMessagePackCollectionParseErrorExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unable to deserialize object from MessagePack");
    ObjectSerialization
        .readMessagePackCollection(new byte[]{1}, List.class, ChannelSegment.class, Waveform.class);
  }

  @Test
  public void testMessagePackCollectionParseNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize null MessagePack");
    ObjectSerialization.readMessagePackCollection(null, List.class, Foo.class, Foo.class);
  }

  @Test
  public void testMessagePackCollectionParseNullCollectionTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize to null collectionClass type");
    ObjectSerialization.readMessagePackCollection(new byte[]{1}, null, Foo.class, Foo.class);
  }

  @Test
  public void testMessagePackCollectionParseNullRawTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize to null rawClass type");
    ObjectSerialization.readMessagePackCollection(new byte[]{1}, List.class, null, Foo.class);
  }

  @Test
  public void testMessagePackCollectionParseNullParameterTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize to null parameterClass type");
    ObjectSerialization.readMessagePackCollection(new byte[]{1}, List.class, Foo.class,
        (Class<?>) null);
  }

  @Test
  public void testMessagePackCollectionParseNullParameterTypeArrayExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize to null parameterClass type");
    ObjectSerialization.readMessagePackCollection(new byte[]{1}, List.class, Foo.class,
        (Class<?>[]) null);
  }

  @Test
  public void testMessagePackCollectionParseNoParameterTypeExpectNullPointerException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unable to deserialize to empty parameterClass type");
    ObjectSerialization.readMessagePackCollection(new byte[]{1}, List.class, Foo.class);
  }

  private ChannelSegment createMockChannelSegment(UUID channelIdA, Instant startTime,
      Instant endTime) {
    return ChannelSegment
        .create(channelIdA, "mockSegment", ChannelSegment.Type.ACQUIRED, new TreeSet<>(), CreationInfo.DEFAULT);
  }
}
