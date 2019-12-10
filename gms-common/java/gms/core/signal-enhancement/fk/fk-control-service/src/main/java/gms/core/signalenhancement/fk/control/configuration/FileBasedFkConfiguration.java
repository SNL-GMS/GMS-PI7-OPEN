package gms.core.signalenhancement.fk.control.configuration;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import gms.core.signalenhancement.fk.control.FkSpectraCommand;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.RegistrationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * An FkConfiguration implementation that uses a file to provide the configuration
 * information
 */
public class FileBasedFkConfiguration {

  private final FkSpectraDefinition defaultDefinition;
  private final Map<UUID, List<UUID>> channelIdsByStationId;
  private final Map<UUID, RelativePosition> relativePositionsByChannelId;
  private final Map<UUID, UUID> outputChannelIdByStationId;

  public FileBasedFkConfiguration(
      FkSpectraDefinition defaultDefinition,
      Map<UUID, List<UUID>> channelIdsByStationId,
      Map<UUID, RelativePosition> relativePositionsByChannelId,
      Map<UUID, UUID> outputChannelIdByStationId) {
    this.defaultDefinition = defaultDefinition;
    this.channelIdsByStationId = channelIdsByStationId;
    this.relativePositionsByChannelId = relativePositionsByChannelId;
    this.outputChannelIdByStationId = outputChannelIdByStationId;
  }

