package gms.shared.mechanisms.configuration;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AutoValue
@JsonSerialize(using = ConfigurationReference.Serializer.class)
@JsonDeserialize(using = ConfigurationReference.Deserializer.class)
/**
 * Class for making references to other configuration objects.
 * Used by {@link gms.shared.mechanisms.configuration.client.GlobalConfigurationReferenceResolver}
 * for substituting configuration values into the referencing configuration.
 */
public abstract class ConfigurationReference {

  //configuration references in a yaml file start with this string
  public static final String REF_COMMAND = "$ref=";

  @JsonCreator
  public static ConfigurationReference from(
      @JsonProperty("key") String key,
      @JsonProperty("selectors") List<Selector> selectors) {

    return new AutoValue_ConfigurationReference(key, selectors);
  }

  public abstract String getKey();

  public abstract List<Selector> getSelectors();

  /**
   * Determines if a field map key is a configuration reference
   * @param fieldMapKey The field map key
   * @return True if the field map key is a configuration reference
   */
  public static boolean isConfigurationReferenceKey(String fieldMapKey) {
    return fieldMapKey.startsWith(REF_COMMAND);
  }

  /**
   * Custom serializer for configuration references.  Configuration references
   * are represented in yaml as:
   * $ref=[name] : list of selectors
   *
   * Note that the key $ref=[name] is not a valid Java object property.  This is by
   * design as before a Java object can be created from yaml, configuration references
   * should be resolved.
   *
   */
  public static class Serializer extends StdSerializer<ConfigurationReference> {

    public Serializer() {
      this(ConfigurationReference.class);
    }

    Serializer(Class<ConfigurationReference> clazz) {
      super(clazz);
    }

    @Override
    public void serialize(ConfigurationReference myClass, JsonGenerator generator,
        SerializerProvider provider)
        throws IOException {
      generator.writeStartObject();
      generator.writeObjectField(REF_COMMAND + myClass.getKey(), myClass.getSelectors());
      generator.writeEndObject();
    }
  }

  /**
   * Custom deserializer for configuration references.  Takes a configuration reference
   * field map key/value and creates a ConfigurationReference object.
   */
  public static class Deserializer extends StdDeserializer<ConfigurationReference> {

    public Deserializer() {
      this(null);
    }

    Deserializer(Class<?> vc) {
      super(vc);
    }

    @Override
    public ConfigurationReference deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException {
      JsonNode node = jp.getCodec().readTree(jp);
      String fieldName = node.fieldNames().next();
      String key = fieldName.substring(REF_COMMAND.length());
      List<Selector> selectors = Arrays.asList(ObjectSerialization.getObjectMapper()
          .readValue(node.get(fieldName).traverse(), Selector[].class));

      return ConfigurationReference.from(key, selectors);
    }
  }
}