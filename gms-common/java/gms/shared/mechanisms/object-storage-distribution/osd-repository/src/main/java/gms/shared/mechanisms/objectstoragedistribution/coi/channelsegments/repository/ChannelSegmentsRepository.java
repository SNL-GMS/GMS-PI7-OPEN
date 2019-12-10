package gms.shared.mechanisms.objectstoragedistribution.coi.channelsegments.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public interface ChannelSegmentsRepository {

  Collection<ChannelSegment<? extends Timeseries>> retrieveChannelSegmentsByIds(Collection<UUID> channelSegmentIds,
      Boolean withTimeseries) throws Exception;

  Collection<ChannelSegment<? extends Timeseries>> retrieveChannelSegmentsByChannelIds(Collection<UUID> channelIds,
      Instant startTime, Instant endTime) throws Exception;
}
