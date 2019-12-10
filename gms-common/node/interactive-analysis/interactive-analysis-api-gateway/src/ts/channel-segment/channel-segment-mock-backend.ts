import * as path from 'path';
import { HttpMockWrapper } from '../util/http-wrapper';
import { gatewayLogger as logger } from '../log/gateway-logger';
import {
    ChannelSegmentInput, ChannelSegmentByIdInput,
    OSDChannelSegment, OSDTimeSeries, isFkSpectraChannelSegmentOSD, ChannelSegmentType, BeamFormingInput
} from './model';
import * as config from 'config';
import { readJsonData, resolveTestDataPaths } from '../util/file-parse';
import { toEpochSeconds } from '../util/time-utils';
import { getWaveformSegmentsByChannelSegments } from '../waveform/waveform-mock-backend';
import { isMockWaveformChannelSegment, MockWaveform, WaveformFileInfo, OSDWaveform } from '../waveform/model';
import { ComputeFkInput, FkPowerSpectraOSD, FkPowerSpectrumOSD } from './model-spectra';
import { CreatorType, OSDCreationInfo } from '../common/model';
import * as uuid4 from 'uuid/v4';
import { getChannelById } from '../station/station-mock-backend';
import { cloneDeep } from 'lodash';
import { performanceLogger } from '../log/performance-logger';
import { DerivedFilterChannelSegmentId } from '../waveform-filter/model';
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const fs = require('file-system');

/**
 * The datastore for channel segment data where channel segments
 * is the complete list, and fkPowerSpectraSegments are fk and the beam
 */
interface ChannelSegmentDataStore {
    channelSegments: OSDChannelSegment<OSDTimeSeries>[];
    fkPowerSpectraSegments: OSDChannelSegment<FkPowerSpectraOSD>[];
    beamChannelSegmentsIds: string[];
    currentFkIdx: number;
    currentBeamIdx: number;
    filterMappings: FilterMapping[];
}

/**
 * Filter mapping interface for mapping filter id to filter name
 */
interface FilterMapping {
    filterId: string;
    filterName: string;
}
let isInitialized = false;
let dataStore: ChannelSegmentDataStore;
let channelSegmentConfig: any;
const fkSegmentIdToPathMap: Map<string, string> = new Map<string, string>();

/**
 * Initialize for the Channel Segment mock processor that sets the mock enable on 
 * the service calls, and intercepts them with internal function calls.
 * @param httpMockWrapper axios mock wrapper 
 */
export function initialize(httpMockWrapper: HttpMockWrapper): void {

    // If already initialized...
    if (isInitialized) {
        return;
    }

    logger.info('Initializing mock backend for channel segment data');

    if (!httpMockWrapper) {
        throw new Error('Cannot initialize mock channel segment services with undefined HTTP mock wrapper');
    }

    // Load test data from the configured data set
    dataStore = loadTestData();

    // Load the channel segment backend service config settings
    channelSegmentConfig = config.get('channel-segment');
    httpMockWrapper.onMock(
        channelSegmentConfig.backend.services.computeFk.requestConfig.url,
        computeFk
    );
    httpMockWrapper.onMock(
        channelSegmentConfig.backend.services.channelSegments.requestConfig.url,
        determineMethod
    );
    httpMockWrapper.onMock(
        channelSegmentConfig.backend.services.computeBeam.requestConfig.url,
        computeBeam
    );

    // Set flag
    isInitialized = true;
}

/**
 * Reads in and sets the test data used for mocking
 */
