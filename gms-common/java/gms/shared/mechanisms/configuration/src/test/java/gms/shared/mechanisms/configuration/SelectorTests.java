package gms.shared.mechanisms.configuration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import org.junit.jupiter.api.Test;

public class SelectorTests {

  private ObjectMapper objectMapper = ObjectSerialization.getObjectMapper();

  @Test
  public void testSerialization() throws Exception {
    final Selector<String> selectorStaIsGeres = Selector.from("STATION", "geres");
    final String json = objectMapper.writeValueAsString(selectorStaIsGeres);

    assertAll(
        () -> assertNotNull(json),
        () -> assertEquals(selectorStaIsGeres, objectMapper.readValue(json, Selector.class))
    );
  }
}
