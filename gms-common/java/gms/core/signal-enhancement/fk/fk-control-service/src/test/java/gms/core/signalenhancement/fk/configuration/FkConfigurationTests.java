package gms.core.signalenhancement.fk.configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import gms.core.signalenhancement.fk.control.configuration.FkAttributesParameters;
import gms.core.signalenhancement.fk.control.configuration.FkConfiguration;
import gms.core.signalenhancement.fk.control.configuration.FkSpectraParameters;
import gms.shared.mechanisms.configuration.FileConfigurationRepository;
import java.io.File;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FkConfigurationTests {

  private static String configurationBase;

  private static final UUID pdarStationUuid = UUID
      .fromString("3308666b-f9d8-3bff-a59e-928730ffa797");

  @BeforeAll
  public static void setUp() {
    configurationBase = Thread.currentThread().getContextClassLoader()
        .getResource("gms/core/signalenhancement/fkcontrol/configuration-base/")
        .getPath();
  }

  @Test
  void testCreate() {

    assertDoesNotThrow(() -> FkConfiguration
        .create(FileConfigurationRepository.create(new File(configurationBase).toPath())));
  }

  @Test
  void testGetFkSpectraParameters() {

    FkConfiguration configuration = FkConfiguration
        .create(FileConfigurationRepository.create(new File(configurationBase).toPath()));

    FkSpectraParameters parameters = configuration.getFkSpectraParameters(pdarStationUuid);

    assertNotNull(parameters);
  }

  @Test
  void testGetFkAttributesParameters() {
    FkConfiguration configuration = FkConfiguration
        .create(FileConfigurationRepository.create(new File(configurationBase).toPath()));

    List<FkAttributesParameters> parametersList = configuration
        .getFkAttributesParameters(pdarStationUuid);

    assertNotNull(parametersList);
    assertEquals(1, parametersList.size());
  }

}
