package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.ChannelDao;
import java.util.Objects;

/**
 * Utility class for converting {@link Channel} to {@link ChannelDao}.
 */
public class ChannelDaoConverter {

  /**
   * Obtains a {@link ChannelDao} containing the same information as a {@link Channel}
   *
   * @param channel Channel to convert to a ChannelDao, not null
   * @return ChannelDao, not null
   * @throws NullPointerException if channel is null
   */
  public static ChannelDao toDao(Channel channel) {
    Objects.requireNonNull(channel,
        "Cannot convert a null Channel to a ChannelDao");

    ChannelDao channelDao = new ChannelDao();
    channelDao.setId(channel.getId());
    channelDao.setChannelType(channel.getChannelType());
    channelDao.setDataType(channel.getDataType());
    channelDao.setDepth(channel.getDepth());
    channelDao.setElevation(channel.getElevation());
    channelDao.setHorizontalAngle(channel.getHorizontalAngle());
    channelDao.setLatitude(channel.getLatitude());
    channelDao.setLongitude(channel.getLongitude());
    channelDao.setName(channel.getName());
    channelDao.setSampleRate(channel.getSampleRate());
    channelDao.setVerticalAngle(channel.getVerticalAngle());

    return channelDao;
  }

  /**
   * Obtains a {@link Channel} containing the same information as a {@link ChannelDao}
   *
   * @param channelDao ChannelDao to convert to a Channel, not null
   * @return Channel, not null
   * @throws NullPointerException if channelDao is null
   */
  public static Channel fromDao(
      ChannelDao channelDao) {
    Objects.requireNonNull(channelDao,
        "Cannot convert a null ChannelDao to a Channel");

    return Channel.from(
        channelDao.getId(),
        channelDao.getName(),
        channelDao.getChannelType(),
        channelDao.getDataType(),
        channelDao.getLatitude(),
        channelDao.getLongitude(),
        channelDao.getElevation(),
        channelDao.getDepth(),
        channelDao.getVerticalAngle(),
        channelDao.getHorizontalAngle(),
        channelDao.getSampleRate());
  }
}
