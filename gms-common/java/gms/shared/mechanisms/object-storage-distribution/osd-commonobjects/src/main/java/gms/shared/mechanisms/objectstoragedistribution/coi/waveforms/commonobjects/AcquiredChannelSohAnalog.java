package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import java.time.Instant;
import java.util.UUID;

/**
 * Define a class to represent an analog State-of-Health reading.
 */
public class AcquiredChannelSohAnalog extends AcquiredChannelSoh<Double> {

  /**
   * Creates an AcquiredChannelSohAnalog anew.
   *
   * @param channelId identifier referencing the ProcessingChannel this SOH is for.
   * @param type The state of health type that will be represented by all of the times and statuses
   * held by this class.
   * @param startTime the start time for the status
   * @param endTime the end time for the status
   * @param status the Status of the State-Of-Health (e.g. a boolean or a float or something)
   * @param creationInfo metadata about when this object was created and by what/whom.
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static AcquiredChannelSohAnalog create(UUID channelId,
      AcquiredChannelSohType type,
      Instant startTime, Instant endTime, double status, CreationInfo creationInfo) {

    return new AcquiredChannelSohAnalog(UUID.randomUUID(), channelId,
        type, startTime, endTime, status, creationInfo);
  }

  /**
   * Creates an AcquiredChannelSohAnalog from all params for it.
   *
   * @param channelId identifier referencing the ProcessingChannel this SOH is for.
   * @param type The state of health type that will be represented by all of the times and statuses
   * held by this class.
   * @param startTime the start time for the status
   * @param endTime the end time for the status
   * @param status the Status of the State-Of-Health (e.g. a boolean or a float or something)
   * @param creationInfo metadata about when this object was created and by what/whom.
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static AcquiredChannelSohAnalog from(UUID id, UUID channelId,
      AcquiredChannelSohType type,
      Instant startTime, Instant endTime, double status, CreationInfo creationInfo) {

    return new AcquiredChannelSohAnalog(id, channelId,
        type, startTime, endTime, status, creationInfo);
  }

  /**
   * @param id the identifier for this entity
   * @param channelId identifier referencing the ProcessingChannel this SOH is for.
   * @param type The state of health type that will be represented by all of the times and statuses
   * held by this class.
   * @param startTime the start time for the status
   * @param endTime the end time for the status
   * @param status the Status of the State-Of-Health (e.g. a boolean or a float or something)
   * @param creationInfo metadata about when this object was created and by what/whom.
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  private AcquiredChannelSohAnalog(UUID id, UUID channelId, AcquiredChannelSohType type,
      Instant startTime, Instant endTime, double status, CreationInfo creationInfo) {
    super(id, channelId, type, startTime, endTime, status, creationInfo);
  }
}
