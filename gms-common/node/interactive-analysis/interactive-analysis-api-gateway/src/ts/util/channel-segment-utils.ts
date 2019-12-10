import { isEmpty } from 'lodash';
import * as uuid4 from 'uuid/v4';
import {
    ChannelSegment,
    OSDChannelSegment,
    OSDTimeSeries,
    TimeSeries,
    isWaveformChannelSegment,
    isFkSpectraTimeSeries,
    isWaveformTimeSeries,
} from '../channel-segment/model';
import { Waveform } from '../waveform/model';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { toEpochSeconds, toOSDTime } from './time-utils';
import { CreatorType } from '../common/model';
import { fixNaNValuesDoubleArray, fixNanValues } from './common-utils';

/**
 * Converts a channel segment in OSD compatible format to api (graphql) format for the frontend. 
 * Ideally this method will go away if/when the COI data structures are finalized 
 * and we adjust our data structures to match them.
 * @param osdChannelSegment a channel segment in OSD compatible format
 * @param channel segment in api gateway format
 * @returns a time series channel segment
 */
export function convertChannelSegmentFromOSDToAPI(
    osdChannelSegment: OSDChannelSegment<OSDTimeSeries>): ChannelSegment<TimeSeries> {
    if (osdChannelSegment && !isEmpty(osdChannelSegment) &&
        osdChannelSegment.timeseries && !isEmpty(osdChannelSegment.timeseries)) {
        try {
            const apiTimeSeries: TimeSeries[] = osdChannelSegment.timeseries.map((timeseries: OSDTimeSeries) => {
                const ts: TimeSeries = {
                    ...timeseries,
                    startTime: toEpochSeconds(timeseries.startTime),
                };
                if (isFkSpectraTimeSeries(ts, osdChannelSegment.timeseriesType)) {
                    ts.reviewed = false;
                    ts.spectrums.forEach(spectrum => {
                        fixNaNValuesDoubleArray(spectrum.fstat);
                        fixNaNValuesDoubleArray(spectrum.power);
                    });
                } else if (isWaveformTimeSeries(ts, osdChannelSegment.timeseriesType)) {
                    fixNanValues(ts.values);
                }
                return ts;
            });
            return {
                id: osdChannelSegment.id,
                name: osdChannelSegment.name,
                channelId: osdChannelSegment.channelId,
                type: osdChannelSegment.type,
                startTime: toEpochSeconds(osdChannelSegment.startTime),
                endTime: toEpochSeconds(osdChannelSegment.endTime),
                timeseriesType: osdChannelSegment.timeseriesType,

                creationInfo: { // do not ...osdChannelSegment due to extra params we don't support
                    id: osdChannelSegment.creationInfo.id ? osdChannelSegment.creationInfo.id : uuid4().toString(),
                    creatorId: osdChannelSegment.creationInfo.creatorId ?
                        osdChannelSegment.creationInfo.id : uuid4().toString(),
                    creatorType: osdChannelSegment.creationInfo.creatorType ?
                        osdChannelSegment.creationInfo.creatorType : CreatorType.System,
                    creatorName: osdChannelSegment.creationInfo.creatorName,
                    creationTime: toEpochSeconds(osdChannelSegment.creationInfo.creationTime),
                },

                timeseries: apiTimeSeries,
            };
        } catch (error) {
            logger.error(`Invalid OSD Channel segment data; failed to convert: ${error}`);
            return undefined;
        }
    } else {
        return undefined;
    }
}

/**
 * Converts a channel segment in api (graphql) format to an OSD compatible channel segment format.
 * Ideally this method will go away if/when the COI data structures are finalized 
 * and we adjust our data structures to match them.
 * @param channelSegment a channel segment in api gateway format
 * @return a OSD compatible channel segment
 */
export function convertChannelSegmentFromAPIToOSD(
    channelSegment: ChannelSegment<TimeSeries>): OSDChannelSegment<OSDTimeSeries> {
    let osdTimeSeries: OSDTimeSeries[];
    if (isWaveformChannelSegment(channelSegment)) {
        osdTimeSeries = channelSegment.timeseries.map((waveform: Waveform) =>
            ({
                ...waveform,
                startTime: toOSDTime(waveform.startTime)
            }));
    } else {
        osdTimeSeries = channelSegment.timeseries.map(timeseries => ({
            ...timeseries,
            startTime: toOSDTime(timeseries.startTime)
        }));
    }
    const creationInfo = channelSegment.creationInfo;
    const osdChannelSegment = {
        ...channelSegment,
        startTime: toOSDTime(channelSegment.startTime),
        endTime: toOSDTime(channelSegment.endTime),
        creationInfo: {
            creatorName: creationInfo.creatorName,
            creationTime: toOSDTime(creationInfo.creationTime),
            softwareInfo: {
                name: 'Default Name',
                version: 'Default Version',
            }
        },
        timeseries: osdTimeSeries,
    };

    return osdChannelSegment;
}

/**
 * Truncates the channel segment timeseries to only be within the requested time range
 * if the channel segment timeseries is partially in and partially out of the requested
 * time range.
 * @param channelSegment channel segment to window
 * @param timeRange the time range tto window within
 * @returns a waveform channel segment
 */
export function truncateChannelSegmentTimeseries(
    channelSegment: ChannelSegment<Waveform>, startTime: number, endTime: number): ChannelSegment<Waveform> {
    // Window truncate the channel segments to the time range if there is overlap
    if (channelSegment.timeseries && channelSegment.timeseries.length > 0
        && (startTime > channelSegment.startTime || endTime < channelSegment.endTime)) {

        const windowedTimeseries = channelSegment.timeseries.map((timeseries: Waveform) => {
            // Calculate the window for the smaller waveform to add
            const startSampleIndex = (startTime - channelSegment.startTime) * timeseries.sampleRate;
            const endSampleIndex = ((endTime - startTime) * timeseries.sampleRate) + startSampleIndex;

            // Window the values of the times
            const values = timeseries.values.slice(startSampleIndex, endSampleIndex - 1);
            const value = {
                ...timeseries,
                startTime,
                sampleCount: values.length,
                values
            };
            return value;
        });

        // Replace timeseries with windowed timeseries
        return {
            ...channelSegment,
            startTime,
            endTime,
            timeseries: windowedTimeseries,
        };
    }
    return channelSegment;
}
