package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.ChannelProcessingGroupDao;
import java.util.Objects;


/**
 * Utility class for converting {@link ChannelProcessingGroup} to {@link
 * ChannelProcessingGroupDao}.
 */
public class ChannelProcessingGroupDaoConverter {

  /**
   * Obtains a {@link ChannelProcessingGroupDao} containing the same information as a {@link
   * ChannelProcessingGroup}
   *
   * @param channelProcessingGroup ChannelProcessingGroup to convert to a ChannelProcessingGroupDao,
   * not null
   * @return ChannelProcessingGroupDao, not null
   * @throws NullPointerException if channelProcessingGroup is null
   */
  public static ChannelProcessingGroupDao toDao(
      ChannelProcessingGroup channelProcessingGroup) {
    Objects.requireNonNull(channelProcessingGroup,
        "Cannot convert a null ChannelProcessingGroup to a ChannelProcessingGroupDao");

    ChannelProcessingGroupDao dao = new ChannelProcessingGroupDao();
    dao.setId(channelProcessingGroup.getId());
    dao.setType(channelProcessingGroup.getType());
    dao.setActualChangeTime(channelProcessingGroup.getActualChangeTime());
    dao.setSystemChangeTime(channelProcessingGroup.getSystemChangeTime());
    dao.setChannelIds(channelProcessingGroup.getChannelIds());
    dao.setComment(channelProcessingGroup.getComment());
    dao.setStatus(channelProcessingGroup.getStatus());

    return dao;
  }

  /**
   * Obtains a {@link ChannelProcessingGroup} containing the same information as a {@link
   * ChannelProcessingGroupDao}
   *
   * @param channelProcessingGroupDao ChannelProcessingGroupDao to convert to a
   * ChannelProcessingGroup, not null
   * @return ChannelProcessingGroup, not null
   * @throws NullPointerException if channelProcessingGroupDao is null
   */
  public static ChannelProcessingGroup fromDao(
      ChannelProcessingGroupDao channelProcessingGroupDao) {
    Objects.requireNonNull(channelProcessingGroupDao,
        "Cannot convert a null ChannelProcessingGroupDao to a ChannelProcessingGroup");

    return ChannelProcessingGroup.from(
        channelProcessingGroupDao.getId(),
        channelProcessingGroupDao.getType(),
        channelProcessingGroupDao.getChannelIds(),
        channelProcessingGroupDao.getActualChangeTime(),
        channelProcessingGroupDao.getSystemChangeTime(),
        channelProcessingGroupDao.getStatus(),
        channelProcessingGroupDao.getComment());
  }
}
