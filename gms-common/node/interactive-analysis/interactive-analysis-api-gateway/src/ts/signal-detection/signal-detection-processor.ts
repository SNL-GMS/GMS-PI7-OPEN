import * as config from 'config';
import * as model from './model';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { HttpClientWrapper } from '../util/http-wrapper';
import { cloneDeep, filter, find } from 'lodash';
import { TimeRange, CreationInfo,
         CreatorType, AssociationChange,
         DetectionAndAssociationChange } from '../common/model';
import * as uuid4 from 'uuid/v4';
import * as signalDetectionMockBackend from './signal-detection-mock-backend';
import { ChannelSegmentProcessor } from '../channel-segment/channel-segment-processor';
import { isWaveformChannelSegment } from '../channel-segment/model';
import { stationProcessor } from '../station/station-processor';
import { ProcessingStation } from '../station/model';
import { configProcessor } from '../config/config-processor';
import { eventProcessor } from '../event/event-processor';
import {
    findArrivalTimeFeatureMeasurementValue,
    findAzimthFeatureMeasurement,
    findPhaseFeatureMeasurement,
    findPhaseFeatureMeasurementValue,
    isNumericFeatureMeasurementValue,
    isAmplitudeFeatureMeasurementValue,
    findAmplitudeFeatureMeasurement,
    convertSDtoOSD
} from '../util/signal-detection-utils';
import { PhaseType } from '../channel-segment/model-spectra';
import { toEpochSeconds, getDurationTime } from '../util/time-utils';
import { Units } from '../event/model';
import { WaveformFilterProcessor } from '../waveform-filter/waveform-filter-processor';
import { performanceLogger } from '../log/performance-logger';

const MILLIS_SEC = 1000;
// Load addition SDs to account for SDs outside the time range
// but associated to events in the time range
logger.info('Initializing the Signal Detection Processor');

/**
 * The signal detection cache.
 */
interface SignalDetectionCache {
    signalDetections: model.SignalDetection[];
}
/**
 * Create a new FK, not associated with any signal detection hypothesis
 * @param input Input values used to compute a new FK dat set
 * @returns new FkData
 */

class SignalDetectionProcessor {
    /** the settings */
    private settings: any;

    /** the http wrapper */
    private httpWrapper: HttpClientWrapper;

    /**
     * the signal detection data cache
     */
    private dataCache: SignalDetectionCache = {
        signalDetections: [],
    };

    public constructor() {

        // Load configuration settings
        this.settings = config.get('signalDetection');

        // Initialize an http client
        this.httpWrapper = new HttpClientWrapper();
    }

    /**
     * Initialize the processor.
     */
    public initialize(): void {
        logger.info('Initializing the Signal Detection processor - Mock Enable: %s', this.settings.backend.mock.enable);

        // If service mocking is enabled, initialize the mock backend
        const backendConfig = config.get('signalDetection.backend');

        if (backendConfig.mock.enable) {
            signalDetectionMockBackend.initialize(this.httpWrapper.createHttpMockWrapper());
        }
    }

    /**
     * Retrieve the signal detection hypotheses associated with the provided list of station names, whose
     * arrival time feature measurements fall within the provided time range. Throws an error
     * if any of the hypotheses associated signal detections or arrival time measurements are missing.
     * @param stationIds The list of station IDs to find signal detections for
     * @param startTime The start of the time range for which to retrieve signal detections
     * @param endTime The end of the time range for which to retrieve signal detections
     */
    public async getSignalDetectionHypothesesByStation(stationIds: string[], timeRange: TimeRange):
        Promise<model.SignalDetectionHypothesis[]> {
        const detectionsByStation = await this.getSignalDetectionsByStation(stationIds, timeRange);
        return detectionsByStation.map(detection => detection.currentHypothesis);
    }

    /**
     * Retrieve the signal detection hypothesis for the provided ID.
     * @param hypothesisId the ID of the signal detection hypothesis to retrieve
     */
    public getSignalDetectionHypothesisById(hypothesisId: string): model.SignalDetectionHypothesis {
        const hyps = this.getSignalDetectionHypothesisListById([hypothesisId]);

        if (hyps && hyps.length > 0) {
            return hyps[0];
        }
        return undefined;
    }

