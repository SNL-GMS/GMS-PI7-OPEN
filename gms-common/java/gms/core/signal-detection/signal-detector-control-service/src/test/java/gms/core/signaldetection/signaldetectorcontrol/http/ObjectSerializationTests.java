package gms.core.signaldetection.signaldetectorcontrol.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.core.signaldetection.signaldetectorcontrol.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
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

  /**
   * Sample {@link StreamingDto} used in the serialization and deserialization tests
   */
  private StreamingDto streamingDto;

  @Before
  public void setUp() {
    streamingDto = TestFixtures.getStreamingDto();
  }

  @Test
  public void testStreamingDtoJsonSerialization() {
    byte[] json = ObjectSerialization.writeJson(streamingDto);

    assertNotNull(json);
    assertTrue(json.length > 0);

    StreamingDto deserialized = ObjectSerialization.readJson(json, StreamingDto.class);
    assertNotNull(deserialized);
    assertEquals(streamingDto, deserialized);
  }

  @Test
  public void testJsonSerializationErrorExpectIllegalArgumentException() {
    exception.expect(UncheckedIOException.class);
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
  public void testStreamingDtoMessagePackSerialization() {
    byte[] messagePack = ObjectSerialization.writeMessagePack(streamingDto);
    assertNotNull(messagePack);
    assertTrue(messagePack.length > 0);

    StreamingDto deserialized = ObjectSerialization
        .readMessagePack(messagePack, StreamingDto.class);
    assertNotNull(deserialized);
    assertEquals(streamingDto, deserialized);
  }

  @Test
  public void testMessagePackSerializationErrorExpectIllegalArgumentException() {
    exception.expect(UncheckedIOException.class);
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
    exception.expect(UncheckedIOException.class);
    exception.expectMessage("Unable to deserialize object from binary");
    ObjectSerialization
        .readMessagePackCollection(new byte[]{1}, List.class, ChannelSegment.class, Waveform.class);
  }

  @Test
  public void testMessagePackCollectionParseNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize null binary");
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
  public void testMessagePackCollectionParseNullClassParameterTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize to null parameterTypes type");
    ObjectSerialization.readMessagePackCollection(new byte[]{1}, List.class, Foo.class,
        (Class<?>) null);
  }

  @Test
  public void testMessagePackCollectionParseNullParameterTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize to null parameterTypes type");
    ObjectSerialization
        .readMessagePackCollection(new byte[]{1}, List.class, Foo.class, (Class<?>[]) null);
  }

  @Test
  public void testMessagePackCollectionParseNullInParameterTypesExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Unable to deserialize to null parameterTypes type");
    ObjectSerialization
        .readMessagePackCollection(new byte[]{1}, List.class, Foo.class, new Class[]{null});
  }

}
