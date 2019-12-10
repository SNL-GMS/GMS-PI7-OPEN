package gms.core.waveformqc.waveformqccontrol.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import gms.core.waveformqc.waveformqccontrol.configuration.QcConfiguration;
import gms.core.waveformqc.waveformqccontrol.configuration.QcParameters;
import gms.core.waveformqc.waveformqccontrol.configuration.QcParametersFile;
import gms.shared.mechanisms.configuration.Configuration;
import gms.shared.mechanisms.configuration.ConfigurationOption;
import gms.shared.mechanisms.configuration.ConfigurationRepository;
import gms.shared.mechanisms.configuration.client.GlobalConfigurationReferenceResolver;
import gms.shared.mechanisms.configuration.constraints.DefaultConstraint;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QcConfigurationTest {

  @Mock
  private ConfigurationRepository mockConfigurationRepository;

  @Mock
  private Configuration mockConfiguration;

  @Test
  void testCreateValidation() {
    assertThrows(NullPointerException.class, () -> QcConfiguration.create(null));
  }

  @Test
  void testCreate() {
    when(mockConfigurationRepository.getKeyRange(QcConfiguration.QC_PREFIX))
        .thenReturn(List.of(mockConfiguration));

    when(mockConfiguration.getName()).thenReturn("mockConfigName");
    when(mockConfiguration.getConfigurationOptions()).thenReturn(ImmutableList.of());
    when(mockConfiguration.getChangeTime()).thenReturn(Instant.EPOCH);

    final QcConfiguration configuration = QcConfiguration.create(mockConfigurationRepository);
    assertNotNull(configuration);

    verify(mockConfigurationRepository, times(1))
        .getKeyRange(QcConfiguration.QC_PREFIX);
  }

  @Test
  void testGetPluginConfiguration() {

    final QcParametersFile qcParametersFile = QcParametersFile
        .from(List.of(QcParameters.from(
            "testPluginName",
            Map.of("testParam1", true, "testParam2", 1234))));

    final Collection<Configuration> configurations =
        List.of(Configuration.from(QcConfiguration.DEFAULT_PLUGIN_CONFIGURATION,
            List.of(ConfigurationOption.from(
                "PLUGIN_CONFIGS",
                List.of(DefaultConstraint.from()),
                ObjectSerialization.toFieldMap(qcParametersFile)))));

    final Predicate<InvocationOnMock> isPrefixQuery = invocation ->
        QcConfiguration.QC_PREFIX.equals(invocation.getArguments()[0]);

    final Predicate<InvocationOnMock> isGlobalQuery = invocation ->
        GlobalConfigurationReferenceResolver.REFERENCED_CONFIGURATION_KEY_PREFIX.equals(invocation.getArguments()[0]);

    when(mockConfigurationRepository.getKeyRange(anyString()))
        .thenAnswer(invocation -> {
          if (isPrefixQuery.test(invocation)) {
            return configurations;
          }

          if (isGlobalQuery.test(invocation)) {
            return List.of();
          }

          throw new InvalidUseOfMatchersException(
              String.format("Arguments %s do not match", Arrays.toString(invocation.getArguments()))
          );
        });

    final QcConfiguration config = QcConfiguration.create(mockConfigurationRepository);
    final List<QcParameters> qcParametersList = config
        .getPluginConfigurations();

    assertNotNull(qcParametersList);
    assertEquals(1, qcParametersList.size());
    QcParameters qcParameters = qcParametersList.get(0);
    assertNotNull(qcParameters);
    assertNotNull(qcParameters.getPluginName());
    assertNotNull(qcParameters.getPluginParams());
    assertEquals(2, qcParameters.getPluginParams().size());
  }

}