    /**
     * Retrieve the signal detection hypotheses matching the provided IDs.
     * @param hypothesisIds the IDs of the signal detection hypotheses to retrieve
     */
    public getSignalDetectionHypothesisListById(hypothesisIds: string[]):
        model.SignalDetectionHypothesis[] {
        const hypsFound: model.SignalDetectionHypothesis[] = [];
        this.dataCache.signalDetections.forEach(sd => {
            const hyps = sd.signalDetectionHypotheses.filter(sdHyp => hypothesisIds.indexOf(sdHyp.id) >= 0);
            if (hyps && hyps.length > 0) {
                hypsFound.push(...hyps);
            }
        });
        return hypsFound;
    }

    /**
     * Retrieve the signal detections associated with the provided list of station IDs, whose
     * arrival time feature measurements fall within the provided time range. Throws an error
     * if any of the hypotheses associated signal detections or arrival time measurements are missing.
     * @param stationIds The station IDs to find signal detections for
     * @param startTime The start of the time range for which to retrieve signal detections
     * @param endTime The end of the time range for which to retrieve signal detections
     */
    public async getSignalDetectionsForDefaultStations(
        timeRange: TimeRange) {
            const stations: ProcessingStation[] = await stationProcessor.getDefaultStations();
            const stationIds: string[] = stations.map(station =>
                station.id);
            await this.loadSignalDetections(timeRange, stationIds);
    }

    /**
     * Retrieve the signal detections associated with the provided list of station IDs, whose
     * arrival time feature measurements fall within the provided time range. Throws an error
     * if any of the hypotheses associated signal detections or arrival time measurements are missing.
     * @param stationIds The station IDs to find signal detections for
     * @param startTime The start of the time range for which to retrieve signal detections
     * @param endTime The end of the time range for which to retrieve signal detections
     */
    public async getSignalDetectionsByStation(
        stationIds: string[], timeRange: TimeRange): Promise<model.SignalDetection[]> {
        performanceLogger.performance('signalDetectionsByStation', 'enteringResolver');
        // Check if cache has data for requested time range
        // if not, load data

        // await this.loadSignalDetections(timeRange, stationIds);
        const returnedSds = filter(this.dataCache.signalDetections, detection => {

            if (!detection || !detection.currentHypothesis) {
                return false;
            }
            // Retrieve the arrival time feature measurement from the current hypothesis
            const arrivalTimeMeasurementValue =
                findArrivalTimeFeatureMeasurementValue(detection.currentHypothesis.featureMeasurements);
            const arrivalTimeEpoch = arrivalTimeMeasurementValue.value;
            const matchedStation = stationIds.indexOf(detection.stationId) > -1;
            const startTimeMatch = arrivalTimeEpoch >= timeRange.startTime;
            const endTimeMatch = arrivalTimeEpoch <= timeRange.endTime;

            // Return true if the detection's station ID matches the input list
            // and the arrival time is in the input time range
            return matchedStation && startTimeMatch && endTimeMatch;
        });
        performanceLogger.performance('signalDetectionsByStation', 'leavingResolver');
        return returnedSds;
    }

    /**
     * Retrieve the signal detections matching the provided IDs.
     * @param detectionIds the IDs of the signal detections to retrieve
     */
    public async getSignalDetectionsById(detectionIds: string[]): Promise<model.SignalDetection[]> {
        const returning = filter(this.dataCache.signalDetections, detection => detectionIds.indexOf(detection.id) > -1);
        return returning;
    }

    /**
     * Retrieve the signal detection hypotheses matching the provided IDs.
     * @param hypothesisIds the IDs of the signal detection hypotheses to retrieve
     */
    public setSignalDetectionAssociationChanged(hypothesisId: string, modified: boolean) {
       const sd = this.getSignalDetectionByHypoId(hypothesisId);
       if (sd) {
           sd.associationModified = modified;
       }
    }

