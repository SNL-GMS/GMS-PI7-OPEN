import * as config from 'config';
import * as waveformFilterMockBackend from './waveform-filter-mock-backend';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { HttpClientWrapper } from '../util/http-wrapper';
import { FilteredWaveformChannelSegment, CalculateWaveformSegmentInput, DerivedFilterChannelSegmentId } from './model';
import { WaveformFilter } from '../waveform-filter-definition/model';
import * as uuid4 from 'uuid/v4';
import { convertChannelSegmentFromAPIToOSD, convertChannelSegmentFromOSDToAPI } from '../util/channel-segment-utils';
import { ChannelSegment, OSDChannelSegment, OSDTimeSeries,
         TimeSeries, isWaveformChannelSegment } from '../channel-segment/model';
import { Waveform, OSDWaveform } from '../waveform/model';
import { WaveformFilterDefinitionProcessor } from '../waveform-filter-definition/waveform-filter-definition-processor';
import { ChannelSegmentProcessor } from '../channel-segment/channel-segment-processor';
import { performanceLogger } from '../log/performance-logger';
import * as channelSegmentMockBackend from '../channel-segment/channel-segment-mock-backend';

/**
 * Waveform filter processor, which handles a filtered waveform segments
 */
export class WaveformFilterProcessor {

    /** The singleton instance */
    private static instance: WaveformFilterProcessor;

    /**
     * Returns the singleton instance of the waveform filter processor.
     */
    public static Instance(): WaveformFilterProcessor {
        if (WaveformFilterProcessor.instance === undefined) {
            WaveformFilterProcessor.instance = new WaveformFilterProcessor();
            WaveformFilterProcessor.instance.initialize();
        }
        return WaveformFilterProcessor.instance;
    }

    /** Local configuration settings */
    private settings: any;

    /** HTTP client wrapper for communicationg with backend services */
    private httpWrapper: HttpClientWrapper;

    /**
     * Constructor - initialize the processor, loading settings and initializing
     * the HTTP client wrapper.
     */
    private constructor() {
        // Load configuration settings
        this.settings = config.get('filter-waveform');

        // Initialize an http client
        this.httpWrapper = new HttpClientWrapper();
    }

    /**
     * Helper function to lookup default filters before calling calculateFilteredWaveformSegment
     * rawChannelSegments
     */
    public async getFilteredWaveformSegments(rawChannelSegments: ChannelSegment<Waveform>[]):
        Promise<FilteredWaveformChannelSegment[]> {
        // Get the default set of waveform filter definitions for filtering
        const defaultFilters: WaveformFilter[]
            = (await WaveformFilterDefinitionProcessor.Instance().getDefaultFilters());
        return await this.calculateFilteredWaveformSegments(rawChannelSegments, defaultFilters);
    }