function loadTestData(): ChannelSegmentDataStore {
    logger.info('Initializing the channel segment service');
    const paths = resolveTestDataPaths();

    // STDS test data path (not part of the deployment this is where the large files exist)
    const testDataSTDSConfig = config.get('testData.standardTestDataSet');
    const testDataSTDSConfigFkSpectraDefinition: string = testDataSTDSConfig.fk.fkSpectraDefinition;

    const chanSegIdWFilename = testDataSTDSConfig.channelSegment.channelSegmentIdToW;
    const chanSegFullPath = paths.channelsHome.concat(path.sep).concat(chanSegIdWFilename);

    logger.info(`Standard test data home ${paths.dataHome}`);
    logger.info(`Loading channel segments id file from ${chanSegFullPath}`);

    let chanSetIdWEntries = [];
    try {
        chanSetIdWEntries = readJsonData(chanSegFullPath);
    } catch (e) {
        logger.error(`Failed to read chan seg to w from file: ${chanSegIdWFilename}`);
    }

    // Build the returning structure of channelSegmentId: {mockChanSegFileInfo} map
    const chanSegFileInfoMap: Map<string, WaveformFileInfo> = new Map();
    Object.keys(chanSetIdWEntries).forEach((key: string) => {
        const value = chanSetIdWEntries[key];
        chanSegFileInfoMap.set(key, value);
    });

    const channelSegDataPaths = [];
    try {
        fs.recurseSync(paths.channelsHome, ['segment*.json'], (filepath, relative, filename: string) => {
            if (filename) {
                channelSegDataPaths.push(paths.channelsHome + path.sep + filename);
            }
        });
    } catch (e) {
        logger.error(`Directory not found: ${paths.channelsHome}`);
    }

    const channelSegments: OSDChannelSegment<MockWaveform>[] = [];

    // Need parallel fkBeam Channel Segment list to rotate thrue to support computeBeam
    const beamChannelSegmentsIds: string[] = [];

    channelSegDataPaths.forEach(file => {
        const channelSegmentEntries: OSDChannelSegment<MockWaveform>[] = readJsonData(file);
        channelSegmentEntries.forEach(cs => {
            const fileInfo = chanSegFileInfoMap.get(cs.id);
            cs.timeseries.forEach(ts => {
                ts.waveformFile = fileInfo.waveformFile;
                ts.fOff = fileInfo.fOff;
                ts.dataType = fileInfo.dataType;
                ts.sampleCount = fileInfo.sampleCount;
            });
            channelSegments.push(cs);
            if (cs.type === ChannelSegmentType.FK_BEAM || cs.type === ChannelSegmentType.DETECTION_BEAM) {
                beamChannelSegmentsIds.push(cs.id);
            }
        });
    });

    logger.info(`Loading Fk spectrum from ${paths.fkHome}`);
    const fkPaths = [];
    try {
        fs.recurseSync(paths.fkHome, ['*.ChanSeg'], (filepath, relative, filename: string) => {
            if (filename) {
                fkPaths.push(paths.fkHome + path.sep + filename);
            }
        });
    } catch (e) {
        logger.error(`Failed to read fk paths from ${paths.fkHome}`);
    }

    // Lookup the Fk Definition path
    const fkDefinitionPath = paths.dataHome + path.sep + testDataSTDSConfigFkSpectraDefinition;
    const fkSpectraDefinitionString = 'FkSpectraDefinition';
    let fkSpectraDefinitions = [];
    try {
        fkSpectraDefinitions = readJsonData(fkDefinitionPath)[fkSpectraDefinitionString];
    } catch (e) {
        logger.error(`Failed to read fk spectra definitions`);
    }
    let stdsFkDataList: OSDChannelSegment<FkPowerSpectraOSD>[][] = [[]];
    stdsFkDataList = fkPaths.map(file => {
        try {
            const data: any = readJsonData(file);
            const fkSegments: OSDChannelSegment<FkPowerSpectraOSD>[]
            = data.channelSegments.map(seg => {
                fkSegmentIdToPathMap.set(seg.id, file);
                const creationInfo: OSDCreationInfo = {
                    creationTime: seg.creationInfo.creationTime,
                    id: uuid4().toString(),
                    creatorId: uuid4().toString(),
                    creatorType: CreatorType.System,
                    creatorName: seg.creationInfo.creatorName,
                    softwareInfo: seg.creationInfo.softwareInfo
                };
                const fkDefinition = fkSpectraDefinitions.find(def => def.channelId === seg.channelId);
                const fkSpectra: OSDChannelSegment<FkPowerSpectraOSD> = {
                    id: seg.id,
                    channelId: seg.channelId,
                    name: seg.name,
                    type: seg.type,
                    timeseriesType: seg.timeseriesType,
                    startTime: seg.timeseries[0].startTime,
                    endTime:
                        '',
                        timeseries: seg.timeseries.map(series =>
                            ({
                            startTime: seg.timeseries[0].startTime,
                            sampleRate: series.sampleRate,
                            sampleCount: series.sampleCount,
                            windowLead: fkDefinition.windowLead,
                            windowLength: fkDefinition.windowLength,
                            phaseType: fkDefinition.phaseType,
                            lowFrequency: fkDefinition.lowFrequency,
                            highFrequency: fkDefinition.highFrequency,
                            xSlowStart: fkDefinition.slowStartX,
                            xSlowCount: fkDefinition.slowCountX,
                            xSlowDelta: fkDefinition.slowDeltaX,
                            ySlowStart: fkDefinition.slowStartY,
                            ySlowCount: fkDefinition.slowCountY,
                            ySlowDelta: fkDefinition.slowDeltaY,
                            values: undefined
                        })),
                    creationInfo
                };
                return fkSpectra;
            });
            return fkSegments;
        } catch (e) {
            logger.error(`Failed to read / process fk at ${file}`);
            return [];
        }
    });

    const stdsFks: OSDChannelSegment<FkPowerSpectraOSD>[] = [];
    stdsFkDataList.forEach(list => list.forEach(li => stdsFks.push(li)));
    const filterMappingsString = 'filterMappings';
    const filterMapPath = config.get('testData.standardTestDataSet.filterMappings');
    const filterMappings = readJsonData(filterMapPath)[filterMappingsString];
    return {
        channelSegments,
        fkPowerSpectraSegments: stdsFks,
        beamChannelSegmentsIds,
        currentFkIdx: stdsFkDataList.length - 1,
        currentBeamIdx: beamChannelSegmentsIds.length - 1,
        filterMappings
    };
}

