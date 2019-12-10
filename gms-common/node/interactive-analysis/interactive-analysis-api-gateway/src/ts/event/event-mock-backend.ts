import * as uuid4 from 'uuid/v4';
import * as path from 'path';
import * as config from 'config';
import { readJsonData, resolveTestDataPaths } from '../util/file-parse';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { HttpMockWrapper } from '../util/http-wrapper';
import * as model from './model-osd';

import { TimeRange, DistanceUnits } from '../common/model';
import { getEpochSeconds } from '../signal-detection/signal-detection-mock-backend';
import { toEpochSeconds, toOSDTime } from '../util/time-utils';
import { cloneDeep } from 'lodash';
import { stationProcessor } from '../station/station-processor';
import { getSecureRandomNumber } from '../util/common-utils';
import { FeaturePredictionStreamingInput, FeaturePrediction } from './model';
import { getNewRandomEventLocation, randomizeResiduals } from '../util/event-utils';
import { InstantMeasurementValue, FeatureMeasurementTypeName } from '../signal-detection/model';
import { performanceLogger } from '../log/performance-logger';
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const fs = require('file-system');

/**
 * Mock backend HTTP services providing access to processing station data. If mock services are enabled in the
 * configuration file, this module loads a test data set specified in the configuration file and configures
 * mock HTTP interfaces for the API gateway backend service calls.
 */

/**
 * Event Data store for holding the data read in by loadTestData
 */
interface EventDataStore {
    eventList: model.EventOSD[];
    featurePredictions: model.FeaturePredictionOSD [];
    idToStationMap: Map<string, string>;
}

let dataStore: EventDataStore = {
    eventList: [],
    featurePredictions: [],
    idToStationMap: new Map()
};

/**
 * Events by time input
 */
interface EventsByTimeInput {
    startTime: string;
    endTime: string;
    minLat?: number;
    maxLat?: number;
    minLong?: number;
    maxLong?: number;
}

/**
 * Get events by IDs input
 */
interface GetEventsByIdsInput {
    ids: string[];
}
let fpIndex = 0;
/**
 * Configure mock HTTP interfaces for a simulated set of QC mask backend services.
 * @param httpMockWrapper The HTTP mock wrapper used to configure mock backend service interfaces
 */
export function initialize(httpMockWrapper: HttpMockWrapper) {

    logger.info('Initializing mock backend for Event data');

    if (!httpMockWrapper) {
        throw new Error('Cannot initialize mock Event services with undefined HTTP mock wrapper');
    }

    // Load test data from the configured data set
    dataStore = loadTestData();

    // Load the Event backend service config settings
    const backendConfig = config.get('event.backend');

    // Override the OSD methods if in mock mode
    httpMockWrapper.onMock(backendConfig.services.getEventsByTimeAndLatLong.requestConfig.url,
                           getEventsByTimeAndLatLong);
    httpMockWrapper.onMock(backendConfig.services.getEventsByIds.requestConfig.url,
                           getEventsByIds);
    httpMockWrapper.onMock(backendConfig.services.computeFeaturePredictions.requestConfig.url,
                           computeFeaturePredictions);
    httpMockWrapper.onMock(backendConfig.services.locateEvent.requestConfig.url,
                           locateEvent);
}

/**
 * Load test data into the mock backend data store from the configured test data set.
 */