  /**
   * Creates a new FileBasedFkConfiguration
   *
   * @param definitionsUrl the URL (not null) pointing to the file that will be used to provide
   * the default FkSpectraDefinition
   * @param channelIdsUrl the URL (not null) pointing to the file that provides channel ids given a
   * station id
   * @param relativePositionsUrl the URL (not null) pointing to the file that provides relative
   * positions given channel ids
   * @param outputIdsUrl the URL (not null) pointing to the file that provides the output channel
   * id given a station id
   * @return a new FileBasedFkConfiguration that uses the provided file to provide
   * configuration.
   */
  public static FileBasedFkConfiguration from(URL definitionsUrl, URL channelIdsUrl,
      URL relativePositionsUrl, URL outputIdsUrl) {
    Objects.requireNonNull(definitionsUrl,
        "FileBasedFkConfiguration cannot be created from a null Definitions URL");
    Objects.requireNonNull(channelIdsUrl,
        "FileBasedFkConfiguration cannot be created from a null Channel Ids URL");
    Objects.requireNonNull(relativePositionsUrl,
        "FileBasedFkConfiguration cannot be created from a null Relative Positions URL");
    Objects.requireNonNull(outputIdsUrl,
        "FileBasedFkConfiguration cannot be created from a null Output Ids URL");
    try {
      ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();
      TypeFactory typeFactory = mapper.getTypeFactory();

      JavaType channelIdsType = typeFactory.constructCollectionType(ArrayList.class,
          UUID.class);
      JavaType channelIdsKeyType = typeFactory.constructType(UUID.class);
      JavaType channelIdsMapType = typeFactory.constructMapType(HashMap.class,
          channelIdsKeyType, channelIdsType);

      JavaType relativePositionsMapType = typeFactory.constructMapType(HashMap.class,
          UUID.class, RelativePosition.class);
      JavaType outputIdsMapType = typeFactory.constructMapType(HashMap.class,
          UUID.class, UUID.class);
      return new FileBasedFkConfiguration(
          mapper.readValue(definitionsUrl, FkSpectraDefinition.class),
          mapper.readValue(channelIdsUrl, channelIdsMapType),
          mapper.readValue(relativePositionsUrl, relativePositionsMapType),
          mapper.readValue(outputIdsUrl, outputIdsMapType));
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  public Optional<RegistrationInfo> getSpectraPlugin(UUID stationId, PhaseType phase) {
    return Optional.of(getDefaultSpectraPlugin());
  }

  /**
   * Creates a new FkSpectraDefinition using pre-configured defaults and the relative positions
   * for each Channel ID
   * @param stationId The Channel IDs for the station providing the FkSpectraDefinition
   * @param phase The PhaseType for the station
   * @return
   */
  public Optional<FkSpectraDefinition> getSpectraDefinition(UUID stationId, PhaseType phase) {
    Preconditions.checkNotNull(stationId);
    Preconditions.checkNotNull(phase);

    Preconditions.checkState(channelIdsByStationId.containsKey(stationId),
        "Cannot build FkSpectraDefinition: no Channel Ids found for station %s", stationId);
    List<UUID> channelIds = channelIdsByStationId.get(stationId);

    Preconditions.checkState(
        channelIds.stream().allMatch(relativePositionsByChannelId::containsKey),
        "Cannot build FkSpectraDefinition: Channel ID without a corresponding RelativePosition found.");

    FkSpectraDefinition.Builder definitionBuilder  = defaultDefinition.toBuilder();
    definitionBuilder
        .setRelativePositionsByChannelId(mapRelativePositions(channelIds));
    return Optional.of(definitionBuilder.build());
  }

  public Optional<List<RegistrationInfo>> getAttributesPlugins(UUID stationId, PhaseType phase) {
    return Optional.of(List.of(getDefaultAttributesPlugin()));
  }

  public Optional<UUID> getOutputChannelId(UUID stationId, PhaseType phase) {
    Preconditions.checkNotNull(stationId);
    Preconditions.checkNotNull(phase);

    return Optional.of(outputChannelIdByStationId.get(stationId));
  }

  public RegistrationInfo getDefaultSpectraPlugin() {
    return RegistrationInfo.create("caponFkSpectraPlugin", 1, 0, 0);
  }

  public FkSpectraDefinition getDefaultSpectraDefinition() {
    return defaultDefinition;
  }

  public RegistrationInfo getDefaultAttributesPlugin() {
    return RegistrationInfo.create("maxPowerFkAttributesPlugin",1, 0, 0);
  }

  public UUID getDefaultOutputChannelId() {
    return new UUID(0, 0);
  }

  public Map<UUID, RelativePosition> mapRelativePositions(Collection<UUID> channelIds) {
    return ImmutableMap.<UUID, RelativePosition>builder()
        .putAll(Maps.filterKeys(relativePositionsByChannelId, channelIds::contains)).build();
  }

  /**
   * Creates {@link FkSpectraParameters} given input configuration values of station id, configuration
   * time, and phase of the signal detection
   *
   * @param stationId Id for the station
   * @param phase Phase of the signal detection
   * @return Parameters used to run FkAnalysis
   */
  public FkSpectraParameters createFkSpectraParameters(UUID stationId,
      PhaseType phase) {
    Optional<RegistrationInfo> spectraPlugin = getSpectraPlugin(stationId, phase);
    Optional<FkSpectraDefinition> spectraDefinition = getSpectraDefinition(stationId, phase);
    Optional<List<RegistrationInfo>> attributesPlugins = getAttributesPlugins(stationId, phase);
    Optional<UUID> outputChannelId = getOutputChannelId(stationId, phase);

    Preconditions.checkState(spectraPlugin.isPresent(),
        "Error retrieving FkParameters, no Spectra Plugin found for station:%s, time:%s, phase:%s",
        stationId, phase);
    Preconditions.checkState(spectraDefinition.isPresent(),
        "Error retrieving FkParameters, no Spectra Definition found for station:%s, time:%s, phase:%s",
        stationId, phase);
    Preconditions.checkState(attributesPlugins.isPresent(),
        "Error retrieving FkParameters, no Attributes Plugins found for station:%s, time:%s, phase:%s",
        stationId, phase);
    Preconditions.checkState(outputChannelId.isPresent(),
        "Error retrieving FkParameters, no Output Channel found for station:%s, time:%s, phase:%s",
        stationId, phase);

    return FkSpectraParameters.from(spectraPlugin.get().getName(), spectraDefinition.get(),
        outputChannelId.get());
  }

  /**
   * Creates {@link FkSpectraParameters} given input {@link FkSpectraCommand}
   *
   * @return Parameters used to run FkAnalysis
   */
  public FkSpectraParameters createFkSpectraParameters(FkSpectraCommand command, double waveformSampleRateHz) {
    RegistrationInfo spectraPlugin = getDefaultSpectraPlugin();
    FkSpectraDefinition spectraDefinition = getDefaultSpectraDefinition();
    UUID outputChannelId = getDefaultOutputChannelId();

    FkSpectraDefinition.Builder builder = spectraDefinition.toBuilder()
        .setSampleRateHz(command.getSampleRate())
        .setWindowLead(command.getWindowLead())
        .setWindowLength(command.getWindowLength())
        .setLowFrequencyHz(command.getLowFrequency())
        .setHighFrequencyHz(command.getHighFrequency())
        .setUseChannelVerticalOffsets(command.getUseChannelVerticalOffset())
        .setNormalizeWaveforms(command.getNormalizeWaveforms())
        .setPhaseType(command.getPhaseType())
        .setRelativePositionsByChannelId(mapRelativePositions(command.getChannelIds()))
        .setWaveformSampleRateHz(waveformSampleRateHz);

    command.getSlowStartX().ifPresent(builder::setSlowStartXSecPerKm);
    command.getSlowDeltaX().ifPresent(builder::setSlowDeltaXSecPerKm);
    command.getSlowCountX().ifPresent(builder::setSlowCountX);
    command.getSlowStartY().ifPresent(builder::setSlowStartYSecPerKm);
    command.getSlowDeltaY().ifPresent(builder::setSlowDeltaYSecPerKm);
    command.getSlowCountY().ifPresent(builder::setSlowCountY);

    return FkSpectraParameters.from(spectraPlugin.getName(), builder.build(), outputChannelId);
  }

  public List<FkAttributesParameters> createFkAttributesParameters(FkSpectraDefinition definition) {
    return List.of(FkAttributesParameters.from(
        getDefaultAttributesPlugin().getName(),
        Map.of("lowFrequency", definition.getLowFrequencyHz(),
            "highFrequency", definition.getHighFrequencyHz(),
            "eastSlowStart", definition.getSlowStartXSecPerKm(),
            "eastSlowDelta", definition.getSlowDeltaXSecPerKm(),
            "northSlowStart", definition.getSlowStartYSecPerKm(),
            "northSlowDelta", definition.getSlowDeltaYSecPerKm())));
  }
}
