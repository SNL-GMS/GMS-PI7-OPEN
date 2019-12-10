package gms.core.signalenhancement.fk.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.core.signalenhancement.fk.FkTestUtility;
import gms.core.signalenhancement.fk.control.FkSpectraCommand;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.Arrays;
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

  //TODO: This test says JSON but uses message pack
  @Test
  public void testFkSpectrumStreamingCommandJsonSerialization() {
    final FkSpectraCommand cmd = FkTestUtility.defaultSpectraCommand();

    byte[] messagePack = ObjectSerialization.writeMessagePack(cmd);
    assertNotNull(messagePack);
    assertTrue(messagePack.length > 0);

    FkSpectraCommand deserialized = ObjectSerialization
        .readMessagePack(messagePack, FkSpectraCommand.class);
    assertNotNull(deserialized);
    assertEquals(cmd, deserialized);
  }

  @Test
  public void testFkSpectrumStreamingCommandMessagePackSerialization() {
    final FkSpectraCommand cmd = FkTestUtility.defaultSpectraCommand();

    byte[] messagePack = ObjectSerialization.writeMessagePack(cmd);
    assertNotNull(messagePack);
    assertTrue(messagePack.length > 0);

    FkSpectraCommand deserialized = ObjectSerialization
        .readMessagePack(messagePack, FkSpectraCommand.class);
    assertNotNull(deserialized);
    assertEquals(cmd, deserialized);
  }

  @Test
  public void testFkSpectrumClaimCheckDtoJsonSerialization() {
    // StationProcessingInterval has the same fields as the claim check.  Just
    // use the FkSpectraClaimCheckCommandDto mixin to serialize the claim check.
    List<SignalDetectionHypothesisDescriptor> descriptors = FkTestUtility.defaultInputDescriptors();
    byte[] json = ObjectSerialization.writeJson(descriptors);
    assertNotNull(json);
    assertTrue(json.length > 0);

    List<SignalDetectionHypothesisDescriptor> deserialized = Arrays.asList(ObjectSerialization
        .readJson(json, SignalDetectionHypothesisDescriptor[].class));
    assertNotNull(deserialized);
    assertEquals(descriptors, deserialized);
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
  public void testJsonParseErrorExpectUncheckedIOException() {
    exception.expect(UncheckedIOException.class);
    exception.expectMessage("Unable to deserialize object from json");
    ObjectSerialization.readJson("{bad json}".getBytes(), Foo.class);
  }

  @Test
  public void testJsonParseNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize null json");
    ObjectSerialization.readJson(null, Foo.class);
  }

  @Test
  public void testJsonParseNullTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize to null class type");
    ObjectSerialization.readJson("{-100}".getBytes(), null);
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
  public void testMessagePackParseErrorExpectUncheckedIOException() {
    exception.expect(UncheckedIOException.class);
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