function loadTestData(): EventDataStore {

    // Get test data configuration settings
    const stdsDataConfig = config.get('testData.standardTestDataSet');
    const paths = resolveTestDataPaths();

    logger.info(`Read feature prediction file names from: ${paths.fpHome}`);
    const fpPaths = [];
    try {
        fs.recurseSync(paths.fpHome, ['*fp.json'], (filepath, relative, filename: string) => {
            if (filename) {
                fpPaths.push(paths.fpHome + path.sep + filename);
            }
        });
    } catch (e) {
        logger.error(`Could not read feature prediction file names from directory`);
    }

    logger.info(`Getting feature predictions from ${fpPaths.length} files`);
    // Load the Feature Predictions to populate the Location Solution
    // TODO: How to associate in Event???
    let featurePredictionSets: any[][] = [[]];
    const featurePredictionsString = 'featurePredictions';
    featurePredictionSets = fpPaths.map(file => {
        try {
            const raw = readJsonData(file);
            const fps = raw[featurePredictionsString];
            fps.forEach(fp => {
                const newId = uuid4().toString();
                dataStore.idToStationMap.set(newId, fp.channelID);
                fp.id = newId;
                delete fp.channelID;
            });

            const fpsReformatted: model.FeaturePredictionOSD[] = fps;
            return fpsReformatted;
        } catch (e) {
            logger.error(`Failed to read feature predictions from file: ${file}`);
            return [];
        }

    });

    const featurePredictions: model.FeaturePredictionOSD[] = [];
    // TODO when we have good data, actually iterate through and match id's to events
    featurePredictionSets.forEach(fps => fps.forEach(fp => featurePredictions.push(fp)));
    dataStore.featurePredictions = featurePredictions;

    // mock events and hypotheses
    let serializedEvents: any[] = [];

    // Read the necessary files into arrays of objects
    logger.info(`Loading Event test data from path: ${paths.jsonHome}`);
    try {
        serializedEvents = readJsonData(paths.jsonHome.concat(path.sep).concat(stdsDataConfig.events.eventsFileName));
    } catch (e) {
        logger.error(`Failed to read event from file: ${stdsDataConfig.events.eventsFileName}`);
    }
    serializedEvents.forEach(event => {
        dataStore.eventList.push(event);
    });

    return dataStore;
}

/**
 * Retrieve events that match provided IDs.
 * @param ids The IDs of the events to retrieve
 */
export async function getEventsByIds(input: GetEventsByIdsInput): Promise<model.EventOSD[]> {
    const ids = input.ids;
    const events = dataStore.eventList.filter(event => ids.indexOf(event.id) >= 0);
    return events;
}

/**
 * Gets events by a time and latitude and longitude
 * @param input events by time input
 * @returns Event OSD representation as a promise 
 */
export async function getEventsByTimeAndLatLong(input: EventsByTimeInput):
                                                Promise<model.EventOSD[]> {
    const timeRange: TimeRange = {
        startTime: getEpochSeconds(input.startTime),
        endTime: getEpochSeconds(input.endTime)
    };
    if (!timeRange) {
        logger.error('No time range given for event mock backend');
    }
    let eventsInRange: model.EventOSD[] = [];
    eventsInRange = dataStore.eventList.filter(event => {
        const eventTime = event.hypotheses[event.hypotheses.length - 1]
            .preferredLocationSolution.locationSolution.location.time;
        const eventTimeSec = toEpochSeconds(eventTime);
        return eventTimeSec >= timeRange.startTime && eventTimeSec < timeRange.endTime;
    });
    return eventsInRange;
}

/**
 * Locate event is a streaming call to COI to compute the location solution
 * @param input Event Hypothesis to use in the compute call (contains the EventHypothesis)
 * @returns OSD Map (not really a map) of LocationSolution[] 
 * (this should be a copy of the preferred ls with a new id)
 */
export async function locateEvent(input: any) {
    if (!input || !input.eventHypotheses || input.eventHypotheses.length === 0) {
        return { key: [] };
    }

    // Only support one event hypothesis for now
    const eventHypothesis: model.EventHypothesisOSD = input.eventHypotheses[0];
    const preferredLocationSolution: model.LocationSolutionOSD =
        eventHypothesis.preferredLocationSolution.locationSolution;

    // Create a location solution based on preferred LS passed. Give it a new uuid and return
    const ls = cloneDeep(preferredLocationSolution);
    ls.locationRestraint.depthRestraintType =
        input.parameters.eventLocationDefinition.locationRestraints[0].depthRestraintType;
    ls.id = uuid4().toString();

    const sdHypoIds = eventHypothesis.associations.map(assoc => assoc.signalDetectionHypothesisId);
    const location = getNewRandomEventLocation(sdHypoIds, toEpochSeconds(ls.location.time));
    ls.location = {
        ...location,
        time: toOSDTime(location.time)
    };
    ls.locationBehaviors = randomizeResiduals(ls.locationBehaviors);
    return {
        locSolutions: [ls]
    };
}

