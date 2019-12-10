import { get } from 'lodash';
import * as config from 'config';
import { HttpClientWrapper } from '../util/http-wrapper';
import { gatewayLogger as logger } from '../log/gateway-logger';
import * as configMockBackend from './config-mock-backend';

/**
 * API gateway processor for configuration-related data APIs. This class supports:
 * - data fetching & caching from the backend service interfaces
 * - mocking of backend service interfaces based on test configuration
 * - session management
 * - GraphQL query resolution from the user interface client
 */
class ConfigProcessor {

    /** Local configuration settings */
    private settings: any;

    /** HTTP client wrapper for communicationg with backend services */
    private httpWrapper: HttpClientWrapper;

    /** Local cache of data fetched from the backend */
    private configCache: any;

    /** Boolean that checks if processor has been initialized */
    private isInitialized: boolean = false;

    /**
     * Constructor - initialize the ConfigProcessor
     */
    public constructor() {
        // Load configuration settings
        this.settings = config.get('config');

        // Initialize an http client wrapper
        this.httpWrapper = new HttpClientWrapper();
    }

    /**
     * Initialize the configuration processor, fetching configuration data from the backend
     * This function sets up a mock backend if configured to do so.
     */
    public initialize() {
        if (this.isInitialized) {
            return;
        }
        logger.info('Initializing the configuration processor');

        // If service mocking is enabled, initialize the mock backend
        if (this.settings.backend.mock.enable) {
            configMockBackend.initialize(this.httpWrapper.createHttpMockWrapper());
        }

        // Cache configuration data needed to support the interactive analysis UI
        const configData = this.fetchConfigByKey(this.settings.rootKey);
        this.configCache = configData;
    }

    /**
     * Retrieve a configuration by key
     * @param key The key to retrieve the configuration value for
     */
    public getConfigByKey(key: string): any {
        logger.debug(`Local fetching configuration for key: ${key}`);
        return get(this.configCache, key);
    }

    /**
     * Fetch configuration data from the backend for the provided key string.
     * This is an asynchronous function.
     * If the provided key is undefined, this function throws an error.
     * This function propagates errors from the underlying HTTP call.
     * @param key The key to retrieve the configuration value for
     */
    public fetchConfigByKey(key: string): any {
        logger.debug(`Fetching configuration for key: ${key}`);

        // Handle invalid input
        if (!key) {
            throw new Error('Cannot fetch configuration for an undefined key');
        }

        // Retrieve the settings for the service
        // const serviceConfig = this.settings.backend.services.configByKey.requestConfig;
        const input: configMockBackend.ConfigKeyInput = {key};
        // Call the service and extract the response data
        return configMockBackend.getConfigByKey(input);
    }
}

// Export an initialized instance of the processor
export const configProcessor: ConfigProcessor = new ConfigProcessor();
configProcessor.initialize();