    /**
     * Retreive the Signal Detection using the Signal Detection Hypo's parent id
     * @param sdHypoId
     * 
     * @returns Signal Detection
     */
    public getSignalDetectionByHypoId(sdHypoId: string): model.SignalDetection {
        const sdHypo = this.getSignalDetectionHypothesisById(sdHypoId);
        if (sdHypo && sdHypo.parentSignalDetectionId) {
            return this.getSignalDetectionById(sdHypo.parentSignalDetectionId);
        }
        return undefined;
    }
    /**
     * Retrieve the signal detections matching the event ID using the SD Associations data structure.
     * @param eventId the ID of the 
     */
    public async getSignalDetectionsByEventId(eventId: string): Promise<model.SignalDetection[]> {
        // Try to retrieve the event with the provided ID; throw an error if it is missing
        const event = await eventProcessor.getEventById(eventId);
        if (!event) {
            throw new Error(`Failed to find event with ID ${eventId}`);
        }

        // Build a list of Signal Detections based on the sdAssocs
        const sdList: model.SignalDetection[] = [];
        event.hypotheses[event.hypotheses.length - 1].associations.forEach(sdAssociation => {
            const signalDetection = this.dataCache.signalDetections.find(sd => {
                if (sd.signalDetectionHypotheses.find(
                    sdh => sdh.id === sdAssociation.signalDetectionHypothesisId)) {
                    return true;
                }
            });
            if (signalDetection) {
                sdList.push(signalDetection);
            }
        });
        return sdList;
    }
    /**
     * Retreive the Signal Detection via the feature measurement if
     * @param featureMeasurementId
     * 
     * @returns Signal Detection
     */
    public getSignalDetectionByFmId(featureMeasurementId: string): model.SignalDetection {
        const maybeSd = this.dataCache.signalDetections.find(sd => {
            if (sd.currentHypothesis) {
                return sd.currentHypothesis.featureMeasurements
                       .find(fm => fm.id === featureMeasurementId) !== undefined;
            } else {
                logger.warn('No current hyp');
                return false;
            }
        });
        return maybeSd;
    }
    /**
     * Creates a new signal detection with an initially hypothesis and time
     * feature measurement based on the provided input.
     * @param input The input parameters used to create the new detection
     */
    public createDetection(input: model.NewDetectionInput): model.SignalDetection {
        const detectionId = uuid4().toString();
        const hypothesisId = uuid4().toString();

        // Todo call CreationInfo component for new CreationInfo
        const creationInfo = this.getCreationInfo(uuid4().toString());

        // Create a new signal detection hypothesis
        const phase: PhaseType = PhaseType[input.phase];
        const newHypothesis: model.SignalDetectionHypothesis = {
            id: hypothesisId,
            rejected: false,
            parentSignalDetectionId: detectionId,
            creationInfoId: creationInfo.id,
            featureMeasurements: [
                this.createFeatureMeasurement(
                    model.FeatureMeasurementTypeName.ARRIVAL_TIME,
                    {
                        value: input.signalDetectionTiming.arrivalTime,
                        standardDeviation: 0
                    },
                    undefined, phase, creationInfo.id),
                this.createFeatureMeasurement(
                    model.FeatureMeasurementTypeName.RECEIVER_TO_SOURCE_AZIMUTH,
                    {
                        referenceTime: input.signalDetectionTiming.arrivalTime,
                        measurementValue: {
                            value: 21.73,
                            standardDeviation: 0.31,
                            units:  Units.SECONDS_PER_DEGREE
                        }
                    },
                    undefined, phase, creationInfo.id),
                this.createFeatureMeasurement(
                    model.FeatureMeasurementTypeName.SLOWNESS,
                    {
                        referenceTime: input.signalDetectionTiming.arrivalTime,
                        measurementValue: {
                            value: 21.73,
                            standardDeviation: 0.31,
                            units:  Units.SECONDS_PER_DEGREE
                        }
                    },
                    undefined, phase, creationInfo.id),
                this.createFeatureMeasurement(
                    model.FeatureMeasurementTypeName.PHASE,
                    {
                        phase,
                        confidence: 1
                    },
                    undefined, phase, creationInfo.id),
            ]
        };

        // only add create the amplitude measurement if values were provided
        if (input.signalDetectionTiming.amplitudeMeasurement) {
            newHypothesis.featureMeasurements.push(this.createFeatureMeasurement(
                model.FeatureMeasurementTypeName.AMPLITUDE_A5_OVER_2,
                {
                    startTime: input.signalDetectionTiming.amplitudeMeasurement.startTime,
                    period: input.signalDetectionTiming.amplitudeMeasurement.period,
                    amplitude: input.signalDetectionTiming.amplitudeMeasurement.amplitude
                },
                undefined, phase, creationInfo.id));
        }

        // Create a new signal detection
        const newDetection: model.SignalDetection = {
            id: detectionId,
            monitoringOrganization: 'CTBTO',
            stationId: input.stationId,
            signalDetectionHypotheses: [newHypothesis],
            currentHypothesis: newHypothesis,
            modified: true,
            associationModified: false,
            creationInfoId: creationInfo.id
        };

        // Add the new detection to the list
        this.dataCache.signalDetections.push(newDetection);
        return newDetection;
    }

