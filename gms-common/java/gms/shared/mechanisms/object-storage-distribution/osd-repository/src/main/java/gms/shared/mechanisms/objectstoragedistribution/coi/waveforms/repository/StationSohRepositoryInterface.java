package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


/**
 * The interface for storing and retrieving COI objects.
 */
public interface StationSohRepositoryInterface {

  /**
   * Initialize the persistence databases and perform other setup.
   *
   * @return True if successful, otherwise false.
   */
  boolean init();

  /**
   * Close persistence databases and perform other shutdown tasks.
   *
   * @return True if successful, otherwise false.
   */
  boolean close();


  /**
   * Store a State from Health object containing an analog value.
   *
   * @param soh The SOH object.
   */
  void storeAnalogSoh(AcquiredChannelSohAnalog soh) throws Exception;

  /**
   * Store a State from Health object containing a boolean value.
   *
   * @param soh The SOH object.
   */
  void storeBooleanSoh(AcquiredChannelSohBoolean soh) throws Exception;

  /**
   * Store State from Health objects.
   *
   * @param sohs The SOH objects.
   */
  void storeSoh(Collection<AcquiredChannelSoh> sohs) throws Exception;

  /**
   * Get the SOH objects from the database that contain analog data.
   *
   * @return A Collection containing the SOH objects.
   */
  Collection<AcquiredChannelSohAnalog> retrieveAllAnalogSoh() throws Exception;

  /**
   * Get the SOH objects from the database that contain boolean data.
   *
   * @return A Collection containing the SOH objects..
   */
  Collection<AcquiredChannelSohBoolean> retrieveAllBooleanSoh() throws Exception;

  /**
   * Retrieve all {@link AcquiredChannelSohBoolean} objects from the provided processing channel
   * created within the provided time range. The time range is inclusive for both start and end
   * time.
   *
   * @param channelId Id for the processing channel the SOH was measured on.
   * @param startTime Inclusive start from time range for the query.
   * @param endTime Inclusive end from time range for the query.
   * @return All SOH boolean objects that meet the query criteria.
   */
  List<AcquiredChannelSohBoolean> retrieveBooleanSohByProcessingChannelAndTimeRange(
      UUID channelId, Instant startTime, Instant endTime) throws Exception;

  /**
   * Retrieve all {@link AcquiredChannelSohAnalog} objects from the provided processing channel
   * created within the provided time range. The time range is inclusive for both start and end
   * time.
   *
   * @param channelId Id for the processing channel the SOH was measured on.
   * @param startTime Inclusive start from time range for the query.
   * @param endTime Inclusive end from time range for the query.
   * @return All SOH analog objects that meet the query criteria.
   */
  List<AcquiredChannelSohAnalog> retrieveAnalogSohByProcessingChannelAndTimeRange(
      UUID channelId, Instant startTime, Instant endTime) throws Exception;

  /**
   * Retrieve the {@link AcquiredChannelSohBoolean} with the provided id.  Returns an empty
   * {@link Optional} if no AcquiredChannelSohBoolean has that id.
   *
   * @param acquiredChannelSohId id for the AcquiredChannelSohBoolean, not null
   * @return Optional AcquiredChannelSohBoolean object with the provided id, not null
   */
  Optional<AcquiredChannelSohBoolean> retrieveAcquiredChannelSohBooleanById(
      UUID acquiredChannelSohId) throws Exception;

  /**
   * Retrieve the {@link AcquiredChannelSohAnalog} with the provided id.  Returns an empty
   * {@link Optional} if no AcquiredChannelSohAnalog has that id.
   *
   * @param acquiredChannelSohId id for the AcquiredChannelSohAnalog, not null
   * @return Optional AcquiredChannelSohAnalog object with the provided id, not null
   */
  Optional<AcquiredChannelSohAnalog> retrieveAcquiredChannelSohAnalogById(
      UUID acquiredChannelSohId) throws Exception;
}
