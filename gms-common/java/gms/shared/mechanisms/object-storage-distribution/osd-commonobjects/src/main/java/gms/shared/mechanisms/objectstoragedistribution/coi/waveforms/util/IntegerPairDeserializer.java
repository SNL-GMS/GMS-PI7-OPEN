package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Deserializer for a {@link Pair} of{@link Integer}s as a {@link java.util.Map} key
 */
public class IntegerPairDeserializer extends KeyDeserializer {

  @Override
  public Pair<Integer, Integer> deserializeKey(String key, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    Pattern pattern = Pattern.compile("\\(([0-9]+),([0-9]+)\\)");
    Matcher matcher = pattern.matcher(key);
    if (matcher.find()) {
      return Pair.of(Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(2)));
    } else {
      throw new IOException("Could not deserialize " + key + " into Pair<Integer,Integer>");
    }
  }
}