    /**
     * Updates the signal detection with the provided unique ID if it exists, using
     * the provided UpdateDetectionInput object. This function creates a new signal detection hypothesis
     * and sets it to the 'current', reflecting the change.
     * This function throws an error if no signal detection hypothesis exists for the provided ID.
     * @param hypothesisId The unique ID identifying the signal detection hypothesis to update
     * @param input The UpdateDetectionInput object containing fields to update in the hypothesis
     */
    public updateDetection(detectionId: string,
                           input: model.UpdateDetectionInput): DetectionAndAssociationChange {

        const detection = find(this.dataCache.signalDetections, { id: detectionId });
        let changes: AssociationChange = {
            events: [],
            sds: []
        };
        // Throw an error if no detection exists for the provided ID
        if (!detection) {
            throw new Error(`Couldn't find Signal Detection with ID ${detectionId}`);
        }

        if (!input.phase && !input.signalDetectionTiming) {
            throw new Error(`No valid input provided to update detection with ID: ${detectionId}`);
        }
        if (!detection.modified) {
            // updates and returns the modified event and sd
            changes = this.createNewHypothesis(detection, input.phase, input.signalDetectionTiming);
        } else {
            // update the current hypothesis
            this.updateCurrentHypothesis(detection, input.phase, input.signalDetectionTiming);
            changes.sds.push(detection);
        }
        detection.modified = true;
        return {
            detections: [detection],
            associationChange: changes
        };
    }

    /**
     * Updates the collection of signal detections matching the provided list of unique IDs
     * using the provided UpdateDetectionInput object. A new hypothesis is created for each detection,
     * reflecting the updated content.
     * This function throws an error if no signal detection hypothesis exists for any of the provided IDs.
     * @param hypothesisIds The list of unique IDs identifying the signal detection hypothesis to update
     * @param input The UpdateDetectionInput object containing fields to update in the hypothesis
     */
    public updateDetections(detectionIds: string[],
                            input: model.UpdateDetectionInput): DetectionAndAssociationChange {
        const updatedDetections = [];
        const changes: AssociationChange = {
            events: [],
            sds: []
        };
        detectionIds.forEach(detectionId => {
            const detectionSpecificChange = this.updateDetection(detectionId, input);
            updatedDetections.push(...detectionSpecificChange.detections);
            changes.events.push(...detectionSpecificChange.associationChange.events);
            changes.sds.push(...detectionSpecificChange.associationChange.sds);
        });
        return {
            detections: updatedDetections,
            associationChange: changes
        };
    }

    /**
     * Rejects the collection of signal detections matching the provided list of unique IDs
     * This function throws an error if no signal detection exists for any of the provided IDs.
     * @param detectionIds The list of unique IDs identifying the signal detections to update
     * @param input The UpdateDetectionIrejectedt containing fields to update in the detections
     */
    public rejectDetections(detectionIds: string[]): AssociationChange {
        const detections = [];
        const eventsChanged = [];
        detectionIds.forEach(detectionId => {
            const detection = find(this.dataCache.signalDetections, { id: detectionId });

            detection.currentHypothesis.rejected = true;
            detection.modified = true;
            eventsChanged.push(...eventProcessor.rejectAssociationForSDHyp(detection.currentHypothesis.id));
            detections.push(detection);
        });
        return {
            sds: detections,
            events: eventsChanged
        };
    }

    /**
     * Retrieve the signal detection for the provided ID.
     * @param detectionId the ID of the signal detection to retrieve
     */
    public getSignalDetectionById(detectionId: string): model.SignalDetection {
        return find(this.dataCache.signalDetections, { id: detectionId });
    }

