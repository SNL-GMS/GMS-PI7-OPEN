package gms.core.signalenhancement.beam.core;

import gms.core.signalenhancement.beam.osd.client.OsdClient;
import gms.core.signalenhancement.beamcontrol.plugin.BeamPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginRegistry;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.RegistrationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ProcessingGroupDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeamControl {

  private static final Logger logger = LoggerFactory.getLogger(BeamControl.class);

  private final PluginRegistry<BeamPlugin> registry;
  private final OsdClient osdAccess;
  private BeamConfiguration configuration;

  private boolean initialized;

  /**
   * Factory method for creating a BeamControl
   *
   * @param registry          {@link PluginRegistry}, not null
   * @param osdAccessLibrary  {@link OsdClient} OSD access library, not null
   * @param beamConfiguration {@link BeamConfiguration}, not null
   * @return a new BeamControl object
   */
  public static BeamControl create(PluginRegistry<BeamPlugin> registry,
      OsdClient osdAccessLibrary, BeamConfiguration beamConfiguration) {

    Objects.requireNonNull(registry,
        "Error creating BeamControl: registry cannot be null");
    Objects.requireNonNull(osdAccessLibrary,
        "Error creating BeamControl: osdAccessLibrary cannot be null");
    Objects.requireNonNull(beamConfiguration,
        "Error creating BeamControl: beamConfiguration cannot be null");

    return new BeamControl(registry, osdAccessLibrary, beamConfiguration);
  }

  /**
   * Constructs a BeamControl with provided plugin registry and OSD access library.
   *
   * @param registry          {@link PluginRegistry}, not null
   * @param osdAccessLibrary  {@link OsdClient} OSD access library, not null
   * @param beamConfiguration {@link BeamConfiguration}, not null
   */
  private BeamControl(PluginRegistry<BeamPlugin> registry,
      OsdClient osdAccessLibrary, BeamConfiguration beamConfiguration) {

    this.registry = registry;
    this.osdAccess = osdAccessLibrary;
    this.configuration = beamConfiguration;
    this.initialized = false;
  }

  /**
   * Initialization method used to set configuration for control class and its bound plugins.
   */
  public void initialize() {
    // TODO: is separate initialize() called by client necessary? Depends on SystemControl pattern.

    logger.info("Loading configuration for plugins");
    registry.entrySet().forEach(e -> e.getPlugin()
        .initialize(osdAccess.loadPluginConfiguration(e.getRegistration())));

    initialized = true;
  }

  /**
   * Execute Waveform qc processing using the provided {@link ProcessingGroupDescriptor}
   *
   * @param descriptor object describing the beam processing request, not null
   * @return list of {@link UUID} to generated {@link ChannelSegment}, not null
   */
  public List<ChannelSegment<Waveform>> executeClaimCheck(ProcessingGroupDescriptor descriptor) {

    if (!initialized) {
      throw new IllegalStateException(
          "BeamControl must be initialized before execution");
    }

    Objects
        .requireNonNull(descriptor, "BeamControl cannot executeStreaming a null ClaimCheck");

    logger
        .info(
            "BeamControl executing claim check command for Processing Group Id {} with start time" +
                " {} and end time {}",
            descriptor.getProcessingGroupId(), descriptor.getStartTime(), descriptor.getEndTime());

    // Get the beam definitions
    final List<BeamDefinition> beamDefinitions = configuration
        .getAutomaticBeamDefinitions(descriptor);
    if (beamDefinitions.isEmpty()) {
      throw new IllegalStateException(
          "No BeamDefinitionAndChannelIdPair for channel " + descriptor.getProcessingGroupId());
    }

    return beamDefinitions.stream()
        .map(beamDefinition -> processBeam(beamDefinition, descriptor))
        .flatMap(Optional::stream)
        .collect(Collectors.toList());
  }

  public void storeWaveforms(List<ChannelSegment<Waveform>> channelSegments) {
    osdAccess.store(channelSegments);
  }

  private Optional<ChannelSegment<Waveform>> processBeam(BeamDefinition beamDefinition,
      ProcessingGroupDescriptor descriptor) {

    ChannelProcessingGroup channelProcessingGroup = osdAccess
        .loadChannelProcessingGroup(descriptor.getProcessingGroupId());
    List<ChannelSegment<Waveform>> rawWaveforms = osdAccess
        .loadChannelSegments(descriptor.getProcessingGroupId(), descriptor.getStartTime(),
            descriptor.getEndTime());
    BeamPlugin plugin = getPlugin(configuration.getAutomaticPluginRegistrationInfo(descriptor));
    String beamName = String.format("%s/%s-%s", channelProcessingGroup.getComment(), beamDefinition.getAzimuth(), beamDefinition.getSlowness());
    return calculateBeam(plugin, beamDefinition, rawWaveforms, beamName,
        UUID.nameUUIDFromBytes(beamName.getBytes()));
  }

  /**
   * Execute beam forming using the provided {@link BeamStreamingCommand}
   *
   * @param command object describing the beam processing request, not null
   * @return list of generated {@link ChannelSegment}, not null
   */
  public List<ChannelSegment<Waveform>> executeStreaming(BeamStreamingCommand command) {
    if (!initialized) {
      throw new IllegalStateException(
          "BeamControl must be initialized before execution");
    }

    Objects.requireNonNull(command,
        "BeamControl cannot executeStreaming a null BeamStreamingCommand");

    logger.info(
        "BeamControl executing streaming command: UUID: {} Beam definition: {} # of waveforms: {}",
        command.getOutputChannelId(),
        command.getBeamDefinition(),
        command.getWaveforms().size());

    logger.info(
        "BeamControl executing streaming command for channel segment {}",
        command.getOutputChannelId());

    final BeamPlugin beamPlugin = getPlugin(configuration.getInteractivePluginRegistrationInfo());
    Optional<ChannelSegment<Waveform>> waveform = calculateBeam(
        beamPlugin,
        command.getBeamDefinition(),
        command.getWaveforms(), "streaming",
        command.getOutputChannelId());

    return waveform.stream().collect(Collectors.toList());
  }

  /**
   * Creates a new {@link ChannelSegment} as results from beaming from the input {@link
   * ChannelSegment} using the supplied {@link BeamPlugin}. The new ChannelSegment is defined using
   * the a new output channel id that is not the same as the channel id referenced by the input
   * {@link ChannelSegment}.
   *
   * @param plugin            The plugin beam algorithm
   * @param beamDefinition    The beam definition identifying window lead and length, low/high
   *                          frequencies, sample rate, etc. used by the beam plugin
   * @param channelSegments   The input ChannelSegment supplying the waveforms as input to beam
   *                          Spectrum
   * @param outputChannelName
   * @return The new ChannelSegment of type waveform
   */
  private Optional<ChannelSegment<Waveform>> calculateBeam(BeamPlugin plugin,
      BeamDefinition beamDefinition,
      Collection<ChannelSegment<Waveform>> channelSegments,
      String outputChannelName, UUID outputChannelId) {
    logger.info("Performing beaming on {} ChannelSegments", channelSegments.size());

    List<Waveform> beams = plugin.beam(channelSegments, beamDefinition);
    if (beams.isEmpty()) {
      logger.info("Beaming resulted in 0 waveforms");
      return Optional.empty();
    } else {
      return Optional.of(ChannelSegment.create(
          outputChannelId,
          outputChannelName,
          ChannelSegment.Type.DETECTION_BEAM,
          beams,
          CreationInfo.DEFAULT));
    }
  }

  /**
   * Returns the {@link BeamPlugin} for the input registrationInfo
   *
   * @param registrationInfo set of {@link RegistrationInfo}, not null
   * @return {@link BeamPlugin}, not null
   * @throws IllegalStateException if the registrationInfo does not correspond to a plugin
   *                               registered in {@link BeamControl#registry}
   */
  private BeamPlugin getPlugin(RegistrationInfo registrationInfo) {
    return registry.lookup(registrationInfo).orElseThrow(() -> new IllegalStateException(
        "Cannot executeStreaming beam. Missing plugin found for: " + registrationInfo));
  }
}
