package gms.shared.mechanisms.configuration.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.configuration.Configuration;
import gms.shared.mechanisms.configuration.ConfigurationRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateConfigurationClientUtility {

  private static final Logger logger = LoggerFactory
      .getLogger(UpdateConfigurationClientUtility.class);

  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  private UpdateConfigurationClientUtility() {
  }

  public static void store(ConfigurationRepository configurationRepository, Configuration configuration) {

    // TODO: verify inputs

    final Map<String, String> keyValues = new HashMap<>();

    // Extract each Configuration
    keyValues.put(configuration.getName(), serialize(configuration));

    // TODO: some sort of exception if couldn't prepare the configurations for storage

    keyValues.forEach(configurationRepository::put);

    // TODO: exception if configuration could not be stored?
  }

  private static String serialize(Object o) {
    try {
      return objectMapper.writeValueAsString(o);
    } catch (JsonProcessingException e) {
      logger.error("Could not serialze object to json", e);
      logger.debug("Could not serialize object " + o + " to json", e);
      throw new IllegalArgumentException("Could not serialize object to json", e);
    }
  }
}