    /**
     * Loads signal detections.
     * 
     * @param timeRange the time range
     * @param stationIds the station ids
     */
    public async loadSignalDetections(timeRange: TimeRange, stationIds: string[]) {
        if (!timeRange) {
            throw new Error('Unabled to retrieve Signal Detections for undefined time range');
        }

        const requestConfig = this.settings.backend.services.sdsByStation.requestConfig;
        // TODO Check cache then call backend if not found
        const endTimePadding: number = configProcessor.getConfigByKey('extraLoadingTime');
        const query = {
            stationIds,
            startTime: new Date(timeRange.startTime * MILLIS_SEC).toISOString(),
            endTime: new Date((timeRange.endTime + endTimePadding) * MILLIS_SEC).toISOString()
        };
        // await eventProcessor.getEventsInTimeRange(timeRange);
        // tslint:disable-next-line: max-line-length
        logger.debug(`Signal Detections sending service request: ${JSON.stringify(requestConfig, undefined, 2)} query: ${JSON.stringify(query, undefined, 2)}`);
        const responseData = await this.httpWrapper.request(requestConfig, query);
        if (responseData) {

            // unwrap the response
            const signalDetectionsReturned: model.SignalDetection[] = [];
            // tslint:disable-next-line:forin
            for (const key in responseData) {
                const sds = responseData[key];
                if (sds) {
                    sds.forEach(sd => signalDetectionsReturned.push(sd));
                }
            }
            const signalDetectionPromises = signalDetectionsReturned.map(
                async sd => await this.hydrateSignalDetections(sd));
            const maybeSds = await Promise.all(signalDetectionPromises);
            maybeSds.forEach(maybeSd => {
                if (maybeSd !== undefined) {
                    // Lookup index sd index if -1 then add
                    const index = this.dataCache.signalDetections.findIndex(dsd => dsd.id === maybeSd.id);
                    if (index > -1) {
                        this.dataCache.signalDetections[index] = maybeSd;
                    } else {
                        this.dataCache.signalDetections.push(maybeSd);
                    }
                }
            });
        }
        return this.dataCache.signalDetections;
    }

    /**
     * Calls the Filter Waveform streaming service to calculate Filtered versions of the Arrival Time beam
     * @param atfm 
     */
    public async populateFilterBeams(atfm: model.FeatureMeasurement): Promise<model.FeatureMeasurement[]> {
        const beamChannelSegment = await ChannelSegmentProcessor.Instance().getChannelSegment(atfm.channelSegmentId);
        const filteredFMs: model.FeatureMeasurement[] = [];
        if (!beamChannelSegment) {
            return [];
        } else if (isWaveformChannelSegment(beamChannelSegment)) {
            const filteredCS = await WaveformFilterProcessor.Instance()
                .getFilteredWaveformSegments([beamChannelSegment]);
            filteredCS.forEach(cs => {
                const fm = this.createFeatureMeasurement(
                    model.FeatureMeasurementTypeName.FILTERED_BEAM,
                    {
                        strValue: cs.wfFilterId,
                    },
                    cs.id, PhaseType.P, uuid4().toString());
                filteredFMs.push(fm);
            });
        }
        return filteredFMs;
    }

    /**
     * Clears the modified lists and restores hypothesis to last hypothesis
     */
    public clearModifiedSignalDetections(): AssociationChange {
        const sdsToClear = [];
        const changes: AssociationChange = {
            events: [],
            sds: []
        };
        this.dataCache.signalDetections.forEach(sd => {
            if (sd.modified) {
                sd.modified = false;
                // undo the changes
                const unsavedCurrent = sd.signalDetectionHypotheses.pop();
                sd.currentHypothesis =
                    sd.signalDetectionHypotheses[(sd.signalDetectionHypotheses.length - 1)];
                sdsToClear.push(sd);

                // Call the event process to update the event hypo to SD association
                changes.events.push(
                    ...eventProcessor.updateSignalDetectionAssociation(
                        unsavedCurrent.id, sd.currentHypothesis.id));
            }
        });
        changes.sds.push(...sdsToClear);
        return changes;
    }

    /**
     * Saves all signal detections for an activityID to the backend/osd and resets the modifed bit
     */
    public async saveSignalDetections(): Promise<model.SignalDetection[]> {
        const sdsToSaveConst: model.SignalDetection[] = [];
        this.dataCache.signalDetections.forEach(sd => {
            if (sd.modified) {
                sd.modified = false;
                sdsToSaveConst.push(sd);
            }
        });

        //  TODO: Convert to OSD format
        const requestConfig = this.settings.backend.services.saveSds.requestConfig;
        const sdsToSave = [];
        sdsToSaveConst.forEach((sd: any) => {
            sdsToSave.push(convertSDtoOSD(sd));
        });

        // If service mocking is enabled, initialize the mock backend
        const backendConfig = config.get('signalDetection.backend');
        if (sdsToSave && sdsToSave.length > 0 && backendConfig.mock.enable) {
            logger.debug('Saving sds');
            // tslint:disable-next-line:max-line-length
            logger.debug(`Sending service request: ${JSON.stringify(requestConfig, undefined, 2)} query: ${JSON.stringify(sdsToSave, undefined, 2)}`);
            await this.httpWrapper.request(requestConfig, JSON.stringify(sdsToSave));
        }

        return sdsToSaveConst;
    }

