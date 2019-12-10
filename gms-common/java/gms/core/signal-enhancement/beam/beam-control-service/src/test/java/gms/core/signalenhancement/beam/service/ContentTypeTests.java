package gms.core.signalenhancement.beam.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ContentTypeTests {

  private static class DummyClass {

    private int dummyIntField;
    private String dummyStringField;
    private List<Integer> dummyListIntField;

    public DummyClass() {

    }

    public DummyClass(int dummyIntField, String dummyStringField,
        List<Integer> dummyListIntField) {
      this.dummyIntField = dummyIntField;
      this.dummyStringField = dummyStringField;
      this.dummyListIntField = dummyListIntField;
    }

    public int getDummyIntField() {
      return dummyIntField;
    }

    public void setDummyIntField(int dummyIntField) {
      this.dummyIntField = dummyIntField;
    }

    public String getDummyStringField() {
      return dummyStringField;
    }

    public void setDummyStringField(String dummyStringField) {
      this.dummyStringField = dummyStringField;
    }

    public List<Integer> getDummyListIntField() {
      return dummyListIntField;
    }

    public void setDummyListIntField(List<Integer> dummyListIntField) {
      this.dummyListIntField = dummyListIntField;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      DummyClass that = (DummyClass) o;

      if (dummyIntField != that.dummyIntField) {
        return false;
      }
      if (dummyStringField != null ? !dummyStringField.equals(that.dummyStringField)
          : that.dummyStringField != null) {
        return false;
      }
      return dummyListIntField != null ? dummyListIntField.equals(that.dummyListIntField)
          : that.dummyListIntField == null;
    }

    @Override
    public int hashCode() {
      int result = dummyIntField;
      result = 31 * result + (dummyStringField != null ? dummyStringField.hashCode() : 0);
      result = 31 * result + (dummyListIntField != null ? dummyListIntField.hashCode() : 0);
      return result;
    }
  }

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testContentTypesWithImplementedSerialization() {
    DummyClass dummyObject = new DummyClass(4, "Test", List.of(1, 2, 3));

    Map<ContentType, Function<Object, byte[]>> serializerMap = Map.of(
        ContentType.APPLICATION_JSON, ObjectSerialization::writeJson,
        ContentType.APPLICATION_MSGPACK, ObjectSerialization::writeMessagePack,
        ContentType.APPLICATION_ANY, ObjectSerialization::writeJson
    );

    Map<ContentType, BiFunction<byte[], Class<DummyClass>, DummyClass>> deserializerMap = Map.of(
        ContentType.APPLICATION_JSON, ObjectSerialization::readJson,
        ContentType.APPLICATION_MSGPACK, ObjectSerialization::readMessagePack,
        ContentType.APPLICATION_ANY, ObjectSerialization::readJson
    );

    Arrays.stream(ContentType.values()).filter(contentType ->
        serializerMap.keySet().contains(contentType)
    ).forEach(contentType -> {
      byte[] json = serializerMap.get(contentType).apply(dummyObject);
      assertArrayEquals(json, contentType.getSerializer().apply(dummyObject));

      assertEquals(deserializerMap.get(contentType).apply(json, DummyClass.class),
          contentType.getDeserializer(DummyClass.class).apply(json));
    });
  }

  @Test
  public void testUnimplementedContentTypesThrowException() {

    Arrays.stream(ContentType.values()).filter(contentType ->
        contentType.equals(ContentType.TEXT_PLAIN)
            || contentType.equals(ContentType.UNKNOWN)
    ).forEach(contentType -> {
      try {
        contentType.getSerializer();
        fail("No exception thrown for unimplemented content type serialization");
      } catch (IllegalStateException e) {

      } catch (Exception e) {
        fail("Wrong exception thrown for unimplemented content type serialization");
      }

      try {
        contentType.getDeserializer(DummyClass.class);
        fail("No exception thrown for unimplemented content type deserialization");
      } catch (IllegalStateException e) {

      } catch (Exception e) {
        fail("Wrong exception thrown for unimplemented content type serialization");
      }
    });
  }
}
