import { get } from 'lodash';
import * as path from 'path';
import * as config from 'config';
import { readJsonData, resolveTestDataPaths } from '../util/file-parse';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { HttpMockWrapper } from '../util/http-wrapper';

/**
 * Mock backend HTTP services providing access to configuration data used by the API gateway.
 * If mock services are enabled in the configuration file, this module loads a test configuration
 * data set specified in the configuration file, and configures mock HTTP interfaces for API
 * gateway backend service calls.
 */

/**
 * Encapsulates the query parameters used to retrieve a configuration item by key.
 */
export interface ConfigKeyInput {
    key: string;
}

// Declare a backend data store for the mock config backend
let configDataStore: any;

/**
 * Retrieve a configuration value for the provided key.
 * @param input The query parameters used to retrieve configuration data by key.
 */
export function getConfigByKey(input: ConfigKeyInput): any {
    // Handle undefined input
    if (!input || !input.key) {
        throw new Error('Unable to retrieve configuration item for undefined key');
    }

    // Handle cases where the config data store has not been initialized
    handleUnitializedDataStore();
    let configItem: any;

    // Use lodash to retrieve and return the requested property from the configuration object
    try {
        configItem = get(configDataStore, input.key);
    } catch (error) {
        logger.error(error);
    }

    // Return undefined if an exception is thrown accessing the configuration object
    return configItem;
}

/**
 * Configure mock HTTP interfaces for a simulated set of station-related backend services.
 * @param httpMockWrapper The HTTP mock wrapper used to configure mock backend service interfaces
 */
export function initialize(httpMockWrapper: HttpMockWrapper) {

    logger.info('Initializing mock backend for config data');
    logger.info(`YAML name: ${config.get('yamlName')}`);

    if (!httpMockWrapper) {
        throw new Error('Cannot initialize mock config services with undefined HTTP client');
    }

    // Load test data from the configured data set
    configDataStore = loadTestData();

    // Load the station backend service config settings
    const backendConfig = config.get('config.backend');

    // Configure mock service interfaces
    httpMockWrapper.onMock(backendConfig.services.configByKey.requestConfig.url, getConfigByKey);
}

/**
 * Load test data into the mock backend data store from the configured test data set.
 */
function loadTestData(): any {

    const dataPath = resolveTestDataPaths().additionalDataHome;

    // Read the network definitions from the test data file set
    return readJsonData(dataPath.concat(path.sep).concat(config.get('testData.additionalTestData.uiConfigFileName')));
}

/**
 * Handle cases where the data store has not been initialized.
 */
function handleUnitializedDataStore() {
    // If the data store is uninitialized, throw an error
    if (!configDataStore) {
        throw new Error('Mock backend config data store has not been initialized');
    }
}