    /**
     * Creates a new CreationInfo later will be own component to create/retrieve from OSD
     */
    public getCreationInfo(creationInfoId: string): CreationInfo {
        if (!creationInfoId) {
            creationInfoId =  uuid4().toString();
        } else if (typeof(creationInfoId) === 'object') {
            // !TODO for some reason creationInfoId sometimes is passed as an object
            creationInfoId = ((creationInfoId as CreationInfo).id) ?
                (creationInfoId as CreationInfo).id : uuid4().toString();
        }
        const creationInfo: CreationInfo = {
            id: creationInfoId,
            creationTime: Date.now() / MILLIS_SEC,
            creatorType: CreatorType.Analyst,
            creatorId: 'creatorId',
            creatorName: 'Matthew Carrasco'
        };
        return creationInfo;
    }

    /**
     * Resolves Hyp Ids to SDs based on association changes from the
     * event processor
     * @param sdHypIds hyp ids to resolve
     */
    public getSdsToPublish(sdHypIds: string[]): model.SignalDetection[] {
        return sdHypIds.map(hypId => this.getSignalDetectionByHypoId(hypId));
    }
    /**
     * Hydrates a feature measurement
     * @param fm feature measurement to hydrate
     * @param definingRules defining rules based on pahse
     * @returns a Feature measurement[] as a promise
     */
    private async hydrateFeatureMeasurements(fm: model.FeatureMeasurement, definingRules: [model.DefiningRule]):
    Promise<model.FeatureMeasurement[]> {
        fm.definingRules = definingRules;
        // TODO: check if/how beams are being loaded
        if (fm.featureMeasurementType ===
            model.FeatureMeasurementTypeName.ARRIVAL_TIME) {
            const arrivalTimeString: any =
                (fm.measurementValue as model.InstantMeasurementValue).value;
            (fm.measurementValue as model.InstantMeasurementValue).value =
                toEpochSeconds(arrivalTimeString);
            // Convert Standard Deviation time duration to seconds
            const arrivalSDString: any = (fm.measurementValue as
                model.InstantMeasurementValue).standardDeviation;
            (fm.measurementValue as model.InstantMeasurementValue).
                standardDeviation = getDurationTime(arrivalSDString);

            // Populate the Fk_Beam and Filter Fk_Beams in the channel segment processor cache
            const filteredFms = await this.populateFilterBeams(fm);
            return [...filteredFms, fm];
        } else if (isNumericFeatureMeasurementValue(fm.measurementValue)) {
                const referenceTime: any = fm.measurementValue.referenceTime;
                const epochSeconds = toEpochSeconds(referenceTime);
                const newFMValue = {
                    ...fm.measurementValue,
                    referenceTime: epochSeconds
                };
                return [{
                    ...fm,
                    measurementValue: newFMValue
                }];
        } else if (isAmplitudeFeatureMeasurementValue(fm.measurementValue)) {
            const startTime: any = fm.measurementValue.startTime;
            const epochSeconds = toEpochSeconds(startTime);
            const newFMValue = {
                ...fm.measurementValue,
                startTime: epochSeconds,
                period: getDurationTime(fm.measurementValue.period)
            };
            return [{
                ...fm,
                measurementValue: newFMValue
            }];
        } else {
            return [fm];
        }
    }

