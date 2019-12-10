package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A repository interface for storing and retrieving FK Spectrum data.
 */
public interface FkSpectraRepository {

  /**
   * Stores newly generated FK Spectra Channel Segment.
   *
   * @param newFkChannelSegment newly generated FK Segment
   * @throws Exception operation could not be performed
   */
  void storeFkSpectra(ChannelSegment<FkSpectra> newFkChannelSegment) throws Exception;

  /**
   * Returns true if the ChannelSegment already exists
   *
   * @return true if record exists, false otherwise
   */
  boolean fkChannelSegmentRecordExists(ChannelSegment<FkSpectra> fkChannelSegment)
      throws Exception;

  /**
   * Returns true if ALL channel segments passed in the fkChannelSegments list parameter exist in
   * the channel segment table
   */
  boolean fkChannelSegmentRecordsExist(List<ChannelSegment<FkSpectra>> fkChannelSegments)
      throws Exception;

  /**
   * Retrieves a single Fk Spectra Channel Segment, with the given Channel Segment ID.
   *
   * @param channelSegmentId channel segment ID
   * @return optional of FK Spectra Channel Segment; may be empty
   * @throws Exception operation could not be performed
   */
  Optional<ChannelSegment<FkSpectra>> retrieveFkChannelSegment(UUID channelSegmentId,
      boolean withFkSpectra)
      throws Exception;

  /**
   * Retrieves a list of Fk Spectra Channel Segments that fall within the given time range.
   *
   * @param channelId ID of the Channel
   * @param fkReferenceStartTime the start of the time range
   * @param fkReferenceEndTime the end of the time range
   * @return list of FK Spectra Channel Segments; may be empty
   * @throws Exception operation could not be performed
   */
  List<ChannelSegment<FkSpectra>> segmentsForProcessingChannel(
      UUID channelId, Instant fkReferenceStartTime, Instant fkReferenceEndTime)
      throws Exception;

  /**
   * Retrieves a list of Fk Spectra Channel Segments that fall within the given time range. Merges
   * where needed
   *
   * @param channelId ID of the Channel
   * @param fkReferenceStartTime the start of the time range
   * @param fkReferenceEndTime the end of the time range
   * @param includeSpectrum whether to include spectrum values
   * @return list of FK Spectra Channel Segments; may be empty
   * @throws Exception operation could not be performed
   */
  Optional<ChannelSegment<FkSpectra>> retrieveFkSpectraByTime(
      UUID channelId, Instant fkReferenceStartTime, Instant fkReferenceEndTime,
      boolean includeSpectrum)
      throws Exception;

  /**
   * Retrieves a {@link List} of {@link FkSpectra} objects associated with the provided {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects.TimeseriesDao}
   * {@link UUID}s.  Optionally includes the underlying {@link gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum} values.
   *
   * @param ids {@link gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects.TimeseriesDao}
   * {@link UUID}s for which to retrieve the associated {@link FkSpectra}s
   * @param withValues {@link Boolean} denoting whether or not to include the underlying {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum} values
   * @return {@link List} of {@link FkSpectra}. Optionally includes the underlying {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum} values.
   */

  List<FkSpectra> retrieveFkSpectrasByTimeseriesIds(Collection<UUID> ids, Boolean withValues)
      throws Exception;
}
