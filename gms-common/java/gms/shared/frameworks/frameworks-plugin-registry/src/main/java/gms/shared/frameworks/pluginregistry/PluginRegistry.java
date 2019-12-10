package gms.shared.frameworks.pluginregistry;

import gms.shared.frameworks.utilities.Validation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads and provides lookup for plugin implementations. PluginRegistry discovers available plugin
 * implementations at runtime. Plugin discovery occurs only when PluginRegistry is initialized.
 * PluginRegistry has no facility to discover plugins after initialization.
 */
public class PluginRegistry {

  private static final Logger logger = LoggerFactory.getLogger(PluginRegistry.class);

  private final Map<String, Plugin> plugins;

  private PluginRegistry(Map<String, Plugin> plugins) {
    this.plugins = plugins;
  }

  /**
   * Creates a PluginRegistry, discovering and registering all plugins available to a client.
   *
   * @return a plugin registry with plugins loaded
   * @throws IllegalStateException if errors instantiating registry and loading plugins
   */
  public static PluginRegistry create() {
    final Collection<Plugin> plugins;
    try {
      plugins = ServiceLoader.load(Plugin.class).stream()
          .map(Provider::get)
          .collect(Collectors.toList());
    } catch (Exception ex) {
      final String errorMsg = "Error loading plugins";
      logger.error(errorMsg);
      throw new IllegalStateException(errorMsg, ex);
    }
    return new PluginRegistry(registerPlugins(plugins));
  }
  ///////////////////////////////////////////////////////////////
  // operations to retrieve loaded plugins

  /**
   * Gets a collection of Plugins by their names. All of the returned Plugins must be instances of
   * the provided requiredType.
   *
   * @param name the name of the plugin
   * @param requiredType the required (super) class of the plugin
   * @param <T> the type of the plugin returned
   * @return plugin of that type; an exception is thrown if there is no plugin by the requested name
   * or if the found plugin doesn't have the requested type.
   * @throws NullPointerException if name or requiredType are null
   * @throws IllegalArgumentException if errors on retrieving or casting the plugin
   */
  public <T extends Plugin> T get(String name, Class<T> requiredType) {
    Objects.requireNonNull(name, "Cannot get plugin from null name");
    Objects.requireNonNull(requiredType, "Cannot get plugin of null type");
    validateRetrieval(Set.of(name), requiredType);
    return requiredType.cast(this.plugins.get(name));
  }

  /**
   * Gets a plugin by names and required type.
   *
   * @param names the names of the plugins
   * @param requiredType the required (super) class of the plugin * @param <T> the type of the
   * plugin returned
   * @return plugins matching the provided names of that type. If any plugin cannot be found as the
   * requested type, an exception is thrown.
   * @throws NullPointerException if name or requiredType are null
   * @throws IllegalArgumentException if errors on retrieving or casting the plugin
   */
  public <T extends Plugin> Collection<T> get(Set<String> names, Class<T> requiredType) {
    Validate.notEmpty(names, "Cannot get plugin from null names");
    Objects.requireNonNull(requiredType, "Cannot retrieve plugin by null required type");
    validateRetrieval(names, requiredType);
    return names.stream().map(s -> get(s, requiredType)).collect(Collectors.toSet());
  }

  /**
   * Throws an exception if any plugin in the names cannot be found as the required type.  The
   * thrown exception contains all problematic plugin names (not just the first problematic one).
   */
  private <T extends Plugin> void validateRetrieval(Set<String> names, Class<T> requiredType) {
    final Set<String> errors = new HashSet<>();
    for (String s : names) {
      final Plugin p = this.plugins.get(s);
      if (p == null) {
        errors.add("No plugin found by name " + s);
      } else if (!requiredType.isAssignableFrom(p.getClass())) {
        errors.add("Plugin named " + s + " is not of required type " + requiredType);
      }
    }
    Validate.isTrue(errors.isEmpty(), "Error retrieving plugin by names "
        + names + "; errors " + errors);
  }

  //////////////////////////////////////////////////////////////
  // operations to load plugins

  /**
   * Returns a map from plugin name to plugin, not allowing duplicate names.
   */
  private static Map<String, Plugin> registerPlugins(Collection<Plugin> plugins) {
    Objects.requireNonNull(plugins, "Cannot register plugins from null collection");
    Validation.throwForNonUnique(plugins, Plugin::getName,
        "Could not instantiate plugin registry because of duplicate plugin names");

    return Collections.unmodifiableMap(plugins.stream()
        .collect(Collectors.toMap(Plugin::getName, Function.identity())));
  }
}