    /**
     * Hydrates sd hypothesiss with feature measurements
     * @param sdHypo hypothesis to hydrate
     * 
     */
    private async hydrateSignalDetectionHypothesis(sdHypo: model.SignalDetectionHypothesis):
    Promise<model.SignalDetectionHypothesis> {
        const phaseFeatureMeasurement =
        findPhaseFeatureMeasurement(sdHypo.featureMeasurements);
        if (phaseFeatureMeasurement) {
            const realMeasurementValue: any = phaseFeatureMeasurement.measurementValue;
            const phaseFeatureMeasurementValue: model.PhaseTypeMeasurementValue = {
                phase: realMeasurementValue.value, // swap to `phase` from `value`
                confidence: realMeasurementValue.confidence
            };
            phaseFeatureMeasurement.measurementValue = phaseFeatureMeasurementValue;
        } else {
            logger.warn('No phase measurement found. Setting phase to N');
        }
        // Retrieve the each feature measurement and add defining rule
        const phase: PhaseType = findPhaseFeatureMeasurementValue(sdHypo.featureMeasurements).phase;
        const definingRules: [model.DefiningRule] = [{
            operationType: model.DefiningOperationType.Location,
            isDefining: phase ? phase.toString().startsWith('P') : false
        }];
        sdHypo.rejected = false;
        const newFeatureMeasurementArraysPromises =
            sdHypo.featureMeasurements.map(async fm => await this.hydrateFeatureMeasurements(fm, definingRules));
        const featureMeasurementsArrays = await Promise.all(newFeatureMeasurementArraysPromises);
        const featureMeasurements = featureMeasurementsArrays.reduce(
            (prev: model.FeatureMeasurement[], curr: model.FeatureMeasurement[]) =>
                [...prev, ...curr],
            [] // Initial empty value of prev for first curr
        );
        sdHypo.featureMeasurements = featureMeasurements;
        return sdHypo;
    }
    /**
     * Hydrates sd hypothesis
     * @param sd Sd to hydrate
     */
    private async hydrateSignalDetections(sd: model.SignalDetection):
    Promise<model.SignalDetection> {
        try {
            // If the Signal Detection came from OSD then it is not modified
            // only local SD are modified by the Analyst
            sd.modified = false;
            if (sd.signalDetectionHypotheses && sd.signalDetectionHypotheses.length > 0) {
                // First cycle through the sdHypo feature measurements adding the azmith and slowness
                // defining rules (eventually OSD should support these)
                const sdhPromises = sd.signalDetectionHypotheses.map(
                    async sdh => await this.hydrateSignalDetectionHypothesis(sdh));
                const sdhArray = await Promise.all(sdhPromises);
                sd.signalDetectionHypotheses = sdhArray;
                // Set the last SD Hypo to current hypo
                sd.currentHypothesis = sd.signalDetectionHypotheses[sd.signalDetectionHypotheses.length - 1];
            }
            if (sd.currentHypothesis) {
                // Populate the fk spectra from the channel segment
                const azimthFeatureMeasurement = findAzimthFeatureMeasurement(
                    sd.currentHypothesis.featureMeasurements);
                if (azimthFeatureMeasurement && azimthFeatureMeasurement.channelSegmentId) {
                    // Retrieve the FK Spectra timeseries. The channel segment processor will cache after
                    // OSD call
                    await ChannelSegmentProcessor.Instance().
                        getChannelSegment(azimthFeatureMeasurement.channelSegmentId, sd);
                }
            }
            return sd;

        } catch (e) {
            logger.error(`Error adding SD ID: ${sd.id} to data cache`);
            logger.error(e);
            logger.debug(`SD returned from OSD: ${JSON.stringify(sd, undefined, 2)}`);
            return undefined;
        }
    }
    /**
     * Update Signal Detection's current Hypothesis time and or phase
     * @param detection Signal Detection to update
     * @param phase If phase is set update current SD Hypothesis
     * @param time If time is set update current SD Hypothesis
     */
    private updateCurrentHypothesis(detection: model.SignalDetection,
                                    phase?: string, sdTiming?: model.SignalDetectionTimingInput) {
        const currentHypo = detection.currentHypothesis;
        const phaseFeatureMeasurementValue = findPhaseFeatureMeasurementValue(currentHypo.featureMeasurements);
        const phaseType = phase ? PhaseType[phase] : phaseFeatureMeasurementValue.phase;
        phaseFeatureMeasurementValue.phase = phaseType;

        const creation = this.getCreationInfo(uuid4().toString());

        // TODO Refactor and reuse the create and update logic
        // If SignalDetectionTiming is set then update arrival time and amplitude FMV
        // Update arrival time
        if (sdTiming) {
            const arrivalTimeFeatureMeasurementValue = findArrivalTimeFeatureMeasurementValue(
                currentHypo.featureMeasurements);
            arrivalTimeFeatureMeasurementValue.value =
                sdTiming.arrivalTime ? sdTiming.arrivalTime : arrivalTimeFeatureMeasurementValue.value;

            // Update amplitude measurement value
            if (sdTiming.amplitudeMeasurement) {
                const amplitudeFeatureMeasurement = findAmplitudeFeatureMeasurement(
                    currentHypo.featureMeasurements);
                if (amplitudeFeatureMeasurement) {
                    amplitudeFeatureMeasurement.measurementValue = sdTiming.amplitudeMeasurement;
                } else {
                    currentHypo.featureMeasurements.push(this.createFeatureMeasurement(
                        model.FeatureMeasurementTypeName.AMPLITUDE_A5_OVER_2,
                        sdTiming.amplitudeMeasurement,
                        undefined, phaseType, creation.id));
                }
            }
        }
    }

