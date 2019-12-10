import * as express from 'express';
import * as config from 'config';
import { gatewayLogger as logger, gatewayLogger } from '../log/gateway-logger';
import { ChannelSegmentProcessor } from '../channel-segment/channel-segment-processor';
import { WaveformFilterDefinitionProcessor } from '../waveform-filter-definition/waveform-filter-definition-processor';
import { WaveformFilterProcessor } from '../waveform-filter/waveform-filter-processor';
import { FilteredWaveformChannelSegment, RawAndFilteredChannelSegments } from '../waveform-filter/model';
import { WaveformFilter } from '../waveform-filter-definition/model';
import { ChannelSegment, isWaveformChannelSegment, ChannelSegmentType } from '../channel-segment/model';
import { Waveform } from './model';
import { includes } from 'lodash';
import { stationProcessor } from '../station/station-processor';

/**
 * Waveform Processor that handles channel segments and time series
 */
export class WaveformProcessor {

    /** The singleton instance */
    private static instance: WaveformProcessor;

    /**
     * Returns the singleton instance of the waveform filter processor.
     */
    public static Instance(): WaveformProcessor {
        if (WaveformProcessor.instance === undefined) {
            WaveformProcessor.instance = new WaveformProcessor();
            WaveformProcessor.instance.initialize();
        }
        return WaveformProcessor.instance;
    }

    /** Local configuration settings */
    private settings: any;

    /**
     * Constructor - initialize the processor, loading settings and initializing
     * the HTTP client wrapper.
     */
    private constructor() {
        // Load configuration settings
        this.settings = config.get('waveform');
    }

    /**
     * Retrieve raw waveform from the cache.
     */
    public async getRawWaveformSegmentsByChannels
        (startTime: number,
         endTime: number,
         channelIds: string[]):
        Promise<ChannelSegment<Waveform>[]> {
        // Retrieve the raw channel segments for channel ids from channel segment processor
        // !TODO: FK_BEAM will be removed once these are retrieved from the signal detection
        // ! Beams are only associated with the station id
        const defaultStationIds = (await stationProcessor.getDefaultStations()).map(s => s.id);

        // FKBeam
        if (channelIds.every(id => includes(defaultStationIds, id))) {
            logger.warn('Warning FK_BEAM Channel Segments is depreciated and should not be called!');
            return [];
        } else {
            // Return Raw Channel Segments found
            return (await ChannelSegmentProcessor.Instance()
                .getChannelSegmentsByChannels(startTime, endTime, channelIds, ChannelSegmentType.ACQUIRED))
                .filter(isWaveformChannelSegment).filter(entry => entry.type === ChannelSegmentType.ACQUIRED);
        }
    }

    /**
     * Retrieve filtered waveform from the cache.
     */
    public async getFilteredWaveformSegmentsByChannels
        (startTime: number, endTime: number, channelIds: string[], filterIds?: string[]):
        Promise<FilteredWaveformChannelSegment[]> {

        const rawChannelSegments: ChannelSegment<Waveform>[] =
            await this.getRawWaveformSegmentsByChannels(startTime, endTime, channelIds);

        // Get the default set of waveform filter definitions for filtering
        const defaultFilters: WaveformFilter[]
            = (await WaveformFilterDefinitionProcessor.Instance().getDefaultFilters());

        const filters = filterIds
            ? defaultFilters.filter(wf => includes(filterIds, wf.id))
            : defaultFilters;

        // Filter the raw waveforms using the default filter definitions
        const filteredChannelSegments = await WaveformFilterProcessor.Instance()
            .calculateFilteredWaveformSegments(rawChannelSegments, filters)
            .catch(error => {
                logger.error(`Error getFilteredWaveformSegmentsByChannels failed `
                    + `returning empty filters: ${error}`);
                return [];
            });
        return filteredChannelSegments;
    }

