package gms.core.signaldetection.signaldetectorcontrol.configuration;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import gms.core.signaldetection.onsettimerefinement.AicOnsetTimeRefinementParameters;
import gms.core.signaldetection.snronsettimeuncertainty.SnrOnsetTimeUncertaintyParameters;
import gms.core.signaldetection.staltapowerdetector.StaLtaParameters;
import gms.shared.mechanisms.configuration.FileConfigurationRepository;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import java.io.File;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SignalDetectorConfigurationTests {

  private static String configurationBase;

  @BeforeAll
  public static void setUp() {
    configurationBase = Thread.currentThread().getContextClassLoader()
        .getResource("gms/core/signaldetection/signaldetectorcontrol/configuration-base/")
        .getPath();
  }

  @Test
  void testCreate() {

    assertDoesNotThrow(() -> SignalDetectorConfiguration
        .create(FileConfigurationRepository.create(new File(configurationBase).toPath())));
  }

  @Test
  void testGetSignalDetectionParameters() {

    SignalDetectorConfiguration configuration = SignalDetectorConfiguration
        .create(FileConfigurationRepository.create(new File(configurationBase).toPath()));

    List<SignalDetectionParameters> parametersList = configuration
        .getSignalDetectionParameters(UUID.randomUUID());

    assertNotNull(parametersList);
    assertEquals(1, parametersList.size());

    List<StaLtaParameters> staLtaParametersList = parametersList.stream().map(
        parameters -> ObjectSerialization
            .fromFieldMap(parameters.getPluginParameters(), StaLtaParameters.class))
        .collect(toList());

    assertNotNull(staLtaParametersList);
    assertEquals(1, staLtaParametersList.size());
  }

  @Test
  void testGetOnsetTimeUncertaintyParameters() {
    SignalDetectorConfiguration configuration = SignalDetectorConfiguration
        .create(FileConfigurationRepository.create(new File(configurationBase).toPath()));

    OnsetTimeUncertaintyParameters parameters = configuration
        .getOnsetTimeUncertaintyParameters();

    assertNotNull(parameters);

    SnrOnsetTimeUncertaintyParameters uncertaintyParameters = ObjectSerialization
        .fromFieldMap(parameters.getPluginParameters(), SnrOnsetTimeUncertaintyParameters.class);

    assertNotNull(uncertaintyParameters);
  }

  @Test
  void testGetOnsetTimeRefinementParameters() {
    SignalDetectorConfiguration configuration = SignalDetectorConfiguration
        .create(FileConfigurationRepository.create(new File(configurationBase).toPath()));

    OnsetTimeRefinementParameters parameters = configuration
        .getOnsetTimeRefinementParameters();

    assertNotNull(parameters);

    AicOnsetTimeRefinementParameters refinementParameters = ObjectSerialization
        .fromFieldMap(parameters.getPluginParameters(), AicOnsetTimeRefinementParameters.class);

    assertNotNull(refinementParameters);
  }
}
