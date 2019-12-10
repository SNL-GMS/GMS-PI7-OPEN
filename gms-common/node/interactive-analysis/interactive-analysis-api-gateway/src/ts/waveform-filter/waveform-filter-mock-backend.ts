import * as model from './model';
import { OSDChannelSegment, isOSDWaveformChannelSegment, ChannelSegmentType } from '../channel-segment/model';
import * as config from 'config';
import { gatewayLogger as logger } from '../log/gateway-logger';

import { HttpMockWrapper } from '../util/http-wrapper';
import * as channelSegmentMockBackend from '../channel-segment/channel-segment-mock-backend';
import { OSDWaveform } from '../waveform/model';
import { toEpochSeconds } from '../util/time-utils';
import { performanceLogger } from '../log/performance-logger';

/**
 * Encapsulates backend data supporting retrieval by the API gateway.
 */
let wfConfig;
let isInitialized = false;

/**
 * Initialize the waveform filter mock backend
 * @param httpMockWrapper lightweight wrapper
 */
export function initialize(httpMockWrapper: HttpMockWrapper): void {

    // If already initialized...
    if (isInitialized) {
        return;
    }
    logger.info('Initializing mock backend for waveform filter data');

    if (!httpMockWrapper) {
        throw new Error('Cannot initialize mock waveform filter services with undefined HTTP mock wrapper');
    }

    // Load the waveform filter backend service config settings
    wfConfig = config.get('filter-waveform');

    httpMockWrapper.onMock(
        wfConfig.backend.services.calculateWaveformSegments.requestConfig.url,
        calculateWaveformSegments
    );
    isInitialized = true;
}

/**
 * Retrieve a list of waveform filters that matches the filter ids list
 * @param ids The list of filter ids desired
 */
function calculateWaveformSegments(
    input: model.CalculateWaveformSegmentInput): OSDChannelSegment<OSDWaveform>[] {
    performanceLogger.performance('filterChannelSegment', 'enteringService',
                                  `${input.channelSegments[0].id}${input.pluginParams.id}`);
    // Handle undefined input
    if (!input || !input.inputToOutputChannelIds || Object.keys(input.inputToOutputChannelIds).length <= 0 ||
        !input.channelSegments || input.channelSegments.length === 0 ||
        !input.pluginParams) {
        logger.warn('Unable to calculate Waveform Segments input params are not properly populated. Input structures:');
        logger.warn(JSON.stringify(input.pluginParams));
        logger.warn(JSON.stringify(input.inputToOutputChannelIds));
        logger.warn(JSON.stringify(input.inputToOutputChannelIds));
        logger.warn(`Segment length: ${input.channelSegments.length}`);
        throw new Error('Unable to calculate Waveform Segments input params are not properly populated.');
    }

    const filteredChannelSegments: any[] = [];
    input.channelSegments.forEach((chanSeg, index) => {
        const startTime = toEpochSeconds(chanSeg.timeseries[0].startTime);
        const endTime = chanSeg.timeseries[0].sampleCount * chanSeg.timeseries[0].sampleRate + startTime;
        let foundSegments: OSDChannelSegment<OSDWaveform>[] = [];
        if (chanSeg.type === ChannelSegmentType.ACQUIRED) {
            foundSegments = channelSegmentMockBackend.getAcquiredFilteredChannelSegments(
                [chanSeg.channelId], startTime,
                endTime, input.pluginParams.id)
                .filter(isOSDWaveformChannelSegment);
        } else if (chanSeg.type === ChannelSegmentType.FK_BEAM || chanSeg.type === ChannelSegmentType.DETECTION_BEAM) {
            const chanStartTime = toEpochSeconds(chanSeg.startTime);
            const chanEndTime = toEpochSeconds(chanSeg.endTime);
            foundSegments = channelSegmentMockBackend.getBeamFilteredChannelSegments(
                chanStartTime, chanEndTime, input.pluginParams.id, chanSeg.name)
                .filter(isOSDWaveformChannelSegment);
        }

        foundSegments.forEach((segment: OSDChannelSegment<OSDWaveform>) => {
            const outputId = input.inputToOutputChannelIds[input.channelSegments[index].channelId];
            segment.channelId = outputId;
            filteredChannelSegments.push(segment);
        });
    });
    performanceLogger.performance('filterChannelSegment', 'returningFromService',
                                  `${input.channelSegments[0].id}${input.pluginParams.id}`);
    return filteredChannelSegments;
}
