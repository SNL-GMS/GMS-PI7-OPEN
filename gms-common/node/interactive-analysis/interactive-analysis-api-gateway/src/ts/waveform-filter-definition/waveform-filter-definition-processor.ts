import * as model from './model';
import * as config from 'config';
import * as wfFilterDefinitionMockBackend from './waveform-filter-definition-mock-backend';
import { HttpClientWrapper } from '../util/http-wrapper';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { configProcessor } from '../config/config-processor';

// TODO replace with a more robust caching solution
/**
 * Encapsulates waveform filter data cached in memory
 */
interface WaveformFilterDataCache {
    waveformFilters: model.WaveformFilter[];
}

/**
 * Waveform filter cache results
 */
interface WaveformFilterCacheResults {
    hits: model.WaveformFilter[];
    misses: string[];
}

/**
 * API gateway processor for waveform filter data APIs. This class supports:
 * - data fetching & caching from the backend service interfaces
 * - mocking of backend service interfaces based on test configuration
 * - session management
 * - GraphQL query resolution from the user interface client
 */
export class WaveformFilterDefinitionProcessor {

    /** The singleton instance */
    public static instance: WaveformFilterDefinitionProcessor;

    /**
     * Returns the singleton instance of the waveform filter definition processor.
     */
    public static Instance(): WaveformFilterDefinitionProcessor {
        if (WaveformFilterDefinitionProcessor.instance === undefined) {
            WaveformFilterDefinitionProcessor.instance = new WaveformFilterDefinitionProcessor();
            WaveformFilterDefinitionProcessor.instance.initialize();
        }
        return WaveformFilterDefinitionProcessor.instance;
    }

    /** Local configuration settings */
    private settings: any;

    /** HTTP client wrapper for communicationg with backend services */
    private httpWrapper: HttpClientWrapper;

    /** Local cache of data fetched from the backend */
    private dataCache: WaveformFilterDataCache = {
        waveformFilters: [],
    };

    /** Default filters */
    private defaultFilters: string[] = [];

    /**
     * Constructor - initialize the processor, loading settings and initializing
     * the HTTP client wrapper.
     */
    private constructor() {

        // Load configuration settings
        this.settings = config.get('waveformFilterDefinition');

        // Initialize an http client
        this.httpWrapper = new HttpClientWrapper();
    }

    /**
     * Retrieve waveform filters from the cache, filtering the results
     * down to those default filter names loaded from the config
     * @returns a WaveformFilter[] as a promise
     */
    public async getDefaultFilters(): Promise<model.WaveformFilter[]> {
        // Retrieve the requested masks via backend service calls
        // TODO update the cache with results when available

        // Retrieve the request configuration for the service call
        const requestConfig = this.settings.backend.services.filtersByIds.requestConfig;

        // Query OSD for waveform filters not found in the cache (names returned from find WF filter cache)
        const cacheResults: WaveformFilterCacheResults = this.computeCacheResults(this.defaultFilters);

        // Check if the cache has all values requested, in which case http call unnecessary
        if (cacheResults.misses.length === 0) {
            return cacheResults.hits;
        }

        const query = {
            ids: cacheResults.misses,
        };

        return this.httpWrapper.request(requestConfig, query)
            .then<model.WaveformFilter[]>((responseData: model.WaveformFilter[]) => {
                // Cache filters after request
                if (responseData && responseData.length > 0) {
                    responseData.forEach((filter: model.WaveformFilter) => {
                        if (!this.dataCache.waveformFilters.find(cachedFilter => cachedFilter.id === filter.id)) {
                            this.dataCache.waveformFilters.push(filter);
                        }
                    });
                }

                return responseData;
            })
            .then<model.WaveformFilter[]>((responseFilters: model.WaveformFilter[]) => {
                const updatedResponseFilters: model.WaveformFilter[] = responseFilters.concat(cacheResults.hits);
                return updatedResponseFilters;
            });
    }

    /**
     * Updates wavefrom filters
     * @param changedFilters list of changed filters
     * @returns a WaveformFilter[]
     */
    public updateWaveformFilters(changedFilters: model.WaveformFilter[]): model.WaveformFilter[] {
        // FIXME figure out a better way of doing this
        changedFilters.forEach(cwf => {
            let foundOne = false;
            this.dataCache.waveformFilters.forEach((wf, i) => {
                if (cwf.id === wf.id) {
                    this.dataCache.waveformFilters[i] = cwf;
                    foundOne = true;
                }
            });
            if (!foundOne) {
                this.dataCache.waveformFilters.push(cwf);
            }
        });
        return changedFilters;
    }

    /**
     * Initialize the waveform filter processor, setting up a mock backend if configured to do so.
     */
    private initialize(): void {

        logger.info('Initializing the waveform filter definition processor');

        // Retrieve default waveform filter names from the config processor
        this.defaultFilters = configProcessor.getConfigByKey('waveformFilterIds');

        // If service mocking is enabled, initialize the mock backend
        if (this.settings.backend.mock.enable) {
            wfFilterDefinitionMockBackend.initialize(this.httpWrapper.createHttpMockWrapper());
        }
    }

    /**
     * Computes the cache results
     * @param waveformFilterIds  list of wavefrom filter IDs
     * @returns WaveformFilterCacheResults
     */
    private computeCacheResults(waveformFilterIds: string[]): WaveformFilterCacheResults {
        const cacheHits: model.WaveformFilter[]
            = this.dataCache.waveformFilters.filter((cachedFilter: model.WaveformFilter) =>
                waveformFilterIds.find((filterId: string) => cachedFilter.id === filterId));

        const cacheMisses: string[] = waveformFilterIds.filter(id => !cacheHits.find(hit => hit.id === id));

        return {
            hits: cacheHits,
            misses: cacheMisses,
        };
    }
}
// Initialize at startup
WaveformFilterDefinitionProcessor.Instance();
