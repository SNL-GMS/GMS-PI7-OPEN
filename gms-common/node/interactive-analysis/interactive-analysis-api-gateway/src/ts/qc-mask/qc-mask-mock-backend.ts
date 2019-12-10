import { filter, findIndex, flatMap } from 'lodash';
import * as path from 'path';
import * as config from 'config';
import { readJsonData, resolveTestDataPaths } from '../util/file-parse';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { HttpMockWrapper } from '../util/http-wrapper';
import * as model from './model';
import { toEpochSeconds } from '../util/time-utils';

/**
 * Mock backend HTTP services providing access to processing station data. If mock services are enabled in the
 * configuration file, this module loads a test data set specified in the configuration file and configures
 * mock HTTP interfaces for the API gateway backend service calls.
 */

/**
 * Encapsulates backend data supporting retrieval by the API gateway.
 */
interface QcMaskDataStore {
    qcMasks: model.QcMask[];
}

// Declare a data store for the mock QC mask backend
let dataStore: QcMaskDataStore;

/**
 * Configure mock HTTP interfaces for a simulated set of QC mask backend services.
 * @param httpMockWrapper The HTTP mock wrapper used to configure mock backend service interfaces
 */
export function initialize(httpMockWrapper: HttpMockWrapper) {

    logger.info('Initializing mock backend for QC mask data');

    if (!httpMockWrapper) {
        throw new Error('Cannot initialize mock QC mask services with undefined HTTP mock wrapper');
    }

    // Load test data from the configured data set
    dataStore = loadTestData();

    // Load the QC mask backend service config settings
    const backendConfig = config.get('qcMask.backend');

    // Configure mock service interfaces
    httpMockWrapper.onMock(backendConfig.services.masksByChannelIds.requestConfig.url, getQcMasksByChannelIds);
    httpMockWrapper.onMock(backendConfig.services.saveMasks.requestConfig.url, saveQcMasks);
}

/**
 * Retrieve QC Masks from the mock data store. Filter the results
 * down to those masks overlapping the input time range and matching
 * the input channel ID.
 * 
 * @param timeRange The time range in which to retreive QC masks 
 * @param channelId The channel ID to retrieve QC masks for
 */
function getQcMasksByChannelIds(input: any): any {
    logger.debug(`QC mask query inputs - ${JSON.stringify(input, undefined, 2)}`);
    // Handle uninitialized data store
    handleUnitializedDataStore();

    // Handle undefined input
    if (!input) {
        throw new Error('Unable to retrieve QC masks for undefined input');
    }

    // Handle undefined input time range
    if (!input['start-time'] || !input['end-time']) {
        throw new Error('Unable to retrieve QC masks for undefined time range');
    }

    // Handle undefined input channel ID
    if (!input['channel-ids']) {
        throw new Error('Unable to retrieve QC masks for undefined channel IDs');
    }
    // Retrieve the masks from the data store with channel ID matching the
    // input ID, and time range overlapping the input range.
    const startTime: number = toEpochSeconds(input['start-time']);
    const endTime: number = toEpochSeconds(input['end-time']);
    const channelIds = input['channel-ids'];
    const mapEntries = {};
    channelIds.forEach(chanId => {
        const filtMasks = filter(dataStore.qcMasks, mask => {
            const currentVersion = mask.qcMaskVersions[mask.qcMaskVersions.length - 1];
            const currStart = toEpochSeconds(currentVersion.startTime);
            const currEnd = toEpochSeconds(currentVersion.endTime);
            if (mask.channelId === chanId &&
                currStart < endTime &&
                currEnd > startTime) {
                return true;
            }
        }
        );
        mapEntries[chanId] = filtMasks;
    });
    return mapEntries;

}

/**
 * Load test data into the mock backend data store from the configured test data set.
 */
function loadTestData(): QcMaskDataStore {

    // Get test data configuration settings
    const additionalDataConfig = config.get('testData.additionalTestData');
    const stdsDataConfig = config.get('testData.standardTestDataSet');
    const paths = resolveTestDataPaths();

    // Read the necessary files into arrays of objects
    const dataPaths: string[] = [
        [paths.jsonHome, stdsDataConfig.qcMask.qcMaskFileName],
        [paths.additionalDataHome, additionalDataConfig.qcMaskFileName]].map(dataPath => {
        // If dataPath is a relative path, make it absolute by prefixing
        // it with the current working directory.
        if (!path.isAbsolute(dataPath[0])) {
            dataPath.unshift(process.cwd());
        }
        return dataPath.join(path.sep);
    });

    logger.info(`Loading QC mask test data from path(s): ${dataPaths.join(';')}`);

    // Read the test data set from file; parse into object structure representing rows in the file

    let qcMasks: model.QcMask[] = [];
    try {
        qcMasks = flatMap(dataPaths.map(p => {
            const masks: model.QcMask[] = readJsonData(p);
            return masks;
        }));
    } catch (e) {
        logger.error(`Failed to read qc masks from file: ${dataPaths.join(';')}`);
    }

    return {
        qcMasks
    };
}

/**
 * Add qcMasks to the data store, replace mask if already defined.
 * @param qcMasks 
 */
function saveQcMasks(qcMasks: model.QcMask[]) {
    qcMasks.forEach(mask => {
        // find index otherwise just add it
        const index = findIndex(dataStore.qcMasks, qcM => qcM.id === mask.id);
        if (index >= 0) {
            dataStore.qcMasks[index] = mask;
        } else {
            dataStore.qcMasks.push(mask);
        }
    });
}

/**
 * Handle cases where the data store has not been initialized.
 */
function handleUnitializedDataStore() {
    // If the data store is uninitialized, throw an error
    if (!dataStore) {
        throw new Error('Mock backend QC mask processing data store has not been initialized');
    }
}