/**
 * Update or add changed FkPowerSpectra Channel Segment to the data store
 */
export function updateFkPowerSpectra(newFkPowerSpectra: OSDChannelSegment<FkPowerSpectraOSD>) {
    if (newFkPowerSpectra && isFkSpectraChannelSegmentOSD(newFkPowerSpectra)) {
        const index = dataStore.fkPowerSpectraSegments.findIndex(fkp => fkp.id === newFkPowerSpectra.id);
        if (index >= 0) {
            dataStore.fkPowerSpectraSegments[index] = newFkPowerSpectra;
        } else {
            dataStore.fkPowerSpectraSegments.push(newFkPowerSpectra);
        }
    }
}

/**
 * Returns the next FK in the test data set, incrementing a static index.
 * This method provides a basic capability for mock FK data on demand
 * (e.g. to simulate calculating a new FK for an updated set of inputs specified
 * in the UI). If signal detection is set will try and lookup next Fk via that 
 * specific signal detection (list fkToSdhIds).
 */
export function getNextFk(sdHypId?: string): OSDChannelSegment<FkPowerSpectraOSD> {
    dataStore.currentFkIdx = (dataStore.currentFkIdx + 1) % dataStore.fkPowerSpectraSegments.length;
    return getFkFromStore(dataStore.fkPowerSpectraSegments[dataStore.currentFkIdx]);
}

/**
 * Gets new fk for given input - more or less a dummy function
 * @param createFkOsdInput window and frequency parameters for new fk
 */
