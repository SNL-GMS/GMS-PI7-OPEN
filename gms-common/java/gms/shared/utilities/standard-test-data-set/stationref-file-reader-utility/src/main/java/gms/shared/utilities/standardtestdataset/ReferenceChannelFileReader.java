package gms.shared.utilities.standardtestdataset;

import com.google.common.collect.ListMultimap;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class ReferenceChannelFileReader {

  private final ListMultimap<String, ReferenceChannel> channelsByName;

  public ReferenceChannelFileReader(String channelsFilePath) throws Exception {
    this.channelsByName = StationReferenceFileReaderUtility.readBy(
        channelsFilePath, ReferenceChannel[].class, ReferenceChannel::getName);
  }

  public Optional<UUID> findChannelIdByNameAndTime(
      String siteName, String channelName, Instant time) {
    return StationReferenceFileReaderUtility.findByNameAndTime(
        this.channelsByName, siteName + "/" + channelName, time, ReferenceChannel::getActualTime)
        .map(ReferenceChannel::getVersionId);
  }

}