/**
 * Return the Feature Predictions in the LocationSolution found by how?
 * @param input streaming input to compute feature predictions
 * @return promise of location solution with updated feature predictions
 */
export async function computeFeaturePredictions(
    input: FeaturePredictionStreamingInput): Promise<model.LocationSolutionOSD> {

    performanceLogger.performance('computeFeaturePredictions', 'enteringService', `${input.sourceLocation.id}`);

    // Need to clone becuase interface being called multiple times with different phases
    const myInput = cloneDeep(input);
    const milliSecs = 1000;
    const eventArrivalTime = new Date(myInput.sourceLocation.location.time).valueOf() / milliSecs;

    const eventLocation = {
        latDegrees: myInput.sourceLocation.location.latitudeDegrees,
        lonDegrees: myInput.sourceLocation.location.longitudeDegrees,
        elevationKm: myInput.sourceLocation.location.depthKm,
        time: myInput.sourceLocation.location.time
    };
    // Create a unique entry for each call since there will be multiple async calls
    // made to the mock backend
    const fpList: any[] = [];

    const fps = findFp(eventLocation);
    // If we returned with multiple fps we found a matching event that we have data
    // for and we should use the 'real' mocked feature predictions
    const useMockedFpsForEvent = fps.length > 1;
    myInput.receiverLocations.forEach(channel => {
        const station = stationProcessor.getStationByChannelId(channel.id);
        // Set as any in order to change number to string to send to OSD

        const fpsForChannel: any = fps.filter(featurePrediction => {
            const stationName = dataStore.idToStationMap.get(featurePrediction.id);
            return stationName === station.name && featurePrediction.phase === myInput.phase;
        });
        const distanceToSource = stationProcessor.getDistanceToSource(
            eventLocation, DistanceUnits.degrees, stationProcessor.getStationByChannelId(channel.id));
        if (useMockedFpsForEvent) {
            if (fpsForChannel.length > 0) {
                fpsForChannel.forEach(fpForChannel => {
                    fpForChannel.channelId = channel.id;
                    fpForChannel.predictionType = fpForChannel.predictionType.featureMeasurementTypeName ?
                        fpForChannel.predictionType.featureMeasurementTypeName : fpForChannel.predictionType;
                    fpForChannel.predictedValue =
                        fpForChannel.predictionType === FeatureMeasurementTypeName.ARRIVAL_TIME ?
                    {
                        ...fpForChannel.predictedValue,
                        value: toOSDTime((fpForChannel.predictedValue as InstantMeasurementValue).value)
                    } :
                    {
                        measurementValue: fpForChannel.predictedValue.measurementValue ?
                            fpForChannel.predictedValue.measurementValue : fpForChannel.predictedValue,
                        referenceTime: toOSDTime(Date.now())
                    };
                    fpList.push(fpForChannel);
                });
                fpList.push(createMockAzimuthFeaturePrediction(channel.id, input.phase));
            }
        } else {
            // Create mocked feature prediction for ARRIVAL, SLOWNESS, AZIMUTH
            fpList.push(createMockArrivalFeaturePrediction(
                channel.id, input.phase, eventArrivalTime, distanceToSource));
            fpList.push(createMockSlownessFeaturePrediction(channel.id, input.phase));
            fpList.push(createMockAzimuthFeaturePrediction(channel.id, input.phase));
        }
    });

    // Set the FPs in the source location
    myInput.sourceLocation.featurePredictions = fpList;
    performanceLogger.performance('computeFeaturePredictions', 'returningFromService', `${input.sourceLocation.id}`);
    return myInput.sourceLocation;
}