    /**
     * Retrieve waveform filters from the cache, filtering the results
     * down to those default filter names loaded from the config
     */
    public async calculateFilteredWaveformSegments(
        rawChannelSegments: ChannelSegment<Waveform>[], filters: WaveformFilter[]):
            Promise<FilteredWaveformChannelSegment[]> {

        // Call for the derived Channel Segments. Uses the ChannelSegmentMockBackend find
        // the Filtered ChannelSegment IDs instead of calling Filter streaming service to
        // calculate new Filtered Channel Segments
        // const filterIds = filters.map(f => f.id);
        // const filteredCS: FilteredWaveformChannelSegment[] =
        //     await this.getDerivedChannelSegments(rawChannelSegments, filterIds);
        // if (filteredCS && filteredCS.length > 0) {
        //     console.log("Yes found the derived the right way returning length: " + filteredCS.length);
        //     return filteredCS;
        // }
        logger.debug(`Waveform processor entered filtered waveforms`);

        // Construct the parameters for the OSD call for each filter
        const filterCallParameters = filters.map(filter => {
            // Segments are only valid when all timeseries have the same sample rate as the filter sample rate
            // tslint:disable-next-line:arrow-return-shorthand
            const validChannelSegments: ChannelSegment<Waveform>[] = rawChannelSegments.filter(channelSegment =>
                channelSegment.timeseries.every(waveform => waveform.sampleRate === filter.sampleRate)
            );

            return {
                filter,
                filterChannelIds: validChannelSegments.map(channelSegment => uuid4().toString()),
                channelSegments: validChannelSegments,
            };
        }
        ).filter(parameters => // Remove all filters that have no valid channel segments to filter
            parameters.filterChannelIds.length > 0 && parameters.channelSegments.length > 0
        );

        // Segments are only valid when all timeseries have the same sample rate as the filter sample rate
        // Call OSD for filtered channel segments then associate filtered channel segments with raw
        const promises = filterCallParameters.map(async parameters =>
            this.calculateWaveformSegments(
                parameters.filter, parameters.filterChannelIds, parameters.channelSegments
            ));
        const channelSegmentsArray = await Promise.all(promises);
        const filteredWaveforms: FilteredWaveformChannelSegment[][] =
        channelSegmentsArray.map((channelSegments, index) =>
            channelSegments.map((channelSegment: ChannelSegment<Waveform>) => {
            // Find raw channel segment that this filtered segment is associated with through constructed id
            const rawChannelSegmentIndex: number =
            filterCallParameters[index].filterChannelIds.findIndex(filterChannelId =>
                filterChannelId === channelSegment.channelId
            );
            // Store the Filtered Channel Segment in the ChannelSegmentProcessor for future retrieval
            ChannelSegmentProcessor.Instance().addOrUpdateToCache(channelSegment);

            // Associate filter id value and raw channel segment channel id for filtered channel segment
            return {
                ...channelSegment,
                wfFilterId: filterCallParameters[index].filter.id,
                sourceChannelId: filterCallParameters[index].channelSegments[rawChannelSegmentIndex].channelId,
            };
        }));
        const toReturn = filteredWaveforms.reduce(
            (prev: FilteredWaveformChannelSegment[], curr: FilteredWaveformChannelSegment[]) =>
                [...prev, ...curr],
            [] // Initial empty value of prev for first curr
        );
        // Concatenate all filtered channel segment lists together when promises complete
        return toReturn;
    }

    /**
     * Retrieve waveform filters from the cache, filtering the results
     * down to those default filter names loaded from the config
     */
    public async calculateWaveformSegments(
        filter: WaveformFilter,
        filterChannelOutputIds: string[],
        channelSegments: ChannelSegment<Waveform>[]
    ): Promise<ChannelSegment<Waveform>[]> {

        // Retrieve the request configuration for the service call
        const requestConfig = this.settings.backend.services.calculateWaveformSegments.requestConfig;

        // Truncate the channel segment waveforms first then convert to OSD format
        const osdChannelSegments: OSDChannelSegment<OSDTimeSeries>[] = channelSegments.map(channelSegment =>
            ({
                ...channelSegment,
                timeseries: this.truncateWaveforms(channelSegment.timeseries),
            })
        ).map(convertChannelSegmentFromAPIToOSD);

        // Create map between between derived channel (we created) and reference channel
        const idMap = {};
        channelSegments.forEach((segment, index) => {
            idMap[segment.channelId] = filterChannelOutputIds[index];
        });
        const channelSegIds = osdChannelSegments.map(cs => `${cs.id}${filter.id}`);
        const query: CalculateWaveformSegmentInput = {
            pluginParams: filter,
            channelSegments: osdChannelSegments as OSDChannelSegment<OSDWaveform>[],
            inputToOutputChannelIds: idMap,
        };
        // tslint:disable-next-line:max-line-length
        logger.debug(`Sending service request: ${JSON.stringify(requestConfig, undefined, 2)} query: ${JSON.stringify(query, undefined, 2)}`);
        performanceLogger.performance('filterChannelSegment', 'requestedFromService', channelSegIds[0]);
        const channelSegmentsToReturn = this.httpWrapper.request(requestConfig, query)
            .then<ChannelSegment<Waveform>[]>((responseData: OSDChannelSegment<OSDWaveform>[]) => {
                performanceLogger.performance('filterChannelSegment', 'returnedFromService', channelSegIds[0]);
                // Cache filters after request
                if (responseData && responseData.length > 0) {
                    return responseData.map(convertChannelSegmentFromOSDToAPI) as ChannelSegment<Waveform>[];
                }

                // No data, pass through
                return [];
            });

        return channelSegmentsToReturn;
    }

