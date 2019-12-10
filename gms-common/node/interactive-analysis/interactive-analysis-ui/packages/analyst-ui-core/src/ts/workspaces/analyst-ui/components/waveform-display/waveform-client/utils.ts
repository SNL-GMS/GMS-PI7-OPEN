import { ChannelSegmentTypes, WaveformTypes } from '~graphql/';

/**
 * Creates a zeroe'd out array of waveform data with given time interva
 * 
 * @param startTimeSecs start of the array
 * @param endTimeSecs end of the array
 * 
 * @returns float32 array with values of all zero
 */
export function createZeroArray(startTimeSecs: number, endTimeSecs: number): number[] {
    return Array.from(
        new Float32Array(
          WaveformTypes.DEFAULT_SAMPLE_RATE * (endTimeSecs - startTimeSecs) + 1
        ).fill(0)
    );
}
/**
 * Creates a channel segment object with all zero values for waveform data
 * 
 * @param cId channelId to create mock segment for
 * @param fId filterId to create mock segment for
 * @param startTimeSecs start time for mock segment time interval
 * @param endTimeSecs end time for mock segment time interval
 * @param data data to be used as wf data
 */
export const createZeroDataChannelSegment = (
    cId: string,
    fId: string,
    startTimeSecs: number,
    endTimeSecs: number,
    data: number[]
  ): ChannelSegmentTypes.ChannelSegment<WaveformTypes.Waveform> => {
    const ts: WaveformTypes.Waveform = {
      startTime: startTimeSecs,
      sampleRate: WaveformTypes.DEFAULT_SAMPLE_RATE,
      sampleCount: 1,
      values: data
    };
    return ({
      id: `${cId}${fId}`,
      type: ChannelSegmentTypes.ChannelSegmentType.RAW,
      channelId: cId,
      startTime: startTimeSecs,
      endTime: endTimeSecs,
      timeseriesType: ChannelSegmentTypes.TimeSeriesType.WAVEFORM,
      timeseries: [ts]
  });
};

/**
 * Creates a channel segment object with all zero values for waveform data
 * 
 * @param cId channelId to create mock segment for
 * @param fId filterId to create mock segment for
 * @param startTimeSecs start time for mock segment time interval
 * @param endTimeSecs end time for mock segment time interval
 * @param data data to be used as wf data
 */
export const createZeroDataFilteredChannelSegment = (
    cId: string,
    fId: string,
    startTimeSecs: number,
    endTimeSecs: number,
    data: number[]
  ): WaveformTypes.FilteredChannelSegment => {
    const ts: WaveformTypes.Waveform = {
      startTime: startTimeSecs,
      sampleRate: WaveformTypes.DEFAULT_SAMPLE_RATE,
      sampleCount: 1,
      values: data
    };
    return ({
      id: `${cId}${fId}`,
      type: ChannelSegmentTypes.ChannelSegmentType.FILTER,
      wfFilterId: fId,
      channelId: cId,
      sourceChannelId: cId,
      startTime: startTimeSecs,
      endTime: endTimeSecs,
      timeseriesType: ChannelSegmentTypes.TimeSeriesType.WAVEFORM,
      timeseries: [ts]
    });
  };
