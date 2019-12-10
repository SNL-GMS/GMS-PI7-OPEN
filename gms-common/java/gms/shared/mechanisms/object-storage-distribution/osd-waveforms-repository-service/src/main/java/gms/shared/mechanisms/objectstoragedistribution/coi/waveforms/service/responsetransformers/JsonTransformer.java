package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.responsetransformers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import spark.ResponseTransformer;

/**
 * Transforms spark response objects to JSON.
 */
public class JsonTransformer implements ResponseTransformer {

  /**
   * Serializes and deserializes objects
   */
  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  @Override
  public String render(Object model) throws JsonProcessingException {
    return objectMapper.writeValueAsString(model);
  }
}