    /**
     * Create a new Signal Detection Hypothesis and set current SD Hypothesis to it.
     * @param detection Signal Detection to update
     * @param phase If phase is set update current SD Hypothesis
     * @param time If time is set update current SD Hypothesis
     */
    private createNewHypothesis(detection: model.SignalDetection,
                                phase?: string, sdTiming?: model.SignalDetectionTimingInput): AssociationChange {
        const newId = uuid4().toString();
        const creation = this.getCreationInfo(uuid4().toString());
        const fkDataId = detection.currentHypothesis.fkDataId;
        const phaseType = phase ? PhaseType[phase] : PhaseType.P;

        const newHypothesis: model.SignalDetectionHypothesis = {
            id: newId,
            rejected: false,
            parentSignalDetectionId: detection.id,
            creationInfoId: creation.id,
            fkDataId,
            featureMeasurements: cloneDeep(detection.currentHypothesis.featureMeasurements)
        };

        // Now check if new SDHypo should be updated with phase and/or arrival time with Amplitude FM
        if (sdTiming) {
            const arrivalTimeFeatureMeasurementValue = findArrivalTimeFeatureMeasurementValue(
                newHypothesis.featureMeasurements);
            if (arrivalTimeFeatureMeasurementValue) {
                arrivalTimeFeatureMeasurementValue.value = sdTiming.arrivalTime;
            } else {
                newHypothesis.featureMeasurements.push(this.createFeatureMeasurement(
                    model.FeatureMeasurementTypeName.ARRIVAL_TIME,
                    {
                        value: sdTiming.arrivalTime,
                        standardDeviation: 0
                    },
                    undefined, phaseType, creation.id));
            }

            // update the amplitude measurement if one was provided
            if (sdTiming.amplitudeMeasurement) {
                // If there is already an Amplitude Feature Measurement Value replace it with new one
                // else make one
                const amplitudeFeatureMeasurement = findAmplitudeFeatureMeasurement(
                    newHypothesis.featureMeasurements);
                if (amplitudeFeatureMeasurement) {
                    amplitudeFeatureMeasurement.measurementValue = sdTiming.amplitudeMeasurement;
                } else {
                    newHypothesis.featureMeasurements.push(this.createFeatureMeasurement(
                        model.FeatureMeasurementTypeName.AMPLITUDE_A5_OVER_2,
                        sdTiming.amplitudeMeasurement,
                        undefined, phaseType, creation.id));
                }
            }
        }

        if (phase) {
            const phaseFeatureMeasurement = findPhaseFeatureMeasurementValue(newHypothesis.featureMeasurements);
            if (phaseFeatureMeasurement) {
                phaseFeatureMeasurement.phase = PhaseType[phase];
            } else {
                newHypothesis.featureMeasurements.push(this.createFeatureMeasurement(
                    model.FeatureMeasurementTypeName.PHASE,
                    {
                        phase,
                        confidence: 1
                    },
                    undefined, phaseType, creation.id));
            }
        }

        const events = eventProcessor.updateSignalDetectionAssociation(
                                       newHypothesis.id, detection.currentHypothesis.id);
        detection.signalDetectionHypotheses.push(newHypothesis);
        detection.currentHypothesis = newHypothesis;
        return {
            events: [...events],
            sds: [detection]
        };
    }

    /**
     * Helper function to create a new Azimuth FeatureType
     */
    private createFeatureMeasurement<T extends model.FeatureMeasurementValue>(
            featureType: model.FeatureMeasurementTypeName, measurementValue: T,
            channelSegmentId: string, phase: PhaseType, creationInfoId):
        model.FeatureMeasurement {
        const uuid = uuid4().toString();
        if (!channelSegmentId) {
            channelSegmentId = uuid;
        }
        return {
            id: uuid,
            channelSegmentId,
            featureMeasurementType: featureType,
            measurementValue,
            creationInfoId,
            uncertainty: 0,
            calculationTime: '2018-06-28T20:00:00Z',
            definingRules: [
                {
                    operationType: model.DefiningOperationType.Location,
                    isDefining: phase ? phase.toString().startsWith('P') : false
                }
            ],
        };

    }
}
export const signalDetectionProcessor: SignalDetectionProcessor = new SignalDetectionProcessor();
signalDetectionProcessor.initialize();
