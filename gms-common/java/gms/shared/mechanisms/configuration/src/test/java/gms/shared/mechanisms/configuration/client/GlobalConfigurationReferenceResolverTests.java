package gms.shared.mechanisms.configuration.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import gms.shared.mechanisms.configuration.Configuration;
import gms.shared.mechanisms.configuration.ConfigurationOption;
import gms.shared.mechanisms.configuration.ConfigurationReference;
import gms.shared.mechanisms.configuration.FileConfigurationRepository;
import gms.shared.mechanisms.configuration.Operator;
import gms.shared.mechanisms.configuration.Operator.Type;
import gms.shared.mechanisms.configuration.Selector;
import gms.shared.mechanisms.configuration.TestUtilities;
import gms.shared.mechanisms.configuration.constraints.StringConstraint;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GlobalConfigurationReferenceResolverTests {

  private static final String CONFIG_KEY = GlobalConfigurationReferenceResolver.REFERENCED_CONFIGURATION_KEY_PREFIX;

  private static final String CONFIG_NAME_PREFIX = "cfg";

  /**
   * Utitlity function for construction a map of Configurations.
   */
  private static Map<String, Configuration> configurationsByKey(int num) {
    List<ConfigurationOption> options = new ArrayList<>();

    for (int i = 0; i < num; i++) {
      options.add(buildOption(i));
    }

    return Map.of(CONFIG_KEY, Configuration.from(CONFIG_KEY, options));
  }

  /**
   * Utility function for building the parameters of a config object
   */
  private static Map<String, Object> buildParams(int id) {
    return Map.of("id", id,
        "field1", "field1_val" + id,
        "field2", "field2_val" + id
    );
  }

  private static String getConfigName(int id) {
    return CONFIG_NAME_PREFIX + id;
  }

  private static Map<String, Object> buildConfigRef(int id) {
    return ObjectSerialization.toFieldMap(
        ConfigurationReference
            .from(GlobalConfigurationReferenceResolver.REFERENCED_CONFIGURATION_KEY_PREFIX,
                List.of(Selector.from("name", getConfigName(id))))
    );
  }

  /**
   * Utility function.  Builds configuration option object used in these tests
   */
  private static ConfigurationOption buildOption(int id) {
    String name = getConfigName(id);

    return ConfigurationOption.from(name,
        List.of(
            StringConstraint.from("name",
                Operator.from(Type.EQ, false),
                Set.of(name), 100)),
        buildParams(id)
    );
  }

  @Test
  void testRemoveConfigurationReference() {
    List<ConfigurationReference> gt = new ArrayList<>(
        List.of(
            ConfigurationReference.from("global.filters",
                List.of(Selector.from("min_freq", "1.0"),
                    Selector.from("max_freq", "2.0"))),

            ConfigurationReference.from("global.beams",
                List.of(Selector.from("beam_name", "utah_beam")))
        ));

    Map<String, Object> nonReferenceParams = Map.of("non_reference_field", "value");
    Map<String, Object> params = new HashMap<>(nonReferenceParams);

    for (ConfigurationReference ref : gt) {
      params.putAll(ObjectSerialization.toFieldMap(ref));
    }

    List<ConfigurationReference> result = GlobalConfigurationReferenceResolver
        .removeConfigurationReferences(params);

    // the returned list should be sorted - this guarantees configuration order during resolution
    gt.sort(Comparator.comparing(ConfigurationReference::getKey));

    // make sure we are extracting the correct configuration reference info
    assertEquals(gt, result);

    // what is left over should be all the non-configuration reference fields
    assertEquals(nonReferenceParams, params);
  }


  @Test
  void testNoSubstitutions() {
    Map<String, Object> parameters = Map.of(
        "map", Map.of(
            "map1_val1", 1.0,
            "map1_val2", 2.0,
            "map1_1", Map.of(
                "map1_1_val2", 10
            )
        )
    );

    Map<String, Object> result = GlobalConfigurationReferenceResolver
        .resolve(configurationsByKey(10), parameters);

    Assert.assertEquals(parameters, result);
  }

  @Test
  void testSubstituteReferenceValuesAtTopLevel() {

    Map<String, Object> noSubValues = Map.of(
        "map1_val1", 1.0,
        "map1_val2", 2.0
    );

    Map<String, Object> parameters = new HashMap<>(buildConfigRef(0));
    parameters.put("map1", noSubValues);

    Map<String, Object> expectedResult = new HashMap<>(buildParams(0));
    expectedResult.put("map1", noSubValues);

    Map<String, Object> result = GlobalConfigurationReferenceResolver
        .resolve(configurationsByKey(10), parameters);

    Assert.assertEquals(expectedResult, result);
  }

  @Test
  void testSubstituteCollectionValues() {

    Map<String, Object> parameters = Map.of(
        "config_vals", List.of(
            buildConfigRef(0),
            buildConfigRef(1),
            buildConfigRef(2)
        )
    );

    Map<String, Object> expectedResult = Map.of(
        "config_vals", List.of(
            buildParams(0),
            buildParams(1),
            buildParams(2)
        )
    );

    Map<String, Object> result = GlobalConfigurationReferenceResolver
        .resolve(configurationsByKey(10), parameters);

    Assert.assertEquals(expectedResult, result);
  }

  @Test
  void testNonGlobalConfigReferenceThrowsException() {

    Map<String, Object> parameters = ObjectSerialization
        .toFieldMap(ConfigurationReference.from("non_global_ref", List.of()));

    TestUtilities.expectExceptionAndMessage(
        () -> GlobalConfigurationReferenceResolver
            .resolve(configurationsByKey(10), parameters),
        IllegalArgumentException.class,
        "Configuration references must use the"
    );
  }

  @Test
  void testEmptyGlobalConfigReference() {
    TestUtilities.expectExceptionAndMessage(
        () -> GlobalConfigurationReferenceResolver
            .resolve(configurationsByKey(10), buildConfigRef(20)),
        IllegalArgumentException.class,
        "Configuration reference returned an empty configuration."
    );
  }

  @Test
  void testNullConfigurationsByKeyParameter() {
    TestUtilities.expectExceptionAndMessage(
        () -> GlobalConfigurationReferenceResolver
            .resolve(null,
                Configuration.from("name",
                    List.of(
                        ConfigurationOption.from("name", List.of(), Map.of())
                    )
                )
            ),
        NullPointerException.class,
        "non-null configurationsByKey"
    );
  }

  @Test
  void testFileFilterReferenceExample() throws JsonProcessingException {
    final String configDir = "gms/shared/mechanisms/configuration/component-reference-example";
    final Path basePath = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource(configDir)).getFile())
        .toPath();

    final FileConfigurationRepository repository = FileConfigurationRepository.create(basePath);

    ConfigurationConsumerUtility u = ConfigurationConsumerUtility
        .builder(repository)
        .configurationNamePrefixes(List.of("component"))
        .build();

    Map<String, Object> globalMap = u.resolve("global.filters", List.of(Selector.from("name", "filter4-8")));
    Map<String, Object> componentMap = u.resolve("component", List.of());

    // Test that the global parameters fields are in the component's parameters
    for(String key : globalMap.keySet()){
      Assertions.assertEquals(globalMap.get(key), componentMap.get(key));
    }

    // Make sure there aren't any references in the resolved parameters
    for(String key : componentMap.keySet()){
      Assertions.assertFalse(key.startsWith(ConfigurationReference.REF_COMMAND));
    }

    // Test that none of the component's non-reference fields changed
    // If the fields in yaml file changes - this test will fail
    Assertions.assertEquals(true, componentMap.get("binary"));
    Assertions.assertEquals(1.0, componentMap.get("numeric"));
    Assertions.assertEquals("string", componentMap.get("string"));


    System.out.println(ObjectSerialization.getObjectMapper().writeValueAsString(componentMap));
  }
}
