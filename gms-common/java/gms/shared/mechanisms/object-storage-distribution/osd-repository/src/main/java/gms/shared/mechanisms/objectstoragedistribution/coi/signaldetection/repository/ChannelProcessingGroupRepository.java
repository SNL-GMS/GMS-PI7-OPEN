package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A repository interface for storing and retrieving FK Spectrum data.
 */
public interface ChannelProcessingGroupRepository {

  /**
   * Create a new Channel Processing Group.
   *
   * @param channelProcessingGroup channel processing group
   */
  void createChannelProcessingGroup(ChannelProcessingGroup channelProcessingGroup)
      throws Exception;

  /**
   * Checks whether a Channel Processing Group with the given UUID already exists.
   *
   * @param channelProcessingGroupId unique id of the channel processing group
   * @return True if exists, false otherwise
   */
  boolean channelProcessingGroupExists(UUID channelProcessingGroupId) throws Exception;

  /**
   * Retrieves a Channel Processing Group by ID, if one exists.
   *
   * @param channelProcessingGroupId unique Channel Segment ID
   * @return FkSpectraCreationInformation object; may be empty
   */
  Optional<ChannelProcessingGroup> retrieve(UUID channelProcessingGroupId) throws Exception;

  /**
   * Retrieves all Channel Processing Groups.
   *
   * @return list of Channel Processing Groups; may be empty
   */
  List<ChannelProcessingGroup> retrieveAll() throws Exception;
}
