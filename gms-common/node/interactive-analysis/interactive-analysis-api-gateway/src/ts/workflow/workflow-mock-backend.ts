import * as config from 'config';
import * as path from 'path';
import { gatewayLogger  as logger } from '../log/gateway-logger';
import { readJsonData, resolveTestDataPaths } from '../util/file-parse';
import { HttpMockWrapper } from '../util/http-wrapper';
import * as model from './model';

export let currentlyOpenActivityId = '';

/**
 * Mock backend HTTP services providing access to the workflow data. If mock services are enabled in the
 * configuration file, this module loads a test data set specified in the configuration file and configures
 * mock HTTP interfaces for the API gateway backend service calls.
 */

// Read the workflow-related data sets from JSON files
let workflowCache: model.WorkflowDataCache;

/**
 * Configure mock HTTP interfaces for a simulated set of waveform filter backend services.
 * @param httpMockWrapper The HTTP mock wrapper used to configure mock backend service interfaces
 */
export function initialize(httpMockWrapper: HttpMockWrapper): void {
    logger.info('Initializing the interval service');

    if (!httpMockWrapper) {
        throw new Error('Cannot initialize mock waveform filter services with undefined HTTP mock wrapper');
    }

    // Load test data from the configured data set
    loadTestData();

    // Load the workflow backend service config settings
    const backendConfig = config.get('workflow.backend');

    httpMockWrapper.onMock(backendConfig.services.workflowData.requestConfig.url, getWorkflowData);
}

/**
 * Load test data into the mock backend data store from the configured test data set.
 */
function loadTestData() {
    const testDataConfig = config.get('testData.additionalTestData');
    // Read the file path to the workflow-related data sets from configuration
    const dataPath = resolveTestDataPaths().additionalDataHome;

    logger.info(`Loading workflow test data from path: ${dataPath}`);

    // Read the workflow-related data sets from JSON files
    workflowCache = readJsonData(dataPath.concat(path.sep).concat(testDataConfig.workflowFilename))[0];
}

/**
 * Retrieves the workflow data loaded from the UEB test data set.
 * @returns a WorkflowDataCache as a promise
 */
export async function getWorkflowData(): Promise<model.WorkflowDataCache> {
    return workflowCache;
}