    /**
     * Retrieve waveform filters from the cache, filtering the results
     * down to those default filter names loaded from the config
     */
    public async getWaveformSegmentsByChannels
        (startTime: number, endTime: number, channelIds: string[], filterIds?: string[]):
        Promise<RawAndFilteredChannelSegments[]> {

        const rawWaveforms: ChannelSegment<Waveform>[] =
            await this.getRawWaveformSegmentsByChannels(startTime, endTime, channelIds);

        // Get the default set of waveform filter definitions for filtering
        const defaultFilters: WaveformFilter[]
            = (await WaveformFilterDefinitionProcessor.Instance().getDefaultFilters());

        const filters = filterIds
            ? defaultFilters.filter(wf => includes(filterIds, wf.id))
            : defaultFilters;

        // Filter the raw waveforms using the default filter definitions
        const filteredChannelSegments: FilteredWaveformChannelSegment[]
            = await WaveformFilterProcessor.Instance()
                .calculateFilteredWaveformSegments(rawWaveforms, filters);

        // Try to associate channel segments grouped by channel id to associated filtered segments
        const map: RawAndFilteredChannelSegments[] = [];

        channelIds.forEach((channelId: string) => {
            map.push(
                {
                    channelId,
                    raw: rawWaveforms.filter(segment => segment.channelId === channelId),
                    filtered: filteredChannelSegments.filter(segment => segment.sourceChannelId === channelId),
                }
            );
        });
        return map;
    }

    /**
     * Initialize the waveform filter processor, setting up a mock backend if configured to do so.
     */
    private initialize(): void {
        logger.info('Initializing the waveform processor - Mock Enable: %s', this.settings.backend.mock.enable);
    }
}

/**
 * express request handler, 
 * @param req - should have query parameters start: number, end: number, channels: comma-separated list e.g. 1,2,3
 * @param res - a message pack encoded set of model.Waveforms
 */
export const waveformRawSegmentRequestHandler: express.RequestHandler = async (req, res) => {
    const startTime = Number(req.query.startTime);
    const endTime = Number(req.query.endTime);
    const channelIds: string[] | null = req.query.channelIds && req.query.channelIds.split(',');
    // If channels is specified, return those channels
    WaveformProcessor.Instance().getRawWaveformSegmentsByChannels(startTime, endTime, channelIds)
        .then((waveforms: ChannelSegment<Waveform>[]) => {
            res.send(waveforms);
        }).catch(e => gatewayLogger.warn(e));
};

/**
 * express request handler, 
 * @param req - should have query parameters start: number, end: number, channels: comma-separated list e.g. 1,2,3
 * @param res - a message pack encoded set of model.Waveforms
 */
export const waveformFilteredSegmentRequestHandler: express.RequestHandler = async (req, res) => {
    const startTime = Number(req.query.startTime);
    const endTime = Number(req.query.endTime);
    const channelIds: string[] | null = req.query.channelIds && req.query.channelIds.split(',');
    const filterIds: string[] | null = req.query.filterIds && req.query.filterIds.split(',');

    // If channels is specified, return those channels
    WaveformProcessor.Instance().getFilteredWaveformSegmentsByChannels(startTime, endTime, channelIds, filterIds)
        .then((waveforms: FilteredWaveformChannelSegment[]) => {
            res.send(waveforms);
        }).catch(e => gatewayLogger.warn(e));
};

/**
 * express request handler, 
 * @param req - should have query parameters start: number, end: number, channels: comma-separated list e.g. 1,2,3
 * @param res - a message pack encoded set of model.Waveforms
 */
export const waveformSegmentRequestHandler: express.RequestHandler = async (req, res) => {
    const startTime = Number(req.query.startTime);
    const endTime = Number(req.query.endTime);
    const channelIds: string[] | null = req.query.channelIds && req.query.channelIds.split(',');
    const filterIds: string[] | null = req.query.filterIds && req.query.filterIds.split(',');

    // If channels is specified, return those channels
    WaveformProcessor.Instance().getWaveformSegmentsByChannels(startTime, endTime, channelIds, filterIds)
        .then((waveforms: RawAndFilteredChannelSegments[]) => {
            res.send(waveforms);
        }).catch(e => gatewayLogger.warn(e));
};
// Initialize at startup
WaveformProcessor.Instance();