export function computeFk(createFkOsdInput: ComputeFkInput): OSDChannelSegment<FkPowerSpectraOSD>[] {
    performanceLogger.performance('fkChannelSegment', 'enteringService', `${createFkOsdInput.startTime}`);
    const nextFk: OSDChannelSegment<FkPowerSpectraOSD> = getNextFk();

    // Clone the channel segment since we are going to change the spectrum list, start and endtimes below.
    // blank out the reference to the values (spectrums list)
    const timeseries: FkPowerSpectraOSD[] = nextFk.timeseries.map(series =>
        ({
            ...series,
            values: []
    }));
    const fkChannelSegment = {
        ...nextFk,
        timeseries
    };
    const sampleCount = createFkOsdInput.sampleCount;
    const valuesLength = nextFk.timeseries[0].values.length;
    // If timeseries[0] values is more than sample count down sample else add more to timeseries values
    // TODO: Fix if sample count is more than double the available values length
    let newValues: FkPowerSpectrumOSD[] = [];
    if (sampleCount < valuesLength) {
        newValues = nextFk.timeseries[0].values.slice(0, sampleCount);
    } else if (sampleCount > valuesLength) {
        let additionalAddedSamples = sampleCount;
        while (additionalAddedSamples > 0) {
            const currentRequiredSamples = additionalAddedSamples < 0 ? additionalAddedSamples : valuesLength;

            const moreSamples = nextFk.timeseries[0].values.slice(0, currentRequiredSamples);
            newValues = newValues.concat(moreSamples);
            additionalAddedSamples -= currentRequiredSamples;
        }
    }

    // Set the list of spectrums on the returning fk
    fkChannelSegment.timeseries[0].values = newValues;

    // Fix timeseries time, sample count, sample rate
    fkChannelSegment.timeseries[0].startTime = createFkOsdInput.startTime;
    fkChannelSegment.timeseries[0].sampleCount = fkChannelSegment.timeseries[0].values.length;
    fkChannelSegment.timeseries[0].sampleRate = createFkOsdInput.sampleRate;
    fkChannelSegment.timeseries[0].windowLead = createFkOsdInput.windowLead;
    fkChannelSegment.timeseries[0].windowLength = createFkOsdInput.windowLength;
    fkChannelSegment.timeseries[0].lowFrequency = createFkOsdInput.lowFrequency;
    fkChannelSegment.timeseries[0].highFrequency = createFkOsdInput.highFrequency;

    performanceLogger.performance('fkChannelSegment', 'returningFromService', `${createFkOsdInput.startTime}`);
    return [fkChannelSegment];
}

/**
 * Check if input is ChannelSegmentByIdInput or ChannelSegmentInput
 * getChannelSegment returns a channel segment based on the ID which is a OSD Timeseries (Waveform or FK)
 * @param input 
 * @returns a OSD channel segment[]
 */
function determineMethod(input: any): OSDChannelSegment<OSDTimeSeries>[] {
    // Check if input is ChannelSegmentByIdInput or ChannelSegmentInput
    if (input && input.ids) {
        // Get channel segments by id
        return getChannelSegment(input);
    } else {
        // Get segments by channel id and time range
        return getChannelSegmentsInput(input);
    }
}

/**
 * Returns the next Beam Channel Segment to be returned by the computeBeam call.
 * @param sampleRate match the sample rate of the Beam being replaced
 */
export function getNextBeam(sampleRate: number): string {
    // Get next index and update the dataStore limited to the length of the beam channel length
    let beamIdx = ++dataStore.currentBeamIdx % dataStore.beamChannelSegmentsIds.length;
    // Starting at next position walk through list to find next beam that match the rate
    let nextBeamId = dataStore.beamChannelSegmentsIds[beamIdx];
    let foundCanidate = false;
    while (!foundCanidate) {
        // Lookup beam in store
        const nextBeam = dataStore.channelSegments.find(cs => cs.id === nextBeamId);
        // If rate match found the canidate else keep looking
        if (nextBeam && nextBeam.timeseries[0].sampleRate === sampleRate) {
            foundCanidate = true;
        } else {
            beamIdx++;
            beamIdx %= dataStore.beamChannelSegmentsIds.length;
            nextBeamId = dataStore.beamChannelSegmentsIds[beamIdx];
        }
    }
    dataStore.currentBeamIdx = beamIdx;
    return nextBeamId;
}

