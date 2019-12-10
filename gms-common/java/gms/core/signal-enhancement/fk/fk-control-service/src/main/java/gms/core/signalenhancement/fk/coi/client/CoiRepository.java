package gms.core.signalenhancement.fk.coi.client;

import com.google.common.base.Preconditions;
import gms.core.signalenhancement.fk.control.configuration.FileBasedFkConfiguration;
import gms.core.signalenhancement.fk.control.configuration.FkConfiguration;
import gms.core.signalenhancement.fk.util.UrlUtility;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginConfiguration;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.RegistrationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.UpdateStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.FkSpectraRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * FkAnalysis Service's Access library for the OSD Gateway.
 *
 * This is a placeholder implementation used to show the initial pattern for control class
 * interfaces to the OSD.
 */
public class CoiRepository {

  private static final Logger logger = LoggerFactory.getLogger(CoiRepository.class);

  private final SignalDetectionRepository signalDetectionRepository;
  private final WaveformRepository waveformRepository;
  private final FkSpectraRepository fkSpectraRepository;

  private CoiRepository(SignalDetectionRepository signalDetectionRepository,
      WaveformRepository waveformRepository,
      FkSpectraRepository fkSpectraRepository) {
    this.waveformRepository = waveformRepository;
    this.fkSpectraRepository = fkSpectraRepository;
    this.signalDetectionRepository = signalDetectionRepository;
  }

  /**
   * Obtains a new {@link CoiRepository} that uses the provided {@link WaveformRepository}
   *
   * @param waveformRepository a WaveformRepository, not null
   * @return an {@link CoiRepository}, not null
   */
  public static CoiRepository from(SignalDetectionRepository signalDetectionRepository,
      WaveformRepository waveformRepository,
      FkSpectraRepository fkSpectraRepository) {
    Preconditions.checkNotNull(signalDetectionRepository);
    Preconditions.checkNotNull(waveformRepository);
    Preconditions.checkNotNull(fkSpectraRepository);

    return new CoiRepository(signalDetectionRepository, waveformRepository, fkSpectraRepository);
  }

  /**
   * @return {@link FkConfiguration}, not null
   */
  public FileBasedFkConfiguration getConfiguration() {
    URL definitionUrl = UrlUtility.getUrlToResourceFile(
        "gms/core/signalenhancement/fk/service/definition.json");
    URL channelIdsUrl = UrlUtility.getUrlToResourceFile(
        "gms/core/signalenhancement/fk/service/channelIds.json");
    URL relativePositionUrl = UrlUtility.getUrlToResourceFile(
        "gms/core/signalenhancement/fk/service/relativePositions.json");
    URL outputIdsUrl = UrlUtility.getUrlToResourceFile(
        "gms/core/signalenhancement/fk/service/outputIds.json");

    return FileBasedFkConfiguration
        .from(definitionUrl, channelIdsUrl, relativePositionUrl, outputIdsUrl);
  }


  /**
   * Obtains the {@link PluginConfiguration} for the plugin with the provided {@link
   * RegistrationInfo}
   *
   * @param registrationInfo {@link RegistrationInfo}, not null
   * @return {@link PluginConfiguration}, not null
   * @throws NullPointerException if registrationInfo is null
   */
  public Map<String, Object> getParameterFieldMap(RegistrationInfo registrationInfo) {
    Preconditions.checkNotNull(registrationInfo);
    return Collections.emptyMap();
  }

  /**
   * Retrieves waveform channel segments by channel id and time range.
   *
   * @param channelIds Collection of channel ids to retrieve waveforms for
   * @param startTime Start of the time range to retrieve waveforms
   * @param endTime End of the time range to retrieve waveforms
   * @return Collection of waveform channel segments.
   * @throws IllegalStateException if there is an error retrieving waveforms from the repository
   */
  public Collection<ChannelSegment<Waveform>> findWaveformsByChannelsAndTime(
      Collection<UUID> channelIds, Instant startTime, Instant endTime) {
    Preconditions.checkNotNull(channelIds);
    Preconditions.checkArgument(!channelIds.isEmpty(), "Channel Ids cannot be empty");
    Preconditions.checkNotNull(startTime);
    Preconditions.checkNotNull(endTime);
    Preconditions.checkArgument(startTime.isBefore(endTime), "Start Time must be before End Time");

    try {
      return waveformRepository.retrieveChannelSegments(channelIds, startTime, endTime, true)
          .values();
    } catch (Exception e) {
      logger.error("Error retrieving channel segments", e);
      throw new IllegalStateException(e);
    }
  }

  public Collection<SignalDetectionHypothesis> findSignalDetectionHypothesesByIds(Collection<UUID>
      hypothesisIds) {
    Preconditions.checkNotNull(hypothesisIds);
    Preconditions.checkArgument(!hypothesisIds.isEmpty(),
        "Cannot retrieve signal detection hypothesis: empty ids collection");

    return signalDetectionRepository.findSignalDetectionHypothesesByIds(hypothesisIds);
  }

  /**
   * FkAnalysis Service OSD Gateway access library operation to storeFkSpectras results of an Fk
   * Spectrum. This invokes an operation in the OSD Gateway service over HTTP.
   *
   * @param channelSegments collection of channel segments, not null
   * @param storageVisibility has the {@link StorageVisibility} and associated context for this
   * storeFkSpectras, not null
   */
  public void storeFkSpectras(
      Collection<ChannelSegment<FkSpectra>> channelSegments,
      StorageVisibility storageVisibility) {
    Preconditions.checkNotNull(channelSegments);
    Preconditions
        .checkArgument(!channelSegments.isEmpty(), "Cannot store empty channel segments");
    Preconditions.checkNotNull(storageVisibility);

    channelSegments.forEach(this::storeFkSpectra);
  }

  private void storeFkSpectra(ChannelSegment<FkSpectra> cs) {
    try {
      fkSpectraRepository.storeFkSpectra(cs);
      logger.info(
          "Stored FkSpectra (id:{}, channel:{}, start:{}, end:{})",
          cs.getId(), cs.getChannelId(), cs.getStartTime(), cs.getEndTime());
    } catch (Exception e) {
      logger.info("Error storing FkSpectra", e);
      throw new IllegalStateException(e);
    }
  }

  public Map<SignalDetectionHypothesisDescriptor, UpdateStatus> storeSignalDetectionHypotheses(
      Collection<SignalDetectionHypothesisDescriptor> descriptors) {
    Preconditions.checkNotNull(descriptors);

    return descriptors.isEmpty() ? Map.of() : signalDetectionRepository.store(descriptors);
  }

}
