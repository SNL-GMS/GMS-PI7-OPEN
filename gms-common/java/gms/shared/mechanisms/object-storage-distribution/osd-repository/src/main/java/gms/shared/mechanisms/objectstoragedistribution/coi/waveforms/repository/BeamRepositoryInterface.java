package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamCreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A repository interface for storing and retrieving Beam waveform data.
 */
public interface BeamRepositoryInterface {

  /**
   * Stores newly generated Beam Channel Segment.
   *
   * @param newBeamChannelSegment newly generated Beam Channel Segment
   * @throws Exception operation could not be performed
   */
  void storeBeam(
      ChannelSegment<Waveform> newBeamChannelSegment,
      BeamCreationInfo beamCreationInfo) throws Exception;

  /**
   * Returns true if the Beam Creation Info record already exists.
   *
   * @param beamCreationInfo beam creation info
   * @return true if record exists, false otherwise
   */
  boolean beamCreationInfoRecordExists(BeamCreationInfo beamCreationInfo) throws Exception;

  /**
   * Retrieves the Beam Creation Info object with the given Channel Segment ID.
   *
   * @param channelSegmentId unique Channel Segment ID
   * @return BeamCreationInfo object; may be empty
   * @throws Exception operation could not be performed
   */
  Optional<BeamCreationInfo> retrieveCreationInfoByChannelSegmentId(
      UUID channelSegmentId) throws Exception;

  /**
   * Retrieves all Beam Creation Info objects for the given Processing Group.
   *
   * @param processingGroupId processing group ID
   * @return list of Beam Creation Info objects; may be empty
   * @throws Exception operation could not be performed
   */
  List<BeamCreationInfo> retrieveCreationInfoByProcessingGroupId(
      UUID processingGroupId) throws Exception;
}
