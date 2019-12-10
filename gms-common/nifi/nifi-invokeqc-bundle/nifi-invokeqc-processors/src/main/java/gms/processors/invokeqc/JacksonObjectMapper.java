package gms.processors.invokeqc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.ObjectMapper;
import java.io.IOException;

public class JacksonObjectMapper implements ObjectMapper {

  private final com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper;

  public JacksonObjectMapper(com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper) {
    this.jacksonObjectMapper = jacksonObjectMapper;
  }

  @Override
  public <T> T readValue(String value, Class<T> valueType) {
    try {
      return jacksonObjectMapper.readValue(value, valueType);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String writeValue(Object value) {
    try {
      return jacksonObjectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
