import { filter } from 'lodash';
import * as path from 'path';
import * as config from 'config';
import { readJsonData, resolveTestDataPaths } from '../util/file-parse';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { HttpMockWrapper } from '../util/http-wrapper';
import * as model from './model';

/**
 * Mock backend HTTP services providing access to processing waveform filter data. If mock services are enabled in the
 * configuration file, this module loads a test data set specified in the configuration file and configures
 * mock HTTP interfaces for the API gateway backend service calls.
 */

/**
 * Encapsulates backend data supporting retrieval by the API gateway.
 */
interface WfFilterDataStore {
    wfFilters: model.WaveformFilter[];
    filterChannels: model.FilterProcessingChannel[];
}

/**
 * Waveform filter query input
 */
interface WaveformFilterQueryInput {
    ids: string[];
}

/**
 * Waveform filter channel IDs query input
 */
interface WaveformFilterChannelIdsQueryInput {
    sourceChannelId: string;
    wfFilters: model.WaveformFilter[];
}

// Declare a data store for the mock waveform filter backend
let dataStore: WfFilterDataStore;

/**
 * Retrieve a list of waveform filters that matches the filter ids list
 * @param ids The list of filter ids desired
 * @returns a WaveformFilter[]
 */
function getFiltersByIds(input: WaveformFilterQueryInput): model.WaveformFilter[] {
    logger.debug(`Retreiving waveform filters for ids - ${JSON.stringify(input, undefined, 2)}`);

    // Handle uninitialized data store
    handleUnitializedDataStore();

    // Handle undefined input
    if (!input || !input.ids || input.ids.length === 0) {
        throw new Error('Unable to retrieve Waveform filters by channel ids for empty list of filter ids');
    }

    const filters = filter(dataStore.wfFilters, wfFilter =>
        wfFilter.id === input.ids.find(id => id === wfFilter.id)
    );
    return filters;
}

/**
 * Retrieve a list of waveform filters that matches the filter ids list
 * @param ids The list of filter ids desired
 * @returns a FilterProcessingChannel[]
 */
export function getChannelFilters(input: WaveformFilterChannelIdsQueryInput): model.FilterProcessingChannel[] {
    logger.debug(`Retreiving filter channel ids - ${JSON.stringify(input, undefined, 2)}`);
    // Handle uninitialized data store
    handleUnitializedDataStore();

    // Handle undefined input
    if (!input || !input.sourceChannelId || !input.wfFilters || input.wfFilters.length === 0) {
        throw new Error('Unable to retrieve Channel ids for empty list of filter ids');
    }

    return dataStore.filterChannels
        .filter((filterChannel: model.FilterProcessingChannel) =>
            filterChannel.sourceChannelId === input.sourceChannelId
        ).filter((filterChannel: model.FilterProcessingChannel) =>
            input.wfFilters.findIndex(
                (wfFilter: model.WaveformFilter) => wfFilter.id === filterChannel.filterParamsId) !== -1);
}

/**
 * Configure mock HTTP interfaces for a simulated set of waveform filter backend services.
 * @param httpMockWrapper The HTTP mock wrapper used to configure mock backend service interfaces
 */
export function initialize(httpMockWrapper: HttpMockWrapper): void {
    logger.info('Initializing mock backend for waveform filter data');

    if (!httpMockWrapper) {
        throw new Error('Cannot initialize mock waveform filter services with undefined HTTP mock wrapper');
    }

    // Load test data from the configured data set
    dataStore = loadTestData();

    // Load the waveform filter backend service config settings
    const backendConfig = config.get('waveformFilterDefinition.backend');

    // Configure mock service interfaces
    httpMockWrapper.onMock(backendConfig.services.filtersByIds.requestConfig.url, getFiltersByIds);
}

/**
 * Load test data into the mock backend data store from the configured test data set.
 * @returns a WfFilterDataStore
 */
function loadTestData(): WfFilterDataStore {

    // Get test data configuration settings
    const testDataConfig = config.get('testData.additionalTestData');

    // Read the necessary files into arrays of objects
    const dataPath = resolveTestDataPaths().additionalDataHome;

    logger.info(`Loading waveform filter test data from path: ${dataPath}`);

    // Read the test data set from file; parse into object structure representing rows in the file
    // This is brittle if id ever changes - the following code assumes that id is a string and is non-optional
    const filters: model.WaveformFilter[] =
        readJsonData(dataPath.concat(path.sep).concat(testDataConfig.waveformFilterFileName));

    const filtChannels: model.FilterProcessingChannel[] =
        readJsonData(dataPath.concat(path.sep).concat(testDataConfig.filterChannelFileName));
    return {
        wfFilters: filters,
        filterChannels: filtChannels
    };
}

/**
 * Handle cases where the data store has not been initialized.
 */
function handleUnitializedDataStore() {
    // If the data store is uninitialized, throw an error
    if (!dataStore) {
        throw new Error('Mock backend waveform filter processing data store has not been initialized');
    }
}
