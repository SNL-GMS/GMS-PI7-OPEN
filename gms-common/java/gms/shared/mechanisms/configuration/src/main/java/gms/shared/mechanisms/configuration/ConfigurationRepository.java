package gms.shared.mechanisms.configuration;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface providing retrieval and storage operations for {@link Configuration}s.
 */
public interface ConfigurationRepository {

  /**
   * Obtains the most recent {@link Configuration} with the provided key.  The returned {@link
   * Optional} is empty if the key does not resolve to a Configuration.
   *
   * @param key key to a single configuration value, not null
   * @return Optional Configuration, not null
   * @throws NullPointerException if key is null
   * @throws IllegalArgumentException if the key resolves to more than a single Configuration
   */
  default Optional<Configuration> get(String key) {
    throw new UnsupportedOperationException();
  }

  /**
   * Obtains the {@link Configuration} with the provided key as the Configuration existed at
   * changeTime.  The returned {@link Optional} is empty if the key does not resolve to a
   * Configuration or the Configuration did not have a value at changeTime.
   *
   * @param key key to a single configuration value, not null
   * @param changeTime lookup the configuration value with the most recent {@link
   * Configuration#getChangeTime()} occurring before or at the same time as this {@link
   * Instant}, not null
   * @return Optional Configuration, not null
   * @throws NullPointerException if key, or changeTime are null
   * @throws IllegalArgumentException if the key resolves to more than a single Configuration
   */
  default Optional<Configuration> get(String key, Instant changeTime) {
    throw new UnsupportedOperationException();
  }

  /**
   * Obtains all of the {@link Configuration}s which have keys beginning with the provided
   * keyPrefix.  The returned Collection is empty if no Configurations have keys matching the
   * prefix.
   *
   * @param keyPrefix key prefix to one or more configuration values, not null
   * @return Collection of Configuration, not null
   * @throws NullPointerException if keyPrefix is null
   */
  default Collection<Configuration> getKeyRange(String keyPrefix) {
    throw new UnsupportedOperationException();
  }

  /**
   * Obtains all of the {@link Configuration}s which have keys beginning with the provided
   * keyPrefix as the Configurations existed at changeTime.  The returned Collection is empty
   * if the keyPrefix does not resolve to any Configuration keys or if no Configuration had
   * a value at changeTime.
   *
   * @param keyPrefix key prefix to one or more configuration values, not null
   * @param changeTime lookup the configuration values with the most recent {@link
   * Configuration#getChangeTime()} occurring before or at the same time as this {@link
   * Instant}, not null
   * @return Collection of Configuration, not null
   * @throws NullPointerException if keyPrefix or changeTime are null
   */
  default Collection<Configuration> getKeyRange(String keyPrefix, Instant changeTime) {
    throw new UnsupportedOperationException();
  }

  /**
   * Obtains the history of {@link Configuration} with the provided key as the Configuration
   * existed between startChangeTime and endChangeTime.  The returned List is empty if the key does
   * not resolve to a Configuration or the Configuration did not have a value between
   * startChangeTime and endChangeTime.
   *
   * @param key key to a single configuration value, not null
   * @param startChangeTime lookup the configuration value history beginning with {@link
   * Configuration#getChangeTime()} occurring before or at the same time as this {@link
   * Instant}, not null
   * @param endChangeTime lookup the configuration value history ending with {@link
   * Configuration#getChangeTime()} occurring before or at the same time as this {@link
   * Instant}, not null
   * @return List of Configuration sorted by ascending {@link Configuration#getChangeTime()},
   * not null
   * @throws NullPointerException if key, startChangeTime, or endChangeTime are null
   * @throws IllegalArgumentException if the key resolves to more than a single Configuration
   */
  default List<Configuration> get(String key, Instant startChangeTime, Instant endChangeTime) {
    throw new UnsupportedOperationException();
  }

  /**
   * Obtains the history of {@link Configuration}s with the provided keyPrefix as the
   * Configurations existed between startChangeTime and endChangeTime.  Returns a mapping
   * from Configuration keys to their histories.  Each history is a List of Configuration
   * sorted by ascending {@link Configuration#getChangeTime()}.  The returned Map is empty if
   * the keyPrefix does not resolve to any Configuration. Individual Lists within the Map are
   * empty when a Configuration's key matches a keyPrefix but the Configuration did not have
   * a value between startChangeTime and endChangeTime.
   *
   * @param keyPrefix prefix to one or more configuration values, not null
   * @param startChangeTime lookup configuration value histories beginning with {@link
   * Configuration#getChangeTime()} occurring before or at the same time as this {@link
   * Instant}, not null
   * @param endChangeTime lookup configuration value histories ending with {@link
   * Configuration#getChangeTime()} occurring before or at the same time as this {@link
   * Instant}, not null
   * @return Map of Configuration keys to Lists of their time histories (sorted by ascending
   * {@link Configuration#getChangeTime()}), not null
   * @throws NullPointerException if keyPrefix, startChangeTime, or endChangeTime are null
   */
  default Map<String, List<Configuration>> getKeyRange(String keyPrefix,
      Instant startChangeTime, Instant endChangeTime) {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates and stores a new {@link Configuration} mapping key to value.  The new
   * Configuration's {@link Configuration#getChangeTime()} is set to the time this operation
   * executes.
   *
   * @param key Configuration key, not null
   * @param value Configuration key, not null
   * @return {@link Optional} GmsConfiguationItem corresponding to the previous entry for the key.
   * Empty if this is the first entry for key.  Not null.
   * @throws NullPointerException if key or value are null
   */
  default Optional<Configuration> put(String key, String value) {
    throw new UnsupportedOperationException();
  }

  /*
  // TODO: need to figure out what watch() methods should look like.
  @FunctionalInterface
  interface GmsConfigurationValueChange {

    // TODO: also provide previous value?
    void update(Configuration updatedConfiguration);
  }

  // TODO: watch until certain time?  only provide updates at fixed intervals?
  // These seem like details if we aren't going to initially focus on watches
  void watch(String key, GmsConfigurationValueChange callback);

  void watchRange(String keyRange, GmsConfigurationValueChange callback);
  */
}
