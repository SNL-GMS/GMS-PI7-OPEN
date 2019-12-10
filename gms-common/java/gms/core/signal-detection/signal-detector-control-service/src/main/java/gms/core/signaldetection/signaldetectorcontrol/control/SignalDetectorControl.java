package gms.core.signaldetection.signaldetectorcontrol.control;

import static java.util.Map.entry;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.google.common.base.Preconditions;
import gms.core.signaldetection.onsettimerefinement.OnsetTimeRefinementPlugin;
import gms.core.signaldetection.onsettimeuncertainty.OnsetTimeUncertaintyPlugin;
import gms.core.signaldetection.plugin.SignalDetectorPlugin;
import gms.core.signaldetection.signaldetectorcontrol.coi.client.CoiRepository;
import gms.core.signaldetection.signaldetectorcontrol.configuration.OnsetTimeRefinementParameters;
import gms.core.signaldetection.signaldetectorcontrol.configuration.OnsetTimeUncertaintyParameters;
import gms.core.signaldetection.signaldetectorcontrol.configuration.SignalDetectionParameters;
import gms.core.signaldetection.signaldetectorcontrol.configuration.SignalDetectorConfiguration;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Plugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginRegistry;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.RegistrationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalDetectorControl {

  private static final Logger logger = LoggerFactory.getLogger(SignalDetectorControl.class);

  private static final Map<UUID, UUID> channelIdToStationIds;

  private final SignalDetectorConfiguration configuration;
  private final PluginRegistry<SignalDetectorPlugin> signalDetectorPluginRegistry;
  private final PluginRegistry<OnsetTimeUncertaintyPlugin> onsetTimeUncertaintyPluginRegistry;
  private final PluginRegistry<OnsetTimeRefinementPlugin> onsetTimeRefinementPluginRegistry;
  private final CoiRepository coiRepository;

  private boolean initialized;

  static {
    Map<String, UUID> stationIdMap = Map.ofEntries(
        entry("PDAR", UUID.fromString("3308666b-f9d8-3bff-a59e-928730ffa797")),
        entry("TXAR", UUID.fromString("565ca127-6d78-32ba-bdc9-ce05fc3b8ddf")),
        entry("ULN", UUID.fromString("c86ec6c1-26a6-335f-b2aa-be083d3e2081"))
    );

    List<UUID> pdarBeamChannelIds = Arrays.asList(
        UUID.fromString("541402a9-901e-3278-b091-53ccf0f1e418"),
        UUID.fromString("2ddc3f9b-334f-35d3-bfd9-0eb1f19f3472"),
        UUID.fromString("03005637-8a27-3e65-93d7-a87fea08fb3a"),
        UUID.fromString("b3847688-03c6-3e85-914c-40d8eba50678"),
        UUID.fromString("dcf69b0d-95b3-3cc7-8735-56518d54ad8d"),
        UUID.fromString("364cf14d-10c8-35ad-bd75-32bb3c358ec2"),
        UUID.fromString("ab45c741-300b-39db-8999-37f9ffdd7588"),
        UUID.fromString("25cab7e7-5425-329e-b80c-aec47dd61e21"),
        UUID.fromString("30ff1d02-31a2-3ee4-ad0b-e8f3051eb585"),
        UUID.fromString("60e3097c-ac59-3233-b089-3b4c502cee62"),
        UUID.fromString("580157d0-608d-31a6-ae90-a60da8432ae4"));

    List<UUID> pdar3XFilterChannelIds = Arrays.asList(
        UUID.fromString("52913a8d-79ac-3d6b-bc20-f1735edfa259"),
        UUID.fromString("52d95347-da97-32f5-a1e9-25fee9c3daf9"),
        UUID.fromString("2e90e845-2edc-3a3f-99fb-e27b71f79516"),
        UUID.fromString("98d3229a-7a15-339d-9311-fb7bf33896cb"),
        UUID.fromString("0e1c57ee-b625-3730-9a9a-c8bb237ade79"),
        UUID.fromString("9b250907-089d-3488-a763-c53c6958683d"));

    List<UUID> txarBeamChannelIds = Arrays.asList(
        UUID.fromString("4872e829-7a5a-310a-8c59-91ecb3a04376"),
        UUID.fromString("c50b5528-1a72-31ab-b2ff-205ac9f4c604"),
        UUID.fromString("e169f21c-22aa-3c3d-b3e0-83b8053d0907"),
        UUID.fromString("b16f200a-b7f4-3f89-8ce5-d01dfb28a486"),
        UUID.fromString("3b245930-cbcc-37c0-a60a-51f2dd03156a"),
        UUID.fromString("722b4e6b-ed24-33dc-b899-65ffdca2372d"),
        UUID.fromString("bf57c8b1-2632-362f-91a1-502abfeaea13"),
        UUID.fromString("c7fa2c87-daea-3a46-a342-50a5c5a4d8a0"),
        UUID.fromString("30b6fc9b-eb1d-3ec8-aa8f-4feb2e49f2cc"),
        UUID.fromString("d73b3cea-2615-34fb-a4ae-6d83fe8bfc08"),
        UUID.fromString("b91ade90-54d0-31f8-995a-43262f16f2c5"));

    List<UUID> txar3XFilterChannelIds = Arrays.asList(
        UUID.fromString("7a026c80-974c-3c3b-94f5-e57875ea40a2"),
        UUID.fromString("84b7f720-3ee0-3845-a72d-a0e0e472b174"),
        UUID.fromString("31a432a1-42fc-3200-b186-caaf61d67ebc"));

    List<UUID> ulnFilterIds = Arrays.asList(
        UUID.fromString("7f468184-e8c1-327d-b1df-cda9a3c2fa83"),
        UUID.fromString("df1acfc2-4cbf-3be3-aeb9-5af1717e621f"),
        UUID.fromString("e8d368f9-db8a-382f-a01f-75fd79ca6157"),
        UUID.fromString("3e41927a-f3d8-32cd-85e4-558c8ef728c6"),
        UUID.fromString("59d8eb55-959b-3d9b-aa5f-8cdcd7735240"),
        UUID.fromString("f353aa0e-fa78-368a-94d9-7d85fb86e0b0"));

    Stream<Entry<UUID, UUID>> pdarStream = Stream
        .concat(pdarBeamChannelIds.stream(), pdar3XFilterChannelIds.stream())
        .map(id -> Map.entry(id, stationIdMap.get("PDAR")));

    Stream<Entry<UUID, UUID>> txarStream = Stream
        .concat(txarBeamChannelIds.stream(), txar3XFilterChannelIds.stream())
        .map(id -> Map.entry(id, stationIdMap.get("TXAR")));

    Stream<Entry<UUID, UUID>> ulnStream = ulnFilterIds.stream()
        .map(id -> Map.entry(id, stationIdMap.get("ULN")));

    channelIdToStationIds = Stream.concat(pdarStream, Stream.concat(txarStream, ulnStream))
        .collect(toMap(Entry::getKey, Entry::getValue));
  }

  private SignalDetectorControl(
      SignalDetectorConfiguration configuration,
      PluginRegistry<SignalDetectorPlugin> signalDetectorPluginRegistry,
      PluginRegistry<OnsetTimeUncertaintyPlugin> onsetTimeUncertaintyPluginRegistry,
      PluginRegistry<OnsetTimeRefinementPlugin> onsetTimeRefinementPluginRegistry,
      CoiRepository coiRepository) {
    this.configuration = configuration;
    this.signalDetectorPluginRegistry = signalDetectorPluginRegistry;
    this.onsetTimeUncertaintyPluginRegistry = onsetTimeUncertaintyPluginRegistry;
    this.onsetTimeRefinementPluginRegistry = onsetTimeRefinementPluginRegistry;
    this.coiRepository = coiRepository;
    this.initialized = false;
  }

  /**
   * Initialization method used to set defaultParametersFactory for control class and its bound
   * signalDetectorPlugins.
   */
  public void initialize() {
    //TODO: Initialization steps

    initialized = true;
  }

  /**
   * Factory method for creating a SignalDetectorControl
   *
   * @param signalDetectorPluginRegistry plugin signalDetectorPluginRegistry, not null
   * @param onsetTimeUncertaintyPluginRegistry registry for onsetTimeUncertaintyPlugins, not null
   * @param onsetTimeRefinementPluginRegistry registry for onsetTimeRefinementPlugins, not null
   * @param coiRepository osd COI repository access object, not null
   * @return a new SignalDetectorControl object
   */
  public static SignalDetectorControl create(
      SignalDetectorConfiguration pluginConfiguration,
      PluginRegistry<SignalDetectorPlugin> signalDetectorPluginRegistry,
      PluginRegistry<OnsetTimeUncertaintyPlugin> onsetTimeUncertaintyPluginRegistry,
      PluginRegistry<OnsetTimeRefinementPlugin> onsetTimeRefinementPluginRegistry,
      CoiRepository coiRepository) {

    Objects.requireNonNull(pluginConfiguration,
        "Error creating SignalDetectorControl: configuration cannot be null");
    Objects.requireNonNull(signalDetectorPluginRegistry,
        "Error creating SignalDetectorControl: signalDetectorPluginRegistry cannot be null");
    Objects.requireNonNull(onsetTimeUncertaintyPluginRegistry,
        "Error creating SignalDetectorControl: onsetTimeUncertaintyPluginRegistry cannot be null");
    Objects.requireNonNull(onsetTimeRefinementPluginRegistry,
        "Error creating SignalDetectorControl: onsetTimeRefinementPluginRegistry cannot be null");
    Objects.requireNonNull(coiRepository,
        "Error creating SignalDetectorControl: osdGatewayAccessLibrary cannot be null");

    return new SignalDetectorControl(pluginConfiguration,
        signalDetectorPluginRegistry,
        onsetTimeUncertaintyPluginRegistry,
        onsetTimeRefinementPluginRegistry,
        coiRepository);
  }

  /**
   * Execute signal detection using the provided {@link ChannelSegmentDescriptor}
   *
   * @param descriptor object describing the filter processing request, not null
   * @return list of {@link UUID} to the most recently generated {@link SignalDetectionHypothesis}
   * for each generated {@link SignalDetection}, not null
   */
  public Collection<SignalDetectionHypothesisDescriptor> execute(
      ChannelSegmentDescriptor descriptor) {
    if (!initialized) {
      throw new IllegalStateException("SignalDetectorControl must be initialized before execution");
    }

    Objects.requireNonNull(descriptor, "SignalDetectorControl cannot execute a null ClaimCheck");

    //TODO: What to do if there's no channel segment? Also, getChannelSegments expects collection
    // of channelIds but we're passing a stationId
    List<ChannelSegment<Waveform>> channelSegments = coiRepository
        .getChannelSegments(List.of(descriptor.getChannelId()), descriptor.getStartTime(),
            descriptor.getEndTime());

    Preconditions.checkState(!channelSegments.isEmpty(),
        "Cannot execute signal detection: Insufficient Data");

    logger.info("SignalDetectorControl ClaimCheck execution processing {} ChannelSegments",
        channelSegments.size());

    configuration.getOnsetTimeRefinementParameters();

    return channelSegments.stream()
        .map(cs -> {
          UUID stationId = channelIdToStationIds.get(cs.getChannelId());
          Objects.requireNonNull(stationId,
              "No mapping to station ID exists for input channel ID: " + cs.getChannelId());
          return execute(stationId, cs);
        })
        .flatMap(Collection::stream).map(sd -> SignalDetectionHypothesisDescriptor.from(
            sd.getSignalDetectionHypotheses()
                //We know that the most recent SDH is appended to the end of the list
                //This is either the initial SDH if refinement was not ran, or the refined SDH if refinement was ran
                .get(sd.getSignalDetectionHypotheses().size() - 1),
            sd.getStationId()
        ))
        .collect(toList());
  }

  /**
   * Execute signal detection using the provided {@link ExecuteStreamingCommand}
   *
   * @param command object describing the qc processing request, not null
   * @return list of generated {@link SignalDetection}, not null
   */
  public Collection<SignalDetection> execute(ExecuteStreamingCommand command) {
    if (!initialized) {
      throw new IllegalStateException("SignalDetectorControl must be initialized before execution");
    }

    Objects.requireNonNull(command, "SignalDetectorControl cannot execute a null StreamingCommand");

    //TODO: We are creating SignalDetection objects by passing in Channel ID instead of a
    // stationId
    return execute(command.getChannelSegment().getChannelId(),
        command.getChannelSegment());
  }

  /**
   * Creates a list of signal detections given a channel segment, time range, processing context and
   * processing channel id. Signal detections are created by calling signalDetectorPlugins
   * configured for use with the processing channel.
   *
   * @param channelSegment channel segment to process
   * @return list of SignalDetections
   */
  private Collection<SignalDetection> execute(UUID stationId,
      ChannelSegment<Waveform> channelSegment) {

    logger.info("Performing signal detection on ChannelSegment");

    List<SignalDetectionParameters> detectionParametersList = configuration
        .getSignalDetectionParameters(stationId);

    OnsetTimeUncertaintyParameters uncertaintyParameters = configuration
        .getOnsetTimeUncertaintyParameters();

    OnsetTimeRefinementParameters refinementParameters = configuration
        .getOnsetTimeRefinementParameters();

    List<SignalDetectionHypothesis> hypothesesList = generateHypotheses(channelSegment,
        detectionParametersList, uncertaintyParameters,
        refinementParameters);

    Map<UUID, List<SignalDetectionHypothesis>> sdhMap = hypothesesList.stream()
        .collect(groupingBy(SignalDetectionHypothesis::getParentSignalDetectionId));

    //TODO: Replace the zeroed UUID with a proper CreationInfo ID when CreationInfo is finalized
    List<SignalDetection> signalDetections = sdhMap.entrySet().stream().map(entry -> SignalDetection
        .from(entry.getKey(), "Organization", stationId, entry.getValue(), new UUID(0, 0)))
        .collect(toList());

    if (!signalDetections.isEmpty()) {
      coiRepository.storeSignalDetections(signalDetections);
      coiRepository.storeChannelSegments(List.of(channelSegment));
    }

    return signalDetections;
  }

  /**
   * Generates initial hypotheses using the provided signal detector plugin, indexed by the ID that
   * will be used to construct a SignalDetection in the future
   *
   * @param channelSegment Channel Segment to detect the signals on
   * @return A list of the generated hypotheses for each detection
   */
  private List<SignalDetectionHypothesis> generateHypotheses(
      ChannelSegment<Waveform> channelSegment,
      List<SignalDetectionParameters> detectionParametersList,
      OnsetTimeUncertaintyParameters uncertaintyParameters,
      OnsetTimeRefinementParameters refinementParameters) {
    List<SignalDetectionHypothesis> hypotheses = new ArrayList<>();

    Map<SignalDetectionParameters, SignalDetectorPlugin> detectorPlugins = detectionParametersList
        .stream().collect(toMap(Function.identity(),
            parameters -> getPlugin(parameters.getPluginName(), SignalDetectorPlugin.class,
                signalDetectorPluginRegistry)));

    OnsetTimeUncertaintyPlugin uncertaintyPlugin = getPlugin(uncertaintyParameters.getPluginName(),
        OnsetTimeUncertaintyPlugin.class,
        onsetTimeUncertaintyPluginRegistry);

    OnsetTimeRefinementPlugin refinementPlugin = getPlugin(refinementParameters.getPluginName(),
        OnsetTimeRefinementPlugin.class,
        onsetTimeRefinementPluginRegistry);

    logger.info("SignalDetectionControl invoking {} plugins for Channel {}",
        detectionParametersList.stream().map(SignalDetectionParameters::getPluginName)
            .collect(toList()), channelSegment.getChannelId());

    Collection<Instant> detectionTimes = detectorPlugins.entrySet()
        .stream()
        .map(entry -> entry.getValue()
            .detectSignals(channelSegment, entry.getKey().getPluginParameters()))
        .flatMap(Collection::stream).collect(toList());

    if (detectionTimes.isEmpty()) {
      return Collections.emptyList();
    } else {
      for (Instant detectionTime : detectionTimes) {
        Duration uncertainty = executeUncertaintyPlugin(channelSegment, uncertaintyPlugin,
            detectionTime, uncertaintyParameters.getPluginParameters());
        final FeatureMeasurement<InstantValue> arrivalTimeMeasurement = createArrivalMeasurement(
            detectionTime, uncertainty, channelSegment.getId());
        final FeatureMeasurement<PhaseTypeMeasurementValue> phaseMeasurement =
            createPhaseMeasurement(
                channelSegment.getId());
        UUID parentSdId = UUID.randomUUID();

        //TODO: Replace the zeroed UUID with a proper CreationInfo ID when CreationInfo is finalized
        SignalDetectionHypothesis initialHypothesis = SignalDetectionHypothesis.create(
            parentSdId, arrivalTimeMeasurement, phaseMeasurement,
            new UUID(0, 0));

        hypotheses.add(initialHypothesis);

        refineHypothesis(initialHypothesis, channelSegment, refinementPlugin,
            refinementParameters.getPluginParameters(), uncertaintyPlugin,
            uncertaintyParameters.getPluginParameters())
            .ifPresent(hypothesis -> hypotheses.add(hypothesis));
      }
    }
    return hypotheses;
  }

  /**
   * Refines the arrival time of the provided hypothesis using the provided refinement plugin
   *
   * @param hypothesis Initial hypotheses to conduct arrival time refinement on
   * @param channelSegment Channel Segment whose waveform data is used to refine the arrival time
   * @param refinementPlugin Plugin to refine the arrival time
   * @param onsetTimeUncertaintyPlugin Plugin to derive the uncertainty of the refined arrival time
   * @return A {@link SignalDetectionHypothesis} with a refined arrival time measurement and new ID
   */
  private Optional<SignalDetectionHypothesis> refineHypothesis(
      SignalDetectionHypothesis hypothesis,
      ChannelSegment<Waveform> channelSegment,
      OnsetTimeRefinementPlugin refinementPlugin,
      Map<String, Object> refinementParamFieldMap,
      OnsetTimeUncertaintyPlugin onsetTimeUncertaintyPlugin,
      Map<String, Object> uncertaintyParamFieldMap) {

    Optional<FeatureMeasurement<InstantValue>> arrivalTimeFeatureMeasurement = hypothesis
        .getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME);

    if (!arrivalTimeFeatureMeasurement.isPresent()) {
      throw new IllegalArgumentException(
          "Signal Detection Hypothesis must have at least one arrival time and one phase feature measurement");
    }

    Instant arrivalTime = arrivalTimeFeatureMeasurement.get().getMeasurementValue().getValue();
    Instant refinedArrivalTime = executeRefinementPlugin(channelSegment, refinementPlugin,
        arrivalTime, refinementParamFieldMap);

    if (arrivalTime.equals(refinedArrivalTime)) {
      logger.info("Refined time did not change - no new hypothesis will be created");
      return Optional.empty();
    }

    Duration refinedTimeUncertainty = executeUncertaintyPlugin(channelSegment,
        onsetTimeUncertaintyPlugin,
        refinedArrivalTime, uncertaintyParamFieldMap);

    return Optional.of(hypothesis
        .withoutMeasurements(List.of(FeatureMeasurementTypes.ARRIVAL_TIME))
        .generateId()
        .addMeasurement(
            createArrivalMeasurement(refinedArrivalTime, refinedTimeUncertainty,
                channelSegment.getId()))
        .build());
  }

  private static Duration executeUncertaintyPlugin(ChannelSegment<Waveform> channelSegment,
      OnsetTimeUncertaintyPlugin plugin, Instant onsetTime,
      Map<String, Object> parameterFieldMap) {

    logger.info("SignalDetectorControl invoking plugin {} {} for Channel {}", plugin.getName(),
        plugin.getVersion(), channelSegment.getChannelId());

    Waveform targetWaveform = null;
    for (int i = 0; i < channelSegment.getTimeseries().size() && targetWaveform == null; i++) {
      Waveform waveform = channelSegment.getTimeseries().get(i);
      if (waveform.getStartTime().isBefore(onsetTime) && waveform.getEndTime().isAfter(onsetTime)) {
        targetWaveform = waveform;
      }
    }

    if (targetWaveform == null) {
      throw new IllegalStateException("No waveform containing onset time in channel");
    }

    return plugin.calculateOnsetTimeUncertainty(targetWaveform, onsetTime, parameterFieldMap);
  }

  private static Instant executeRefinementPlugin(ChannelSegment<Waveform> channelSegment,
      OnsetTimeRefinementPlugin plugin, Instant currentOnsetTime,
      Map<String, Object> parameterFieldMap) {

    logger.info("SignalDetectorControl invoking plugin {} {} for Channel {}", plugin.getName(),
        plugin.getVersion(), channelSegment.getChannelId());

    Waveform targetWaveform = null;
    for (int i = 0; i < channelSegment.getTimeseries().size() && targetWaveform == null; i++) {
      Waveform waveform = channelSegment.getTimeseries().get(i);
      if (waveform.getStartTime().isBefore(currentOnsetTime) && waveform.getEndTime()
          .isAfter(currentOnsetTime)) {
        targetWaveform = waveform;
      }
    }

    if (targetWaveform == null) {
      throw new IllegalStateException("No waveform containing onset time in channel");
    }

    return plugin.refineOnsetTime(targetWaveform, currentOnsetTime, parameterFieldMap);
  }

  /**
   * Returns a list of detector signalDetectorPlugins used for a channel.
   *
   * @param pluginClass The type of {@link Plugin} to retrieve.
   * @param pluginRegistry The {@link PluginRegistry} from which the plugins will be looked up.
   * @return The {@link Plugin} that was retrieved
   */
  private <T extends Plugin> T getPlugin(String pluginName,
      Class<T> pluginClass,
      PluginRegistry<T> pluginRegistry) {

    Optional<T> potentialPlugin = pluginRegistry
        .lookup(RegistrationInfo.create(pluginName, 1, 0, 0));

    if (!potentialPlugin.isPresent()) {
      throw new IllegalStateException(
          "Cannot execute Signal Detection. Missing " + pluginClass.getSimpleName() +
              " found for: " + pluginName);
    }

    return potentialPlugin.get();
  }

  /**
   * Convenience method for creating {@link CreationInformation} from a {@link SignalDetectorPlugin}
   * and a {@link ProcessingContext}. If the processing context is an empty Optional then the
   * analyst action and processing step reference are both set to empty optionals.
   *
   * @param plugin Plugin used to for signal detection.
   * @param processingContext Context in which we are processing the data.
   * @return CreationInformation representing how an object was created.
   */
  private static CreationInformation createCreationInformation(SignalDetectorPlugin plugin,
      ProcessingContext processingContext) {
    return CreationInformation.create(processingContext.getAnalystActionReference(),
        processingContext.getProcessingStepReference(),
        new SoftwareComponentInfo(plugin.getName(), plugin.getVersion().toString()));
  }

  private static FeatureMeasurement<InstantValue> createArrivalMeasurement(
      Instant arrivalTime, Duration uncertainty, UUID channelSegmentId) {

    final InstantValue arrivalMeasurement = InstantValue.from(
        arrivalTime, uncertainty);
    return FeatureMeasurement.create(channelSegmentId, FeatureMeasurementTypes.ARRIVAL_TIME,
        arrivalMeasurement);
  }

  private static FeatureMeasurement<PhaseTypeMeasurementValue> createPhaseMeasurement(
      UUID channelSegmentId) {

    final PhaseTypeMeasurementValue phaseMeasurement = PhaseTypeMeasurementValue.from(
        PhaseType.UNKNOWN, 1.0);
    return FeatureMeasurement.create(channelSegmentId, FeatureMeasurementTypes.PHASE,
        phaseMeasurement);
  }
}