/**
 * Mock to simulate COI streaming call to compute a new Beam (part of Fk)
 * @param input 
 */
function computeBeam(input: BeamFormingInput): OSDChannelSegment<OSDTimeSeries>[] {
    performanceLogger.performance('beamChannelSegment', 'enteringService', `${input.outputChannelId}`);
    if (!input) {
        return undefined;
    }

    // Got the beam but it still needs waveforms populated
    const nextBeamCSId = getNextBeam(input.waveforms[0].timeseries[0].sampleRate);
    if (!nextBeamCSId) {
        return [];
    }

    // build the query
    const query: ChannelSegmentByIdInput = {
        ids: [nextBeamCSId],
        'with-waveforms': true
    };
    const channelSegmentOSDMap = getChannelSegment(query);

    // The channel seg returns a Map, so get the channel segment out of it and return as part of a list
    const channelSegments: OSDChannelSegment<OSDTimeSeries>[] = [];
    if (channelSegmentOSDMap && Object.keys(channelSegmentOSDMap).length > 0) {
        // Clone and then set the times for the beam returning
        const channelSegment: OSDChannelSegment<OSDTimeSeries> =
            cloneDeep(channelSegmentOSDMap[Object.keys(channelSegmentOSDMap)[0]]);
        channelSegment.id = uuid4().toString();
        channelSegment.startTime = input.waveforms[0].startTime;
        channelSegment.endTime = input.waveforms[0].endTime;
        channelSegment.timeseries[0].startTime = input.waveforms[0].startTime;
        channelSegment.channelId = input.outputChannelId;
        channelSegments.push(channelSegment);
    }
    performanceLogger.performance('beamChannelSegment', 'returningFromService', `${input.outputChannelId}`);
    return channelSegments;
}

/**
 * Retrieve Channel Segment based on the Channel Segment Id
 * This could be an FK Spectrum or Waveform data structure
 */
function getChannelSegment(input: ChannelSegmentByIdInput):
    any | undefined {
    const channelSegmentId = input.ids[0];
    const channelSegment = dataStore.channelSegments.find(segment => segment.id === channelSegmentId);
    // Call the waveform mock backend to populate the waveform value
    // TODO add support for the various types and checks for single segment
    if (channelSegment) {
        if (isMockWaveformChannelSegment(channelSegment)) {
            const channelSegments: OSDChannelSegment<OSDWaveform>[] = getWaveformSegmentsByChannelSegments(
                toEpochSeconds(channelSegment.startTime), toEpochSeconds(channelSegment.endTime), [channelSegment]);
            const id: string = channelSegments[0].id;
            const toReturn = {};
            toReturn[id] = channelSegments[0];
            return toReturn;
        } else { // This is a FkSpectra Channel Segment
            return channelSegment;
        }
    } else {
        const fk = dataStore.fkPowerSpectraSegments.find(fkC => fkC.id === channelSegmentId);
        if (fk) {
            return getFkFromStore(fk);
        }
    }
    return undefined;
}

/**
 * Gets the channel segments 
 * @params input as ChannelSegmentInput
 */
export function getChannelSegmentsInput(
    input: ChannelSegmentInput): OSDChannelSegment<OSDTimeSeries>[] {
    return getChannelSegments(
        input['channel-ids'],
        toEpochSeconds(input['start-time']),
        toEpochSeconds(input['end-time'])
    );
}

/**
 * Gets channel segments by
 * @param channelIds channel IDs
 * @param startTime start time as epoch seconds
 * @param endTime end time as epoch seconds
 * @returns a OSD channel segment[]
 */
