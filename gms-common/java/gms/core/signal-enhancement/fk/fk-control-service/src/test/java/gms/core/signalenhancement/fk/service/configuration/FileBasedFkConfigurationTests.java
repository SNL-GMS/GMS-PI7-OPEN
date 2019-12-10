package gms.core.signalenhancement.fk.service.configuration;

import gms.core.signalenhancement.fk.control.configuration.FileBasedFkConfiguration;
import gms.core.signalenhancement.fk.control.configuration.FkSpectraParameters;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.RegistrationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBasedFkConfigurationTests {

  private static final String DEFINITION_FILE = "gms/core/signalenhancement/fk/service/definition.json";
  private static final String CHANNEL_IDS_FILE = "gms/core/signalenhancement/fk/service/channelIds.json";
  private static final String RELATIVE_POSITIONS_FILE = "gms/core/signalenhancement/fk/service/relativePositions.json";
  private static final String OUTPUT_IDS_FILE = "gms/core/signalenhancement/fk/service/outputIds.json";
  private URL definitionUrl;
  private URL channelIdsUrl;
  private URL relativePositionUrl;
  private URL outputIdsUrl;

  private FileBasedFkConfiguration fkConfiguration;
  private FkSpectraDefinition expectedDefinition;
  private RegistrationInfo expectedSpectraPluginInfo;
  private RegistrationInfo expectedAttributesPluginInfo;

  @BeforeEach
  public void setUp() {

    RelativePosition relativePosition = RelativePosition
        .from(0.03, 0.041, 0.0);
    UUID channelId = UUID.fromString("26dbb618-9558-378f-b6c3-497e88fe76ae");

    expectedDefinition = FkSpectraDefinition.builder()
        .setSampleRateHz(40.0)
        .setWindowLead(Duration.ZERO)
        .setWindowLength(Duration.ofSeconds(4))
        .setLowFrequencyHz(1.0)
        .setHighFrequencyHz(2.0)
        .setUseChannelVerticalOffsets(false)
        .setNormalizeWaveforms(false)
        .setPhaseType(PhaseType.P)
        .setSlowStartXSecPerKm(-0.4)
        .setSlowDeltaXSecPerKm(0.01)
        .setSlowCountX(81)
        .setSlowStartYSecPerKm(-0.4)
        .setSlowDeltaYSecPerKm(0.01)
        .setSlowCountY(81)
        .setWaveformSampleRateHz(40.0)
        .setWaveformSampleRateToleranceHz(1.0E-4)
        .setBeamPoint(Location.from(46.79368, 82.29057, 0.0, 0.618))
        .setRelativePositionsByChannelId(Map.of(channelId, relativePosition))
        .setMinimumWaveformsForSpectra(2)
        .build();

    expectedSpectraPluginInfo = RegistrationInfo.create("caponFkSpectraPlugin", 1, 0, 0);
    expectedAttributesPluginInfo = RegistrationInfo.create("maxPowerFkAttributesPlugin", 1, 0, 0);

    definitionUrl = Thread.currentThread().getContextClassLoader().getResource(
        DEFINITION_FILE);
    channelIdsUrl = Thread.currentThread().getContextClassLoader().getResource(
        CHANNEL_IDS_FILE);
    relativePositionUrl = Thread.currentThread().getContextClassLoader().getResource(
        RELATIVE_POSITIONS_FILE);
    outputIdsUrl = Thread.currentThread().getContextClassLoader().getResource(
        OUTPUT_IDS_FILE);

    fkConfiguration = FileBasedFkConfiguration
        .from(definitionUrl, channelIdsUrl, relativePositionUrl,
            outputIdsUrl);
  }

  @Test
  public void testFromValidation() {
    NullPointerException expected = assertThrows(NullPointerException.class,
        () -> FileBasedFkConfiguration
            .from(null, channelIdsUrl, relativePositionUrl, outputIdsUrl));
    assertEquals("FileBasedFkConfiguration cannot be created from a null Definitions URL",
        expected.getMessage());

    expected = assertThrows(NullPointerException.class,
        () -> FileBasedFkConfiguration
            .from(definitionUrl, null, relativePositionUrl, outputIdsUrl));
    assertEquals("FileBasedFkConfiguration cannot be created from a null Channel Ids URL",
        expected.getMessage());

    expected = assertThrows(NullPointerException.class,
        () -> FileBasedFkConfiguration
            .from(definitionUrl, channelIdsUrl, null, outputIdsUrl));
    assertEquals("FileBasedFkConfiguration cannot be created from a null Relative Positions URL",
        expected.getMessage());

    expected = assertThrows(NullPointerException.class,
        () -> FileBasedFkConfiguration
            .from(definitionUrl, channelIdsUrl, relativePositionUrl, null));
    assertEquals("FileBasedFkConfiguration cannot be created from a null Output Ids URL",
        expected.getMessage());

    FileBasedFkConfiguration configurationUtility = FileBasedFkConfiguration
        .from(definitionUrl, channelIdsUrl, relativePositionUrl, outputIdsUrl);
    assertNotNull(configurationUtility);
  }

  @Test
  public void testGetSpectraDefinition() {
    UUID stationId = UUID.fromString("850ed2e3-cc07-3c11-8b36-05ac5bc9e93a");
    Optional<FkSpectraDefinition> definition = fkConfiguration
        .getSpectraDefinition(stationId, PhaseType.P);
    assertTrue(definition.isPresent());
    assertEquals(expectedDefinition, definition.get());
  }

  @Test
  public void testGetSpectraDefinitionChannelNotFound() {
    assertThrows(IllegalStateException.class, () -> fkConfiguration
        .getSpectraDefinition(new UUID(0, 0),
            PhaseType.P));
  }

  @Test
  public void testCreateParameters() {
    FkSpectraParameters parameters = fkConfiguration
        .createFkSpectraParameters(UUID.fromString("850ed2e3-cc07-3c11-8b36-05ac5bc9e93a"),
            PhaseType.P);

    assertEquals(expectedDefinition, parameters.getDefinition());
    assertEquals(expectedSpectraPluginInfo.getName(), parameters.getPluginName());
  }


}
