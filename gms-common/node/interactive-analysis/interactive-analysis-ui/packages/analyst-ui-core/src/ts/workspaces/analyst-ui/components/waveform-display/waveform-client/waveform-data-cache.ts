import { WaveformTypes } from '~graphql/';
import { ChannelSegment } from '~graphql/channel-segment/types';
import { FilteredChannelSegment, Waveform } from '~graphql/waveform/types';

/**
 * Provides an in-memory cache for waveform data used in rendering the waveform display
 */
export class WaveformDataCache {
  private readonly cache:
    Map<string, Map<string, [ChannelSegment<Waveform> | FilteredChannelSegment]>>
    = new Map<string, Map<string, [ChannelSegment<Waveform> | FilteredChannelSegment]>>();

  /**
   * Retrieve a list of all the channel IDs in the cache.
   * 
   * @returns a string array of channel ids
   */
  public readonly getWaveformChannelIds = (): string[] => [...this.cache.keys()];

  /**
   * Retrieve the cache entry associated with the provided channel ID/filter ID. 
   * This method returns undefined if no cache entry exists for the provided 
   * channel ID/filter ID.
   * 
   * @param channelId The channel ID associated with the cache entry to retrieve
   * @param filterId The filter ID associated with the cache entry to retrieve
   * 
   * @returns a list of either channel segments or filtered channel segments
   */
  public readonly getWaveformEntry =
    (channelId: string, filterId: string): [ChannelSegment<Waveform> | FilteredChannelSegment] =>
      this.cache.has(channelId) ?
        (this.cache.get(channelId)
          .has(filterId) ?
          this.cache.get(channelId)
            .get(filterId) : undefined)
        : undefined

  /**
   * Retrieves the cache entries associated with the provided channel ID. 
   * This method returns undefined if no cache entries exists for the provided 
   * channel ID.
   * 
   * @param channelId The channel ID associated with the cache entry to retrieve
   * 
   * @returns a map of channelIds to a list of channelsegments or filteredchannelsegments
   */
  public getWaveformEntriesForChannelId(channelId: string):
    Map<string, [ChannelSegment<Waveform> | FilteredChannelSegment]> {
    return this.cache.has(channelId) ? this.cache.get(channelId) : undefined;
  }

  /**
   * Clear a specific channel ID/filter ID from the cache.
   * If a filter ID is not specified, all entries for the channel ID are removed.
   * If a filter ID is specified, only the channel ID/filter ID entry is removed.
   * @param channelId The channel ID to clear the cache entry for
   * @param filterId (optional)The filter ID to clear the cache entry for
   */
  public readonly clearAllWaveformEntry = (channelId: string, filterId?: string) => {
    if (channelId) {
      if (this.cache.has(channelId)) {
        if (filterId) {
          this.cache.get(channelId)
            .delete(filterId);
        } else {
          this.cache.get(channelId)
            .clear();
          this.cache.delete(channelId);
        }
      }
    }
  }

  /**
   * Clears all entries from the cache.
   */
  public clearAllWaveformEntries = () => {
    this.cache.clear();
  }

  /**
   * Updates the cache from the provided list of ChannelSegments, adding
   * new cache entries for channel IDs not already in the cache, and
   * merging in timeseries data into existing cache entries where they exist.
   * If the overwrite parameter is set to true, this method will replace
   * existing cache entries associated with the channel IDs, rather than merging
   * in the new timeseries data.
   * 
   * @param channelSegments The list of ChannelSegments from which to update
   * the cache
   */
  public readonly updateFromChannelSegments = (
    channelSegments: ChannelSegment<Waveform>[],
  ) => {
    if (channelSegments) {
      channelSegments.forEach(channelSegment => {
        this.updateChannelSegment(
          channelSegment.channelId,
          WaveformTypes.UNFILTERED,
          channelSegment
        );
      });
    }
  }

  /**
   * Updates the cache from the provided list of ChannelSegments, adding
   * new cache entries for channel IDs not already in the cache, and
   * merging in timeseries data into existing cache entries where they exist.
   * If the overwrite parameter is set to true, this method will replace
   * existing cache entries associated with the channel IDs, rather than merging
   * in the new timeseries data.
   * 
   * @param filteredChannelSegments The list of ChannelSegments from which to update
   * the cache
   */
  public readonly updateFromFilteredChannelSegments = (
    filteredChannelSegments: FilteredChannelSegment[],
  ) => {
    if (filteredChannelSegments) {
      filteredChannelSegments.forEach(filteredChannelSegment => {
        this.updateChannelSegment(
          filteredChannelSegment.sourceChannelId,
          filteredChannelSegment.wfFilterId,
          filteredChannelSegment);
      });
    }
  }

  /**
   * Updates the cache from the provided channel segment, adding
   * new cache entries for channel ID/filter ID not already in the cache, and
   * merging in timeseries data into existing cache entries where they exist.
   * If the overwrite parameter is set to true, this method will replace
   * existing cache entries associated with the channel ID/filter ID, 
   * rather than merging in the new timeseries data.
   * 
   * @param channelId The channel ID used as the channel key for the entry in the cache
   * @param filterId The filter ID used as the filter key for the entry in the cache
   * 
   */
  public readonly updateChannelSegment = (
    channelId: string,
    filterId: string,
    channelSegment: ChannelSegment<Waveform> | FilteredChannelSegment) => {
    if (!channelId || !filterId) {
      return;
    }

    if (!this.cache.has(channelId)) {
      this.cache.set(channelId, new Map());
    }
    this.set(channelId, channelSegment, filterId);
  }

  /**
   * Insert the provided entry in the cache replacing the existing entry
   * if one exists. Returns undefined if the entry is not inserted; otherwise
   * returns the entry inserted.
   * 
   * @param channelId The channel ID used as the channel key for the entry in the cache
   * @param filterId The filter ID used as the filter key for the entry in the cache
   * @param value The entry to insert into the cache associated to the provided 
   * channel ID/filter ID
   * 
   * @returns either a channelsegment or a filteredchannelsegment
   */
  private readonly set = (
    channelId: string,
    value: (ChannelSegment<Waveform> | FilteredChannelSegment),
    filterId?: string
  ): ChannelSegment<Waveform> | FilteredChannelSegment => {
    if (channelId) {
      if (!this.cache.has(channelId)) {
        this.cache.set(channelId, new Map());
      }

      if (filterId) {
        const existingData = this.getWaveformEntry(channelId, filterId);
        if (existingData) {
          existingData.push(value);
        } else {
          this.cache.get(channelId)
            .set(filterId, [value]);
        }
        return value;
      }
    }
    return undefined;
  }
}