export function getChannelSegments(
    channelIds: string[], startTime: number, endTime: number): OSDChannelSegment<OSDTimeSeries>[] {
    const mins30 = 1800;
    const allChannelSegments: OSDChannelSegment<MockWaveform>[] =
        dataStore.channelSegments.filter(segment => {
            const segStartTime = toEpochSeconds(segment.startTime);
            const segEndTime = toEpochSeconds(segment.endTime);

            if (isMockWaveformChannelSegment(segment)) {
                // If not the channel id then this is not the one.
                // Else need to look at the length of the waveform (time length)
                // are we looking for a full raw segment something > 2 hours
                // or are we looking for raw data the lenght of fk-beam for compute beam
                if (channelIds.indexOf(segment.channelId) === -1) {
                    return false;
                } else if (endTime - startTime < mins30) {
                    // if less than 30 mins not full interval
                    return segStartTime <= startTime && segEndTime >= endTime;
                } else {
                    return Math.min(segEndTime, endTime) - Math.max(segStartTime, startTime) >= 0;
                }
            }
        }).filter(isMockWaveformChannelSegment);
    if (allChannelSegments.length > 0) {
        return getWaveformSegmentsByChannelSegments(startTime, endTime, allChannelSegments);
    }
    return [];
}

/**
 * Function used in backend only to find Channel Segments matching on the channel name to
 * channel segment name (At least start with channel name). This is due to the Filtered Channel
 * Segment channelId is a derived channel id and not the reference channel id.
 * @param channelIds Lisrt of reference channel ids
 * @param startTime Start of interval searching
 * @param endTime End of interval searching
 */
export function getAcquiredFilteredChannelSegments(
    channelIds: string[], startTime: number, endTime: number, filterId: string): OSDChannelSegment<OSDTimeSeries>[] {
    // list to return
    let allChannelSegments: OSDChannelSegment<MockWaveform>[] = [];
    // Loop through channel find each CS that matching on name and time range
    channelIds.forEach(cid => {
        const channel = getChannelById(cid);
        if (channel && channel.name) {
            // tslint:disable-next-line:arrow-return-shorthand
            allChannelSegments = allChannelSegments.concat(dataStore.channelSegments.filter(segment => {
                // const segStartTime = toEpochSeconds(segment.startTime);
                // const segEndTime = toEpochSeconds(segment.endTime);
                let pushSegment = false;
                if (segment.name.startsWith(channel.name) && segment.type === ChannelSegmentType.FILTER) {
                    const filterToUse: FilterMapping
                        = dataStore.filterMappings.find(fm => fm.filterId === filterId);
                    if (segment.name.includes(filterToUse.filterName)) {
                        pushSegment = true;
                    }
                }
                return pushSegment;
                // && Math.min(segEndTime, endTime) - Math.max(segStartTime, startTime) >= 0;
            }).filter(isMockWaveformChannelSegment));
        }

    });
    if (allChannelSegments.length > 0) {
        allChannelSegments = [allChannelSegments[0]];
        return getWaveformSegmentsByChannelSegments(startTime, endTime, allChannelSegments);
    }
    return [];
}

/**
 * Function used in backend only to find Channel Segments matching on the channel name to
 * channel segment name (At least start with channel name). This is due to the Filtered Channel
 * Segment channelId is a derived channel id and not the reference channel id.
 * @param channelIds Lisrt of reference channel ids
 * @param startTime Start of interval searching
 * @param endTime End of interval searching
 */
export function getBeamFilteredChannelSegments(startTime: number, endTime: number, filterId: string,
                                               csName: string): OSDChannelSegment<OSDTimeSeries>[] {
    const splitValues = csName.split('/fkb');
    const chanName = splitValues[0];
    const filterToUse: FilterMapping
        = dataStore.filterMappings.find(fm => fm.filterId === filterId);
     // list to return
    let allChannelSegments: OSDChannelSegment<MockWaveform>[] = [];
    // Loop through channel find each CS that matching on name and time range
    dataStore.channelSegments.forEach(cs => {
        const csStartTime = toEpochSeconds(cs.startTime);
        const csEndTime = toEpochSeconds(cs.endTime);
        if (cs.name.startsWith(chanName) && cs.name.includes(filterToUse.filterName) &&
            csStartTime <= startTime && csEndTime >= endTime &&
            isMockWaveformChannelSegment(cs)) {
            allChannelSegments.push(cs);
        }
    });
    if (allChannelSegments.length > 0) {
        allChannelSegments = [allChannelSegments[0]];
        return getWaveformSegmentsByChannelSegments(startTime, endTime, allChannelSegments);
    }
    return [];
}
/**
 * Handle cases where the data store has not been initialized.
 */
