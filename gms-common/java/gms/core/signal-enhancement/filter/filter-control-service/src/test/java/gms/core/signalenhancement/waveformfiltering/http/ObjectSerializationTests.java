package gms.core.signalenhancement.waveformfiltering.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.core.signalenhancement.waveformfiltering.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ObjectSerializationTests {

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
   * Sample {@link StreamingRequest} used in the serialization and deserialization tests
   */
  private StreamingRequest streamingRequest;


  @BeforeEach
  public void setUp() {
    streamingRequest = TestFixtures.getStreamingRequest();
  }

  @Test
  void testStreamingDtoJsonSerialization() {
    byte[] json = ObjectSerialization.writeJson(streamingRequest);
    assertNotNull(json);
    assertTrue(json.length > 0);

    StreamingRequest deserialized = ObjectSerialization.readJson(json, StreamingRequest.class);
    assertNotNull(deserialized);
    assertEquals(streamingRequest, deserialized);
  }

  @Test
  void testJsonSerializationErrorExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> ObjectSerialization.writeJson(new Foo(99)));
  }

  @Test
  void testJsonSerializeNullExpectNullPointerException() {
    assertThrows(NullPointerException.class, () -> ObjectSerialization.writeJson(null));
  }

  @Test
  void testJsonParseErrorExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> ObjectSerialization.readJson("{bad json}".getBytes(), Foo.class));
  }

  @Test
  void testJsonParseNullArgumentsExpectNullPointerException() {
    assertThrows(NullPointerException.class, () -> ObjectSerialization.readJson(null, Foo.class));

    assertThrows(NullPointerException.class,
        () -> ObjectSerialization.readJson("{-100}".getBytes(), null));
  }

  @Test
  void testStreamingDtoMessagePackSerialization() {
    byte[] messagePack = ObjectSerialization.writeMessagePack(streamingRequest);
    assertNotNull(messagePack);
    assertTrue(messagePack.length > 0);

    StreamingRequest deserialized = ObjectSerialization
        .readMessagePack(messagePack, StreamingRequest.class);
    assertNotNull(deserialized);
    assertEquals(streamingRequest, deserialized);
  }

  @Test
  void testMessagePackSerializationErrorExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> ObjectSerialization.writeMessagePack(new Foo(99)));
  }

  @Test
  void testMessagePackSerializeNullExpectNullPointerException() {
    assertThrows(NullPointerException.class, () -> ObjectSerialization.writeMessagePack(null));
  }

  @Test
  void testMessagePackParseErrorExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> ObjectSerialization.readMessagePack(new byte[]{1}, Foo.class));
  }

  @Test
  void testMessagePackParseNullArgumentsExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> ObjectSerialization.readMessagePack(null, Foo.class));

    assertThrows(NullPointerException.class,
        () -> ObjectSerialization.readMessagePack(new byte[]{1}, null));
  }

  @Test
  void testMessagePackCollectionParseErrorExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> ObjectSerialization
        .readMessagePackCollection(new byte[]{1}, List.class, ChannelSegment.class,
            Waveform.class));
  }

  @Test
  void testMessagePackCollectionParseNullArgumentsExpectNullPointerException() {
    assertThrows(NullPointerException.class, () -> ObjectSerialization
        .readMessagePackCollection(null, List.class, Foo.class, Foo.class));

    assertThrows(NullPointerException.class, () -> ObjectSerialization
        .readMessagePackCollection(new byte[]{1}, null, Foo.class, Foo.class));

    assertThrows(NullPointerException.class, () -> ObjectSerialization
        .readMessagePackCollection(new byte[]{1}, List.class, null, Foo.class));

    assertThrows(NullPointerException.class, () -> ObjectSerialization
        .readMessagePackCollection(new byte[]{1}, List.class, Foo.class, (Class<?>) null));

    assertThrows(NullPointerException.class, () -> ObjectSerialization
        .readMessagePackCollection(new byte[]{1}, List.class, Foo.class, (Class<?>[]) null));

  }

  @Test
  void testMessagePackCollectionParseNoParameterTypeExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> ObjectSerialization.readMessagePackCollection(new byte[]{1}, List.class, Foo.class));
  }

  @Test
  void testJsonClientObjectMapper() {
    final ChannelSegmentDescriptor requestDto = ChannelSegmentDescriptor.from(
        UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(10));

    com.mashape.unirest.http.ObjectMapper jsonMapper = ObjectSerialization
        .getJsonClientObjectMapper();

    assertEquals(requestDto, jsonMapper
        .readValue(jsonMapper.writeValue(requestDto), ChannelSegmentDescriptor.class));
  }
}
