package gms.utilities.transfercleanup;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.TransferredFileRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.TransferredFileRepositoryJpa;
import java.time.Duration;
import java.util.Map;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    // for now using same default value for maxAgeOfFile and cleanup frequency
    final Duration defaultDur = Duration.ofMinutes(30);
    final Configuration config = Application.loadConfig();
    final TransferredFileRepositoryInterface repo
        = new TransferredFileRepositoryJpa(
            CoiEntityManagerFactory.create(loadJpaConfig(config)));
    final Duration maxAgeOfFile = readConfigDuration(config, "maxAge", defaultDur);
    final Duration cleanupFreq = readConfigDuration(config, "cleanupFreq", defaultDur);;

    while (true) {
      try {
        logger.info("Calling the repo to remove old files");
        repo.removeSentAndReceived(maxAgeOfFile);
        logger.info("transferred file cleanup utility done; will now sleep " + cleanupFreq);
      } catch (Exception e) {
        logger.error("Error trying to cleanup", e);
      }
      try {
        Thread.sleep(cleanupFreq.toMillis());
      } catch (InterruptedException e) {
        logger.error("Interrupted while asleep between cleanup calls", e);
      }
    }
  }

  private static CompositeConfiguration loadConfig() {
    try {
      CompositeConfiguration config = new CompositeConfiguration();
      config.addConfiguration(new EnvironmentConfiguration());

      config.addConfiguration(new PropertiesConfiguration(
          "gms/utilities/transfercleanup/application.properties"));
      return config;
    } catch(Exception e) {
      logger.error("Error loading config, will use defaults", e);
      return null;
    }
  }

  private static Map<String, String> loadJpaConfig(Configuration configuration) {
    if (configuration == null) {
      logger.info("Config not present, not overridding any JPA config");
      return Map.of();
    }

    String persistenceUrl = configuration.getString("persistence_url");
    if (persistenceUrl != null) {
        logger.info("persistence_url specified as " + persistenceUrl);
        return Map.of("hibernate.connection.url", persistenceUrl);
    }
    logger.info("persistence_url not specified, using default from repository");
    return Map.of();
  }

  private static Duration readConfigDuration(Configuration config,
      String name, Duration defaultVal) {

    if (config == null) {
      logger.info("Config not present, defaulting "
          + name + " to " + defaultVal);
      return defaultVal;
    }

    final String val = config.getString(name);
    if (val == null) {
      logger.info("Config value " + name
          + " not present, defaulting to " + defaultVal);
      return defaultVal;
    }
    try {
      final Duration d = Duration.parse(val);
      logger.info(name + " specified as " + d);
      if (d.isNegative()) {
        logger.error("Negative duration specified: " + d
            + ", using default " + defaultVal);
        return defaultVal;
      }
      return d;
    } catch(Exception ex) {
      logger.error("Could not parse " + val
          + " as duration, defaulting to " + defaultVal);
      return defaultVal;
    }
  }
}