/**
 * creates a Mock Azimuth Feature Prediction
 * 
 * @param channelId channel id as string
 * @param phaseTYpe phase type as string
 * 
 * @returns MockAzimuthFeaturePrediction
 */
function createMockAzimuthFeaturePrediction(channelId: string, phase: string): FeaturePrediction {
    const testDataConfig = config.get('testData.additionalTestData');
    const dataPath = resolveTestDataPaths().additionalDataHome;
    const azimuthFeaturePrediction =
        readJsonData(dataPath.concat(path.sep).concat(testDataConfig.featurePredictionAzimuth))[0];
    const threeSixty = 360;

    const mockAzimuthFeaturePrediction = {
        ...azimuthFeaturePrediction,
        id: uuid4().toString(),
        phase,
        channelId,
        predictedValue: {
            referenceTime: Date.now(),
            measurementValue: {
                ...azimuthFeaturePrediction.predictedValue.measurementValue,
                value: getSecureRandomNumber() * threeSixty
            }
        }
    };

    return mockAzimuthFeaturePrediction;
}

/**
 * creates a Mock Slowness Feature Prediction
 * 
 * @param channelId channel id as string
 * @param phaseTYpe phase type as string
 * 
 * @returns mockSlownessFeaturePrediction
 */
function createMockSlownessFeaturePrediction(channelId: string, phase: string): FeaturePrediction {
    const testDataConfig = config.get('testData.additionalTestData');
    const dataPath = resolveTestDataPaths().additionalDataHome;
    const slownessFeaturePrediction =
        readJsonData(dataPath.concat(path.sep).concat(testDataConfig.featurePredictionSlowness))[0];
    // Offset used to make  slowness more variable for mock data
    const randomOffset = 20;
    const mockSlownessFeaturePrediction = {
        ...slownessFeaturePrediction,
        id: uuid4().toString(),
        phase,
        channelId,
        predictedValue: {
            referenceTime: Date.now(),
            measurementValue: {
                ...slownessFeaturePrediction.predictedValue.measurementValue,
                value: getSecureRandomNumber() * randomOffset
            }
        }
    };

    return mockSlownessFeaturePrediction;
}

/**
 * creates a Mock Arrival Time Feature Prediction
 * 
 * @param channelId channel id as string
 * @param phaseTYpe phase type as string
 * 
 * @returns mockArrivalFeaturePrediction
 */
function createMockArrivalFeaturePrediction(channelId: string, phase: string, eventArrivalTime: number, dts: number)
: FeaturePrediction {
    const testDataConfig = config.get('testData.additionalTestData');
    const dataPath = resolveTestDataPaths().additionalDataHome;
    const arrivalFeaturePrediction =
        readJsonData(dataPath.concat(path.sep).concat(testDataConfig.featurePredictionArrival))[0];

    const randomSeconds = 300;
    const dtsMultiplier = 20;
    const randomTime = eventArrivalTime +
        (getSecureRandomNumber() * randomSeconds) + (dts * dtsMultiplier);

    const mockArrivalFeaturePrediction = {
        ...arrivalFeaturePrediction,
        id: uuid4().toString(),
        phase,
        channelId,
        predictedValue: {
            ...arrivalFeaturePrediction.predictedValue,
            value: toOSDTime(randomTime)
        }
    };

    return mockArrivalFeaturePrediction;
}

/**
 * Returns a next fp from the datastore
 */
function getNextFp(): model.FeaturePredictionOSD {
    fpIndex = (fpIndex + 1) % dataStore.featurePredictions.length;
    return dataStore.featurePredictions[fpIndex];
}

/**
 * Finds fps based on event time
 * @param eventLocation location to match on for loaded feature predictions
 */
function findFp(eventLocation): model.FeaturePredictionOSD[] {
    const eventTime = eventLocation.time.split('Z')[0];
    const fps = dataStore.featurePredictions.filter(fp => fp.sourceLocation.time.includes(eventTime));
    if (fps.length === 0) {
        return [getNextFp()];
    } else {
        return fps;
    }
}
