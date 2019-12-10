package gms.shared.mechanisms.objectstoragedistribution.coi.common;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PluginRegistry<T extends Plugin> {

  private static final Logger logger = LogManager.getLogger(PluginRegistry.class);

  private final Map<RegistrationInfo, T> pluginMap;

  /**
   * Creates new HashMap to store the registration information
   */
  public PluginRegistry() {
    this.pluginMap = new HashMap<>();
  }

  /**
   * Gets registration information from {@link RegistrationInfo} and checks if null. If there is no
   * information and error is thrown, else it returns a HashMap of the registration information
   *
   * @param registrationInfo The name and version information collected in {@link RegistrationInfo}
   * @return A HashMap containing the registration information
   */
  public Optional<T> lookup(RegistrationInfo registrationInfo) {
    Objects.requireNonNull(registrationInfo,
        "Error retrieving plugin: null is an invalid RegistrationInformation key");
    return Optional.ofNullable(pluginMap.get(registrationInfo));
  }

  /**
   * Takes registration information and pairs it with the plugin, then puts it into a HashMap,
   * completing the registration.
   *
   * @param plugin The signal detector plugin component
   * @return The registration information (name and version) associated with the plugin
   */
  public RegistrationInfo register(T plugin) {
    RegistrationInfo registrationInfo = RegistrationInfo
        .from(plugin.getName(), plugin.getVersion());

    logger.info("Registering {} {}", plugin.getName(), plugin.getVersion());

    pluginMap.put(registrationInfo, plugin);
    return registrationInfo;
  }

  public Set<Entry> entrySet() {
    return pluginMap.entrySet().stream().map(Entry::from).collect(Collectors.toSet());
  }

  public static class Entry<T extends Plugin> {

    private final RegistrationInfo registration;
    private final T plugin;

    private Entry(RegistrationInfo registration,
        T plugin) {
      this.registration = registration;
      this.plugin = plugin;
    }

    public static <T extends Plugin> Entry<T> create(RegistrationInfo registration, T plugin) {
      Objects.requireNonNull(registration, "Cannot create Entry from null RegistrationInfo");
      Objects.requireNonNull(plugin, "Cannot create Entry from null RegistrationInfo");
      return new Entry<T>(registration, plugin);
    }

    public static <T extends Plugin> Entry<T> from(Map.Entry<RegistrationInfo, T> mapEntry) {
      return create(mapEntry.getKey(), mapEntry.getValue());
    }

    public RegistrationInfo getRegistration() {
      return registration;
    }

    public T getPlugin() {
      return plugin;
    }
  }


}
