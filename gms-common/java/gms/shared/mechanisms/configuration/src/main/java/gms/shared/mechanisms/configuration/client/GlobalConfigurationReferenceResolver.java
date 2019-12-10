package gms.shared.mechanisms.configuration.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import gms.shared.mechanisms.configuration.Configuration;
import gms.shared.mechanisms.configuration.ConfigurationOption;
import gms.shared.mechanisms.configuration.ConfigurationReference;
import gms.shared.mechanisms.configuration.ConfigurationResolver;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class for resolving configuration references.
 */
public class GlobalConfigurationReferenceResolver {

  // Configuration references can only refer to the global configuration space.
  // The global configuration space is not allowed to contain any references.
  // This keeps configuration references simple and gaurantees there are no reference cycles.
  public static final String REFERENCED_CONFIGURATION_KEY_PREFIX = "global";

  private GlobalConfigurationReferenceResolver() {
  }

  /**
   * Resolves/substitutes all referenced configuration values into a configuration object
   *
   * @param configurationsByKey The configurations for each reference continued in the configuration
   * object
   * @param configuration The object that contains the configuration references.  After this
   * operation, this object will contain only configuration values.
   * @return A configuration object with only configuration values.
   */
  public static Configuration resolve(Map<String, Configuration> configurationsByKey,
      Configuration configuration) {
    Objects.requireNonNull(configurationsByKey,
        "GlobalConfigurationReferenceResolver requires a non-null configurationsByKey");
    Objects.requireNonNull(configuration,
        "GlobalConfigurationReferenceResolver requires a non-null configuration");

    return Configuration.from(
        configuration.getName(),
        configuration.getConfigurationOptions().stream()
            .map(opt ->
                ConfigurationOption.from(
                    opt.getName(),
                    opt.getConstraints(),
                    resolve(configurationsByKey, new HashMap<>(opt.getParameters()))
                )
            ).collect(Collectors.toList()),
        configuration.getChangeTime());
  }

  /**
   * Recursively performs configuration resolution on all reference keys in the parameters object.
   *
   * @param configurationsByKey A map of configurations indexed by configuration key
   * @param parameters The parameters map that may contain zero, 1 or more configuration references
   * @return A field map where all configuration references have been substituted with the values
   * from the referenced parameters.
   */
  static Map<String, Object> resolve(Map<String, Configuration> configurationsByKey,
      Map<String, Object> parameters) {
    Objects.requireNonNull(configurationsByKey,
        "GlobalConfigurationReferenceResolver requires a non-null configurationsByKey");
    Objects.requireNonNull(parameters,
        "GlobalConfigurationReferenceResolver requires a non-null parameters");

    Map<String, Object> result = new HashMap<>();

    List<ConfigurationReference> references = removeConfigurationReferences(parameters);

    for (ConfigurationReference ref : references) {
      Map<String, Object> globalConfig = resolveReference(configurationsByKey, ref);
      result.putAll(globalConfig);
    }

    // recursively resolve the values stored in the field keys
    for (Entry<String, Object> entry : parameters.entrySet()) {
      Object val = entry.getValue();

      if (val instanceof Collection) {
        // Check each item in the collection for substitutions
        //TODO - we are assuming that we don't need
        // to create the same type of collection that was passed in.  Jackson serialization should
        // be able to handle serializing a list into a set, etc...
        Collection<Object> resultCollection = new ArrayList<>();
        for (Object valItem : (Collection) val) {
          Map<String, Object> tmp = resolve(
              configurationsByKey,
              //wrap the value in a map
              Map.of("collection_item", valItem)
          );

          // unwrap the value
          resultCollection.add(tmp.get("collection_item"));
        }

        // substituted value is a collection of substituted values
        val = ImmutableList.copyOf(resultCollection);
      } else if (val instanceof Map) {
        // Check this map for substitutions
        // substituted value is a map of substituted values
        val = resolve(configurationsByKey, (Map<String, Object>) val);
      }
      //Otherwise, NoOp - Do nothing!!! no substitution required
      //This assumes that a field value won't be an application specific Java object that contains a map, or
      //string that needs to be checked for substitution.  This assumption is safe given how the configuration
      //objects are loaded - as Maps, Collections and intrinsic java types (strings, booleans, numbers, etc...).
      // It is only later when configuration objects are cast into application specific java objects.

      // the key was a field key - keep it and the resolved value
      result.put(entry.getKey(), val);
    }

    return ImmutableMap.copyOf(result);
  }

  /**
   * Returns the field map specified by the given configuration reference.
   *
   * @param configurationsByKey The configurations map that contains the referenced configuration
   * @param configRef The configuration reference object
   * @return The field map that the configuration reference refers to.
   */
  private static Map<String, Object> resolveReference(
      Map<String, Configuration> configurationsByKey, ConfigurationReference configRef) {
    if (!isValidConfigurationKey(configRef.getKey())) {
      String referenceString = configRef.toString();
      throw new IllegalArgumentException("[Invalid Configuration Reference \"" + referenceString
          + "\"] : Configuration references must use the \""
          + REFERENCED_CONFIGURATION_KEY_PREFIX + "\" key prefix.");
    }

    //Perform the substitution with the configuration key/constraint mechanism
    try {
      return ConfigurationResolver.resolve(
          Optional.ofNullable(configurationsByKey.get(configRef.getKey()))
              .orElseThrow(() -> new IllegalArgumentException(
                  "No Configuration named " + configRef.getKey()
                      + " is in this ConfigurationConsumerUtility")),
          configRef.getSelectors());
    } catch (Exception e) {
      String referenceString = configRef.toString();
      throw new IllegalArgumentException("[Invalid Configuration Reference \"" + referenceString
          + "\"] : Configuration reference returned an empty configuration.");
    }
  }

  /**
   * Removes configuration references from a field map.  Note that this method is not recusive. It
   * does not recursively remove configurations references from field maps contained in the
   * top-level field map.
   *
   * @param map The field map representation of a cofiguration parameter object.  This field map can
   * have 0 or more configuration references.
   * @return A list containing the configuration references that were removed from the input field
   * map.
   */
  static List<ConfigurationReference> removeConfigurationReferences(
      Map<String, Object> map) {
    List<ConfigurationReference> references = new ArrayList<>();

    Set<String> keys = new HashSet<>(map.keySet());

    for (String key : keys) {
      if (ConfigurationReference.isConfigurationReferenceKey(key)) {
        references.add(ObjectSerialization.fromFieldMap(
            Map.of(key, map.remove(key)),
            ConfigurationReference.class)
        );
      }
    }

    references.sort(Comparator.comparing(ConfigurationReference::getKey));

    return ImmutableList.copyOf(references);
  }

  /**
   * Checks that the configuration reference key is allowed/valid.
   *
   * @param key The configuration reference key
   * @return True if the configuration reference is allowed/valid.
   */
  private static boolean isValidConfigurationKey(String key) {
    return key.startsWith(REFERENCED_CONFIGURATION_KEY_PREFIX);
  }
}
