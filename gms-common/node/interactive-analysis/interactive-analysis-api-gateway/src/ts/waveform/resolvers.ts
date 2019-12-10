import { PubSub, withFilter } from 'graphql-subscriptions';
import * as config from 'config';
import { filter } from 'lodash';
import { Waveform } from './model';
import { TimeRange } from '../common/model';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { ChannelSegment, ChannelSegmentType } from '../channel-segment/model';
import { WaveformProcessor } from './waveform-processor';
import { FilteredWaveformChannelSegment } from '../waveform-filter/model';
import { performanceLogger } from '../log/performance-logger';

/**
 * Resolvers for the waveform API gateway
 */

// Create the publish/subscribe API for GraphQL subscriptions
export const pubsub = new PubSub();

// Load subscription configuration settings
const subConfig = config.get('waveform.subscriptions');

// GraphQL Resolvers
logger.info('Creating GraphQL resolvers for the waveform API...');
export const resolvers = {

    // Query resolvers
    Query: {
        getWaveformSegmentsByChannels: async (_, { timeRange, channelIds, filterIds }) =>
            await WaveformProcessor.Instance().getWaveformSegmentsByChannels(
                timeRange.startTime, timeRange.endTime, channelIds, filterIds),
        getRawWaveformSegmentsByChannels: async (_, { timeRange, channelIds }): Promise<ChannelSegment<Waveform>[]> => {
            let waveforms = await WaveformProcessor.Instance().getRawWaveformSegmentsByChannels(
                timeRange.startTime, timeRange.endTime, channelIds);
            // If we are returning Raw waveforms set the ChannelSegmentType to RAW
            if (waveforms && waveforms.length > 0) {
                waveforms.forEach(wf => wf.type = ChannelSegmentType.RAW);
            } else {
                waveforms = [];
            }
            return waveforms;
        },

        getFilteredWaveformSegmentsByChannels: async (
            _, { timeRange, channelIds, filterIds }): Promise<FilteredWaveformChannelSegment[]> => {
            performanceLogger.performance('getFilteredWaveformSegmentsByChannels', 'enteringResolver');
            const fwfs = await WaveformProcessor.Instance().getFilteredWaveformSegmentsByChannels(
                         timeRange.startTime, timeRange.endTime, channelIds, filterIds);
            performanceLogger.performance('getFilteredWaveformSegmentsByChannels', 'leavingResolver');
            return fwfs;
        }
    },
    // Subscription Resolvers
    Subscription: {
        waveformChannelSegmentsAdded: {
            // Set up the subscription to filter results down to those channel segments that overlap
            // a time range provided by the subscriber upon creating the subscription
            subscribe: withFilter(
                () => pubsub.asyncIterator(subConfig.channels.waveformChannelSegmentsAdded),
                async (payload, variables) => {

                    // If the subscriber has provided subscription input parameters
                    // (e.g. time range, channel IDs) filter the array of channel
                    // segments down to those that in the time range & matching an entry
                    // in the channel IDs
                    const timeRange: TimeRange = variables.timeRange;
                    const channelIds: string[] = variables.channelIds;

                    if (timeRange && channelIds) {
                        const segmentsAdded: ChannelSegment<Waveform>[] =
                            payload.waveformChannelSegmentsAdded;
                        payload.waveformChannelSegmentsAdded =
                            filter(segmentsAdded, segment =>
                                segment.startTime < timeRange.endTime &&
                                segment.endTime > timeRange.startTime &&
                                channelIds.indexOf(segment.channelId) > -1);
                    }

                    // Only send the subscription callback if one or more of the available
                    // channel segment notifications matched the subscribed-for time range
                    return payload.waveformChannelSegmentsAdded.length > 0;
                }
            )
        }
    },
};
