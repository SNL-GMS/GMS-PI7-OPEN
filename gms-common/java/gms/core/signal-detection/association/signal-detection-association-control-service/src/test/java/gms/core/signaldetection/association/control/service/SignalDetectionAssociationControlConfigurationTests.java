package gms.core.signaldetection.association.control.service;

import gms.shared.mechanisms.configuration.Configuration;
import gms.shared.mechanisms.configuration.ConfigurationOption;
import gms.shared.mechanisms.configuration.ConfigurationRepository;
import gms.shared.mechanisms.configuration.client.GlobalConfigurationReferenceResolver;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SignalDetectionAssociationControlConfigurationTests {

  @Mock
  private ConfigurationRepository mockConfigurationRepository;

  @Mock
  private Configuration mockConfiguration;

  private static final Predicate<InvocationOnMock> isPrefixQuery = invocation ->
      SignalDetectionAssociationControlConfiguration.SIGNAL_DETECTION_ASSOCIATION_PLUGIN_PREFIX
          .equals(invocation.getArguments()[0]);

  private static final Predicate<InvocationOnMock> isGlobalReferenceQuery = invocation ->
      GlobalConfigurationReferenceResolver.REFERENCED_CONFIGURATION_KEY_PREFIX
          .equals(invocation.getArguments()[0]);

  @Test
  void testCreate() {
    mockConfigurationRepositoryDefaults();
    // Mock Configuration
    final SignalDetectionAssociationControlConfiguration config = SignalDetectionAssociationControlConfiguration
        .create(mockConfigurationRepository);
    Assertions.assertNotNull(config);

    Mockito.verify(mockConfigurationRepository, Mockito.times(1))
        .getKeyRange(
            SignalDetectionAssociationControlConfiguration.SIGNAL_DETECTION_ASSOCIATION_PLUGIN_PREFIX);
  }

  @Test
  void testCreateThrowsExceptionWhenNullConfigurationRepositoryIsPassed() {
    final NullPointerException exception = Assertions.assertThrows(NullPointerException.class,
        () -> SignalDetectionAssociationControlConfiguration.create(null));

    Assertions.assertTrue(exception.getMessage()
        .contains(
            "SignalDetectionAssociatorConfiguration can not be instantiated with a null ConfigurationRepository object."));
  }

  @Test
  void testGetDefaultAssociatorParameters() {
    String expectedModelFileName = "earth.model";
    String expectedPluginName = "default-plugin";
    String expectedPluginVersionInfo = "1.0.0";
    int expectedGridSpacing = 1000;
    int expectedMaxStationsPerGrid = 5;
    double expectedSigmaSlowness = 0.05;
    List<String> expectedPhases = List.of("P");
    List<String> expectedForwardTransformationPhases = List.of("P");
    double expectedBeliefThreshold = 0.05;
    boolean expectedPrimaryPhaseRequiredForSecondary = true;
    double expectedSigmaTime = 0.10;
    double expectedChiLimit = 0.75;
    boolean expectedFreezeArrivalsAtBeamPoints = true;
    double expectedGridCylinderRadiusDegrees = 1.0;
    double expectedGridCylinderDepthKm = 50.0;
    double expectedGridCylinderHeightKm = 100.0;
    double expectedMinimumMagnitude = 3.5;
    int expectednumFirstSta = 5;

    final SignalDetectionAssociatorConfigurationParameters expectedParams = SignalDetectionAssociatorConfigurationParameters
        .from(
        expectedPluginName,
        expectedPluginVersionInfo,
        expectedModelFileName,
        expectedGridSpacing,
        expectedMaxStationsPerGrid,
        expectedSigmaSlowness,
        expectedPhases,
        expectedForwardTransformationPhases,
        expectedBeliefThreshold,
        expectedPrimaryPhaseRequiredForSecondary,
        expectedSigmaTime,
        expectedChiLimit,
        expectedFreezeArrivalsAtBeamPoints,
        expectedGridCylinderRadiusDegrees,
        expectedGridCylinderDepthKm,
        expectedGridCylinderHeightKm,
        expectedMinimumMagnitude,
        expectednumFirstSta);

    final Collection<Configuration> configurations = List.of(
        Configuration.from(
            SignalDetectionAssociationControlConfiguration.SIGNAL_DETECTION_DEFAULT_PLUGIN_PREFIX,
            List.of(ConfigurationOption.from("default-plugin", List.of(),
                ObjectSerialization.toFieldMap(expectedParams)))));

    // mock attempting to grab the associator parameters from YAML file
    Mockito.when(mockConfigurationRepository.getKeyRange(Mockito.anyString()))
        .thenAnswer(invocation -> {
          if (isPrefixQuery.test(invocation)) {
            return configurations;
          } else if (isGlobalReferenceQuery.test(invocation)) {
            return List.of();
          }
          throw new InvalidUseOfMatchersException(
              String.format("Arguments %s do not match", Arrays.toString(invocation.getArguments()))
          );
        });

    final SignalDetectionAssociationControlConfiguration config = SignalDetectionAssociationControlConfiguration
        .create(mockConfigurationRepository);
    final SignalDetectionAssociatorConfigurationParameters params = config.getParams();

    Assertions.assertAll(
        () -> Assertions.assertEquals(expectedModelFileName, params.getGridModelFileName()),
        () -> Assertions.assertEquals(expectedGridSpacing, params.getGridSpacing()),
        () -> Assertions.assertEquals(expectedPluginName, params.getPluginName()),
        () -> Assertions.assertEquals(expectedPluginVersionInfo, params.getPluginVersion()),
        () -> Assertions.assertEquals(expectedModelFileName, params.getGridModelFileName()),
        () -> Assertions.assertEquals(expectedGridSpacing, params.getGridSpacing()),
        () -> Assertions.assertEquals(expectedMaxStationsPerGrid, params.getMaxStationsPerGrid()),
        () -> Assertions.assertEquals(expectedSigmaSlowness, params.getSigmaSlowness()),
        () -> Assertions.assertEquals(expectedPhases, params.getPhases()),
        () -> Assertions.assertEquals(expectedForwardTransformationPhases,
            params.getForwardTransformationPhases()),
        () -> Assertions.assertEquals(expectedBeliefThreshold, params.getBeliefThreshold()),
        () -> Assertions.assertEquals(expectedPrimaryPhaseRequiredForSecondary,
            params.getPrimaryPhaseRequiredForSecondary()),
        () -> Assertions.assertEquals(expectedSigmaTime, params.getSigmaTime()),
        () -> Assertions.assertEquals(expectedChiLimit, params.getChiLimit()),
        () -> Assertions.assertEquals(expectedFreezeArrivalsAtBeamPoints,
            params.getFreezeArrivalsAtBeamPoints()),
        () -> Assertions.assertEquals(expectedGridCylinderRadiusDegrees, params.getGridCylinderRadiusDegrees()),
        () -> Assertions.assertEquals(expectedGridCylinderDepthKm, params.getGridCylinderDepthKm()),
        () -> Assertions.assertEquals(expectedGridCylinderHeightKm, params.getGridCylinderHeightKm()),
        () -> Assertions.assertEquals(expectedMinimumMagnitude, params.getMinimumMagnitude()),
        () -> Assertions.assertEquals(expectednumFirstSta, params.getNumFirstSta())
    );
  }

  @Test
  void testWeightedEventCriterionPluginParameters() throws IOException {
    String pluginName = "name";
    String pluginVersion = "version";
    String primaryTimeWeight = "primaryTimeWeight";
    String secondaryTimeWeight = "secondaryTimeWeight";
    String arrayAzimuthWeight = "arrayAzimuthWeight";
    String threeComponentAzimuthWeight= "threeComponentAzimuthWeight";
    String arraySlowWeight = "arraySlowWeight";
    String threeComponentSlowWeight = "threeComponentSlowWeight";
    String weightThreshold = "weightThreshold";
    double expectedPrimaryTimeWeight = 0.65;
    double expectedSecondaryTimeWeight = 0.65;
    double expectedArrayAzimuthWeight = 0.65;
    double expectedthreeComponentAzimuthWeight = 0.65;
    double expectedarraySlowWeight = 0.65;
    double expectedthreeComponentSlowWeight = 0.65;
    double expectedweightThreshold = 0.65;

    Map<String, Object> mockMap = Map.of(
        pluginName, "testPlugin",
        pluginVersion, "1.0.0",
        primaryTimeWeight, expectedPrimaryTimeWeight,
        secondaryTimeWeight, expectedSecondaryTimeWeight,
        arrayAzimuthWeight, expectedArrayAzimuthWeight,
        threeComponentAzimuthWeight, expectedthreeComponentAzimuthWeight,
        arraySlowWeight, expectedarraySlowWeight,
        threeComponentSlowWeight, expectedthreeComponentSlowWeight,
        weightThreshold, expectedweightThreshold
    );

    final Collection<Configuration> configurations = List.of(
        Configuration.from(
            SignalDetectionAssociationControlConfiguration.SIGNAL_DETECTION_WEIGHTED_EVENT_DEFAULT_PLUGIN_PREFIX,
            List.of(ConfigurationOption.from("default-arrival-quality-plugin", List.of(),
                ObjectSerialization.toFieldMap(mockMap)))));

    // mock attempting to grab the associator parameters from YAML file
    Mockito.when(mockConfigurationRepository.getKeyRange(Mockito.anyString()))
        .thenAnswer(invocation -> {
          if (isPrefixQuery.test(invocation)) {
            return configurations;
          } else if (isGlobalReferenceQuery.test(invocation)) {
            return List.of();
          }
          throw new InvalidUseOfMatchersException(
              String.format("Arguments %s do not match", Arrays.toString(invocation.getArguments()))
          );
        });

    final SignalDetectionAssociationControlConfiguration config = SignalDetectionAssociationControlConfiguration
        .create(mockConfigurationRepository);

    final WeightedEventCriteriaConfigurationParameters params = config.getWeightedEventPluginParams();

    Assertions.assertAll(
        () -> Assertions.assertEquals(expectedPrimaryTimeWeight, params.getPrimaryTimeWeight()),
        () -> Assertions.assertEquals(expectedSecondaryTimeWeight, params.getSecondaryTimeWeight()),
        () -> Assertions.assertEquals(expectedArrayAzimuthWeight, params.getArrayAzimuthWeight()),
        () -> Assertions.assertEquals(expectedthreeComponentAzimuthWeight,
            params.getThreeComponentAzimuthWeight()),
        () -> Assertions.assertEquals(expectedarraySlowWeight, params.getArraySlowWeight()),
        () -> Assertions
            .assertEquals(expectedthreeComponentSlowWeight, params.getThreeComponentSlowWeight()),
        () -> Assertions.assertEquals(expectedweightThreshold, params.getWeightThreshold())
    );

  }

  @Test
  void testArrivalQualityCriterionPluginParameters() throws IOException {
    String pluginName = "name";
    String pluginVersion = "version";
    double expectedarQualAlpha = 0.65;
    double expectedarQualBeta = 0.65;
    double expectedarQualGamma = 0.65;
    double expectedarQualThreshold = 0.65;
    Map<String, Object> mockMap = Map.of(
        pluginName, "testPlugin",
        pluginVersion, "1.0.0",
        "arrivalQualityAlpha", expectedarQualAlpha,
        "arrivalQualityBeta", expectedarQualBeta,
        "arrivalQualityGamma", expectedarQualGamma,
        "arrivalQualityThreshold", expectedarQualThreshold
    );

    final Collection<Configuration> configurations = List.of(
        Configuration.from(
            SignalDetectionAssociationControlConfiguration.SIGNAL_DETECTION_ARRIVAL_QUALITY_DEFAULT_PLUGIN_PREFIX,
            List.of(ConfigurationOption.from("default-arrival-quality-plugin", List.of(),
                ObjectSerialization.toFieldMap(mockMap)))));

    // mock attempting to grab the associator parameters from YAML file
    Mockito.when(mockConfigurationRepository.getKeyRange(Mockito.anyString()))
        .thenAnswer(invocation -> {
          if (isPrefixQuery.test(invocation)) {
            return configurations;
          } else if (isGlobalReferenceQuery.test(invocation)) {
            return List.of();
          }
          throw new InvalidUseOfMatchersException(
              String.format("Arguments %s do not match", Arrays.toString(invocation.getArguments()))
          );
        });

    final SignalDetectionAssociationControlConfiguration config = SignalDetectionAssociationControlConfiguration
        .create(mockConfigurationRepository);

    final ArrivalQualityCriteriaConfigurationParameters params = config.getArrivalQualityPluginParams();

    Assertions.assertAll(
        () -> Assertions.assertEquals("testPlugin", params.getName()),
        () -> Assertions.assertEquals("1.0.0", params.getVersion()),
        () -> Assertions.assertEquals(expectedarQualAlpha, params.getArrivalQualityAlpha()),
        () -> Assertions.assertEquals(expectedarQualBeta, params.getArrivalQualityBeta()),
        () -> Assertions.assertEquals(expectedarQualGamma, params.getArrivalQualityGamma()),
        () -> Assertions.assertEquals(expectedarQualThreshold, params.getArrivalQualityThreshold())
        );

  }

  private void mockConfigurationRepositoryDefaults() {
    Mockito.when(mockConfigurationRepository.getKeyRange(Mockito.anyString()))
        .thenAnswer(invocation -> {
          if (isPrefixQuery.test(invocation)) {
            return List.of(Configuration.from(
                SignalDetectionAssociationControlConfiguration.SIGNAL_DETECTION_DEFAULT_PLUGIN_PREFIX,
                List.of()));
          } else if (isGlobalReferenceQuery.test(invocation)) {
            return List.of();
          }
          throw new InvalidUseOfMatchersException(
              String.format("Arguments %s do not match", Arrays.toString(invocation.getArguments()))
          );
        });
  }
}