export function handleUnitializedDataStore() {
    // If the data store is uninitialized, throw an error
    if (!dataStore) {
        throw new Error('Mock backend channel segment processing data store has not been initialized');
    }
}
/**
 * Get fk timeseries by id and adds back spectrum values
 * @param channelId Id channel to get fk for
 * @param dehydratedSpectra fkSpectra to re-populate value fields for
 */
function getFkTimeseriesForChannelId(channelId: string, dehydratedSpectra: FkPowerSpectraOSD[]): FkPowerSpectraOSD[]  {
    const data = readJsonData(fkSegmentIdToPathMap.get(channelId));
    const seg: OSDChannelSegment<FkPowerSpectraOSD>
    // tslint:disable-next-line: no-string-literal
    = data['channelSegments'].find(s => s.id === channelId);
    const timeseries: FkPowerSpectraOSD[] = seg.timeseries.map(series => {
        const dehydratedSeries = dehydratedSpectra[0];
        return {
            ...dehydratedSeries,
            values: series.values
    }; });
    return timeseries;
}

/**
 * Gets an fk from the fk data store and rehydrates its values
 * @param fk fk to get
 */
function getFkFromStore(fk: OSDChannelSegment<FkPowerSpectraOSD>): OSDChannelSegment<FkPowerSpectraOSD> {
    const returnedFk = cloneDeep(fk);
    if (returnedFk.timeseries[0].values !== undefined) {
        return returnedFk;
    } else {
        const hydratedSeries = getFkTimeseriesForChannelId(fk.id, fk.timeseries);
        returnedFk.timeseries = hydratedSeries;
    }
    return returnedFk;
}

/**
 * Function to return the channel segment ids related to reference channel.
 * These channel segments use a derived channel id so cannot be found via getChannelSegment
 * using an input with channel id and timerange
 * @params input (reference channel id, timerange)
 * @params timeRange
 * @returns ChannelSegments (for now only filtered Raw or Beam)
 */
export function getDerivedChannelSegments(csId: string, filterIds: string[]):
    DerivedFilterChannelSegmentId[] {
        // Loop thru the the filterIds
        const input: ChannelSegmentByIdInput = {
            ids: [csId],
            'with-waveforms': false
        };
        const channelSegmentOSDMap: OSDChannelSegment<OSDTimeSeries> = getChannelSegment(input);
        if (!channelSegmentOSDMap || Object.keys(channelSegmentOSDMap).length === 0) {
            logger.warn(`Failed to find Channel Segment for Id: ${csId}`);
            return [];
        }
        const fkCS: OSDChannelSegment<OSDTimeSeries> =
            channelSegmentOSDMap[Object.keys(channelSegmentOSDMap)[0]];
        const csList: DerivedFilterChannelSegmentId[] = [];
        filterIds.forEach(fid => {
            const startTime = toEpochSeconds(fkCS.startTime);
            const endTime = toEpochSeconds(fkCS.endTime);
            if (fkCS.type === ChannelSegmentType.FK_BEAM) {
                const res = getBeamFilteredChannelSegments(startTime, endTime, fid, fkCS.name);
                csList.push({wfFiltertId: fid, csId: res[0].id});
            } else if (fkCS.type === ChannelSegmentType.ACQUIRED) {
                const res = getAcquiredFilteredChannelSegments([fkCS.channelId], startTime, endTime, fid);
                csList.push({wfFiltertId: fid, csId: res[0].id});
            }
        });
        return csList;
    }