    /**
     * trying to get derived segments
     * @param unfilteredChannelSegments Channel Segments
     * 
     */
    public async getDerivedChannelSegments(
        unfilteredChannelSegments: ChannelSegment<TimeSeries>[], filterIds: string[]):
        Promise<FilteredWaveformChannelSegment[]> {

        // Loop thru the ChannelSegments
        const promises = unfilteredChannelSegments.map(async unfilteredCS =>
            await this.getDerivedChannelSegmentsForUnfilteredSegment(unfilteredCS, filterIds));
        const results = await Promise.all(promises);
        const filteredCSArrays = results.filter(fcs => fcs !== undefined);
        const filteredCSs = filteredCSArrays.reduce(
            (prev: FilteredWaveformChannelSegment[], curr: FilteredWaveformChannelSegment[]) =>
            [...prev, ...curr],
            [] // Initial empty value of prev for first curr
        );
        return filteredCSs;
    }

    /**
     * Individual Channel Segment call for filtered version of segment
     * @param unfilteredCS 
     * @param filterIds 
     */
    public async getDerivedChannelSegmentsForUnfilteredSegment(
        unfilteredCS: ChannelSegment<TimeSeries>, filterIds: string[]):
            Promise<FilteredWaveformChannelSegment[]>{
                // Create empty list of the results to be returned
        // const filteredCSs: FilteredWaveformChannelSegment[] = [];
        // Returns a list of entries with Filter Id and CS Id
        const derivedCSIds: DerivedFilterChannelSegmentId[] =
            channelSegmentMockBackend.getDerivedChannelSegments(unfilteredCS.id, filterIds);
        if (derivedCSIds && derivedCSIds.length > 0) {
            // Loop thru creating the FilteredWaveformChannelSegment
            const promises = derivedCSIds.map(async derivedId =>
                await ChannelSegmentProcessor.Instance().getChannelSegment(derivedId.csId)
                    .then((cs: ChannelSegment<TimeSeries>): FilteredWaveformChannelSegment =>
                        this.populateFilteredWaveformChannelSegment(cs, derivedId.wfFiltertId)));
            return await Promise.all(promises);
        }// if derived ids
    }

    /**
     * stuff
     * @param filteredCS Stuff
     * @param filterId 
     */
    private populateFilteredWaveformChannelSegment(
        filteredCs: ChannelSegment<TimeSeries>, filterId: string): FilteredWaveformChannelSegment {
        let fcs: FilteredWaveformChannelSegment;
        if (filteredCs && isWaveformChannelSegment(filteredCs)) {
            fcs = ({
                ...filteredCs,
                sourceChannelId: filteredCs.channelId,
                wfFilterId: filterId
            });
        }
        return fcs;
    }

    /**
     * Initialize the waveform filter processor, setting up a mock backend if configured to do so.
     */
    private initialize(): void {

        logger.info('Initializing the waveform filter processor - Mock Enable: %s', this.settings.backend.mock.enable);

        // If service mocking is enabled, initialize the mock backend
        if (this.settings.backend.mock.enable) {
            waveformFilterMockBackend.initialize(this.httpWrapper.createHttpMockWrapper());
        }
    }

    /**
     * Shortens waveforms
     * @param waveforms waveforms[]
     * @returns a Waveform[]
     */
    private truncateWaveforms(waveforms: Waveform[]): Waveform[] {
        return waveforms.map((waveform: Waveform) => {
            // If sample size is less than length use it, otherwise short-circuit the boolean if below
            const waveformSampleSize: number
                = this.settings.numberFilterSamples < waveform.values.length
                    ? this.settings.numberFilterSamples
                    : waveform.values.length;
            const numSecs = waveformSampleSize / waveform.sampleRate;

            const startTimeNum = waveform.startTime ? waveform.startTime : 0;
            const endTimeNum = waveform.startTime ? waveform.startTime + numSecs : numSecs;

            // Truncate the waveform if enabled and valid otherwise return same waveform
            if (waveformSampleSize !== waveform.values.length && !this.settings.backend.mock.enable) {
                const newWaveform = {
                    ...waveform,
                    sampleRate: waveformSampleSize / numSecs,
                    values: waveform.values.slice(0, waveformSampleSize),
                    sampleCount: waveformSampleSize,
                    startTime: startTimeNum,
                    endTime: endTimeNum,
                };
                return newWaveform;
            }
            return waveform;
        });
    }
}
// Initialize at startup
WaveformFilterProcessor.Instance();
