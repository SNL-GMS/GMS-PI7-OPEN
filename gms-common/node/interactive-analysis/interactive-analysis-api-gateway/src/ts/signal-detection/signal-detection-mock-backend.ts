import * as path from 'path';
import * as config from 'config';
import { readJsonData, resolveTestDataPaths } from '../util/file-parse';
import * as model from './model';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { HttpMockWrapper } from '../util/http-wrapper';
import { TimeRange } from '../common/model';
import { toEpochSeconds } from '../util/time-utils';
import { findArrivalTimeFeatureMeasurementValue } from '../util/signal-detection-utils';

/**
 * Encapsulates backend data supporting retrieval by the API gateway.
 */
interface SdDataStore {
    signalDetections: model.SignalDetection[];
}

/**
 * Signal detection for time range input
 */
interface SignalDetectionForTimeRangeInput {
    stationIds: string[];
    startTime: string;
    endTime: string;
}

// Declare a data store for the mock QC mask backend
let dataStore: SdDataStore;

/**
 * Initialize the mock backend for signal detections.
 * 
 * @param httpMockWrapper the http mock wrapper
 */
export function initialize(httpMockWrapper: HttpMockWrapper) {
    // Load test data from the configured data set
    dataStore = loadTestData();

    logger.info('Initializing the FK Mock-backend service');

    const backendConfig = config.get('signalDetection.backend');
    httpMockWrapper.onMock(backendConfig.services.sdsByStation.requestConfig.url,
                           getSignalDetectionsForTimerange);
    httpMockWrapper.onMock(backendConfig.services.saveSds.requestConfig.url,
                           saveSignalDetections);
}

/**
 * Help function to convert date string into epoch seconds
 */
export function getEpochSeconds(dateString: any): number {
    if (dateString === undefined) {
        return 0;
    }

    // Maybe if this is a number then it is epoch just return
    if (!isNaN(dateString)) {
        return dateString;
    }
    const milliSecs = 1000;
    return new Date(dateString).getTime() / milliSecs;
}

/**
 * Get signal detection hypothesis by ID
 * @param hypoId signal detection hypothesis ID
 * @returns a Signal Detection Hypothesis
 */
export function getSignalDetectionHypothesisById(hypoId: string): model.SignalDetectionHypothesis {
    let currentSDH: model.SignalDetectionHypothesis;
    dataStore.signalDetections.forEach(sd => {
        if (sd && sd.signalDetectionHypotheses && sd.signalDetectionHypotheses.length > 0) {
            if (sd.signalDetectionHypotheses[sd.signalDetectionHypotheses.length - 1].id === hypoId) {
                currentSDH = sd.signalDetectionHypotheses[sd.signalDetectionHypotheses.length - 1];
            }
        }
    });
    return currentSDH;
}

/**
 * Get signal detections for time range mapped to a station ID
 * @param requestConfig as SignalDetectionForTimeRangeInput
 * @returns a map as Map<string, model.SignalDetection[]>
 */
export function getSignalDetectionsForTimerange(
    requestConfig: SignalDetectionForTimeRangeInput): Map<string, model.SignalDetection[]> {

    const timeRange: TimeRange = {
        startTime: getEpochSeconds(requestConfig.startTime),
        endTime: getEpochSeconds(requestConfig.endTime)
    };

    const stationIds = requestConfig.stationIds;
    const mapEntries: Map<string, model.SignalDetection[]> = new Map();
    stationIds.forEach(stationId => {
        const filteredSds = dataStore.signalDetections.filter(sd => {
            if (sd.signalDetectionHypotheses && sd.signalDetectionHypotheses.length > 0) {
                const currentHypo = sd.signalDetectionHypotheses[sd.signalDetectionHypotheses.length - 1];

                // Look up the Arrival Time FM the measurement value is a string and not a number
                const arrivalTimeMeasurementValue = findArrivalTimeFeatureMeasurementValue(
                    currentHypo.featureMeasurements);
                const value: any = arrivalTimeMeasurementValue.value;
                const arrivalTimeEpoch = toEpochSeconds(value);
                if (arrivalTimeEpoch && stationId === sd.stationId &&
                    (arrivalTimeEpoch >= (timeRange.startTime) &&
                    arrivalTimeEpoch <= (timeRange.endTime))) {
                    return true;
                }
            }
        });
        mapEntries[stationId] = filteredSds;
    });
    return mapEntries;
}
/**
 * Lookup Signal Detection this is used by other mock backends
 * @param sdId Signal Detection Id
 * @returns SignalDetection matching sdId
 */
export function getSignalDetectioById(sdId: string): model.SignalDetection {
    return dataStore.signalDetections.find(sd => sd.id === sdId);
}

/**
 * Retrives signal detection by id
 * @param signalDetectionHypothesisId Id of detection to grab
 * @returns SignalDetection
 */
export function getSignalDetectionsByHypotheisId(signalDetectionHypothesisId: string): model.SignalDetection {
    // Find the signal detection that contains the a matching signal detection hypothesis
    return dataStore.signalDetections.find(sd =>
        sd.signalDetectionHypotheses && sd.signalDetectionHypotheses.length > 0 &&
        sd.signalDetectionHypotheses.findIndex(sdh => sdh.id === signalDetectionHypothesisId) > -1);
}

/**
 * Get signal detections hypothesis by ID
 * @param signalDetectionHypothesisId signal detection hypothesis ID
 * @returns a Signal Detection Hypothesis
 */
export function getSignalDetectionsHypotheisById(signalDetectionHypothesisId: string): model.SignalDetectionHypothesis {
    // Find the signal detection that contains the a matching signal detection hypothesis
    let foundHyp;
    dataStore.signalDetections.forEach(sd => {
        sd.signalDetectionHypotheses.forEach(hyp => {
            if (hyp.id === signalDetectionHypothesisId) {
                foundHyp = hyp;
            }
        });
    });
    return foundHyp;
}

/**
 * Load test data into the mock backend data store from the configured test data set.
 */
export function loadTestData(): SdDataStore {
    const testDataConfig = config.get('testData.standardTestDataSet');

    // Read the necessary UEB data set CSV files into arrays of objects
    const dataPath = resolveTestDataPaths().jsonHome;

    let signalDetections: model.SignalDetection[] = [];
    try {
    signalDetections = readJsonData(dataPath.concat(path.sep)
                     .concat(testDataConfig.signalDetection.signalDetectionFilename));
    } catch (e) {
        logger.error(`Failed to read signal detections from file:
            ${testDataConfig.signalDetection.signalDetectionFilename}`);
    }

    return { signalDetections };
}

/**
 * Saves signal detections to the data store
 */
export function saveSignalDetections(query: any): void {
    const sds = JSON.parse(query);
    if (sds) {
        sds.forEach(sd => {
            const index = dataStore.signalDetections.findIndex(dsSd => dsSd.id === sd.id);
            dataStore.signalDetections[index] = sd;
        });
    }
}
