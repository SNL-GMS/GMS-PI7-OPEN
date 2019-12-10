import * as path from 'path';
import * as uuid4 from 'uuid/v4';
import { ChannelSegmentType, TimeSeriesType, OSDChannelSegment } from '../channel-segment/model';
import { CreatorType } from '../common/model';
import { MockWaveform, OSDWaveform, OSDChannelCalibration } from './model';
import * as config from 'config';
import { gatewayLogger as logger } from '../log/gateway-logger';
import * as fs from 'fs';
import { HttpMockWrapper } from '../util/http-wrapper';
import { getSecureRandomNumber } from '../util/common-utils';
import { toOSDTime } from '../util/time-utils';
import { readJsonData, resolveTestDataPaths } from '../util/file-parse';

/**
 * Waveform data store
 */
interface WaveformDataStore {
    calibrations: OSDChannelCalibration[];
}

let dataStore: WaveformDataStore;
const EVENT_AMPLITUDE = 1;
const NOISE_AMPLITUDE = 0.1;
const CONVERSION_WEIGHT_1000 = 1000;
const FOUR_BYTES = 4;

/**
 * Functional interface for a method to populate waveform samples with in the provided waveform
 * This interface is used to pass the appropriate sample builder function to the buildWaveformSegments
 * function based on configuration settings (either generating mock sample data or reading from file)
 */
interface SamplePopulator {
    (waveform: MockWaveform, startTime: number, segmentType: ChannelSegmentType,
     dataType?: string, calibrationFactor?: number): void;
}

let wfConfig;
let wfFilepaths;
let isInitialized = false;

/**
 * Inializes the mock wavefrom processor
 * @param httpMockWrapper axios mock wrapper
 */
export function initialize(httpMockWrapper: HttpMockWrapper): void {

    // If already initialized...
    if (isInitialized) {
        return;
    }

    logger.info('Initializing mock backend for waveform data');

    if (!httpMockWrapper) {
        throw new Error('Cannot initialize mock waveform filter services with undefined HTTP mock wrapper');
    }

    // Load the waveform filter backend service config settings
    wfConfig = config.get('waveform');
    wfFilepaths = config.get('testData.standardTestDataSet');
    // Load test data from the configured data set
    dataStore = loadTestData();

    // Set flag
    isInitialized = true;
}

/**
 * Reads in and loads test data
 * @returns a WaveformDataStore
 */
function loadTestData(): WaveformDataStore {
    const stdsConfig = config.get('testData.standardTestDataSet');
    const dataPath = resolveTestDataPaths().jsonHome;
    const calibrationFile = `${dataPath}${path.sep}${stdsConfig.waveform.channelCalibrationFile}`;
    let calibrations: OSDChannelCalibration[] = [];
    try {
        calibrations = readJsonData(calibrationFile);
    } catch (e) {
        logger.error(`Failed to calibration data from file: ${stdsConfig.waveform.channelCalibrationFile}`);
    }
    return {
        calibrations
    };
}

/**
 * Gets calibratoin factor
 * @param channelId a channel ID
 */
function getCalibrationFactor(channelId: string): number {
    const defaultCalibrationFactor =  1;
    const calibration = dataStore.calibrations.find(cal => cal.channelId === channelId);
    if (calibration && calibration.calibrationFactor) {
        return calibration.calibrationFactor;
    }
    return  defaultCalibrationFactor;
}

/**
 * Creates collection of waveform channel segments corresponding to the input
 * channel IDs and time range, using the provided populateSamples method
 * to populate the samples in each waveform (e.g. generate or read from file).
 * This is a package private helper used by the exported function:
 * getWaveformSegmentsByChannel()
 * @param startTime The start time of the waveform data to retrieve
 * @param endTime The end time of the waveform data to retrieve
 * @param channelIds The list of channel IDs to retrieve waveform data for
 * @param populateSamples The function that will be used to populate samples
 * for each waveform object
 */
function createWaveformSegments(
    startTime: number, channelSegments: OSDChannelSegment<MockWaveform>[],
    populateSamples: SamplePopulator): OSDChannelSegment<OSDWaveform>[] {

    // Initialize the array of ChannelSegments to return
    const requestedChannelSegments: OSDChannelSegment<OSDWaveform>[] = [];

    // For each matching channel segment in the pre-loaded test set,
    // create a new channel segment tailored to the requested time range,
    // (i.e. containing only the waveforms that fall within the requested time range)
    channelSegments.forEach(segment => {
        const channelId: string = segment.channelId;
        const calibrationFactor = getCalibrationFactor(channelId);
        const newSegment: OSDChannelSegment<OSDWaveform> = {
            id: segment.id,
            type: segment.type,
            timeseriesType: TimeSeriesType.WAVEFORM,
            name: segment.name,
            startTime: segment.startTime,
            endTime: segment.endTime,
            channelId: segment.channelId,
            timeseries: segment.timeseries.map(mockWaveform => {
                populateSamples(mockWaveform, startTime, segment.type, mockWaveform.dataType, calibrationFactor);
                return {
                    startTime: segment.startTime,
                    sampleCount: mockWaveform.sampleCount,
                    sampleRate: mockWaveform.sampleRate,
                    values: mockWaveform.values
                };
            }),
            // featureMeasurementIds: [],
            creationInfo: {
                id: uuid4().toString(),
                creationTime: toOSDTime(Date.now() / CONVERSION_WEIGHT_1000),
                creatorId: 'Auto',
                creatorType: CreatorType.System,
                creatorName: 'Spencer',
                softwareInfo: {
                    name: 'yes',
                    version: '0'
                }
            }
        };

        if (newSegment.timeseries.length > 0) {
            requestedChannelSegments.push(newSegment);
        }
    });
    return requestedChannelSegments;
}

/**
 * Creates collection of waveform channel segments corresponding to the input
 * channel IDs and time range.
 * @param startTime The start time of the waveform data to retrieve
 * @param endTime The end time of the waveform data to retrieve
 * @param channelIds The list of channel IDs to retrieve waveform data for
 */
export function getWaveformSegmentsByChannelSegments(
    startTime: number, endTime: number, channelSegments: OSDChannelSegment<MockWaveform>[]):
        OSDChannelSegment<OSDWaveform>[] {

    // Read the waveform sample creation mode from configuration settings
    // (e.g. generate, load from file)
    const waveformMode: string = wfConfig.waveformMode;
    // If configured for 'simulated' data, generate mock waveform data sets
    if (waveformMode.toLowerCase() === 'simulated') {
        return createWaveformSegments
            (startTime, channelSegments, generateMockWaveformSamples);
        // If configured for data 'fromFile', read waveforms from the configured files
    } else if (waveformMode.toLowerCase() === 'fromfile') {
        return createWaveformSegments
            (startTime, channelSegments, readWaveformSamples);
    } else {
        // Invalid waveformMode -- an empty list of waveforms will be returned.
        logger.debug(`Invalid testData.waveformMode config value: ${waveformMode}`);
    }

    return [];
}

/**
 * Read in raw waveform data for a desired channel and time interval 
 * and assign the sample array to the provided waveform object.
 * IMPLEMENTS SamplePopulator
 * @param waveform Waveform object to read in samples for
 * @param startTime The start time of the waveform data to retrieve.
 * @param endTime The end time of the waveform data to retrieve.
 * @param channelId The channel ID of the waveform.
 */
function readWaveformSamples(
    waveform: MockWaveform, startTime: number, segmentType: ChannelSegmentType,
    dataType: string, calibrationFactor: number) {
    // Explicitly casting numeric values out of `wfd` to Number to
    // avoid weird string/number issues.

    const wPath = `${wfFilepaths.stdsDataHome}/${wfFilepaths.waveform.files}`;

    const waveformDir = [wPath.replace(/\$\{([^\}]+)\}/g, (_, v) => process.env[v])];
    if (segmentType !== ChannelSegmentType.ACQUIRED) {
        calibrationFactor = 1;
    }
    // If waveformDir is a relative path, make it absolute by prefixing
    // it with the current working directory.
    if (!path.isAbsolute(waveformDir[0])) {
        waveformDir.unshift(process.cwd());
    }
    const waveformFile: string = waveformDir.concat(waveform.waveformFile).join(path.sep);
    logger.debug(`startTime:       ${startTime}`);
    logger.debug(`waveformFile:    ${waveformFile}`);
    logger.debug(`segmentType:     ${segmentType}`);
    waveform.values = readWaveformSamplesFromFile(
        waveformFile, waveform.fOff, waveform.sampleCount * FOUR_BYTES,
        calibrationFactor, segmentType, dataType);
}

/**
 * Read in raw waveform data from an "s4" formatted .w file.
 * @param filename Full or relative path to "s4" formatted waveform file on disk.
 * @param readOffsetInBytes Offset in bytes within the file to start reading at.
 * @param readLengthInBytes Number of bytes to read.
 * @param calibrationFactor Optional calibration factory to multiply each raw sample by (default = 1.0).
 * @return A number[] containing all of the read waveform samples.
 */
function readWaveformSamplesFromFile(
    filename: string,
    readOffsetInBytes: number,
    readLengthInBytes: number,
    calibrationFactor: number = 1,
    segmentType: ChannelSegmentType,
    dataType: string
): number[] {

    // Read in the desired block of bytes from the specified s4 waveform file.
    const fd = fs.openSync(filename, 'r');
    const byteBuffer = new Buffer(readLengthInBytes);
    const bytesRead = fs.readSync(fd, byteBuffer, 0, readLengthInBytes, readOffsetInBytes);
    fs.closeSync(fd);

    // For the raw waveforms the bytes read in are actually 32-bit big-endian integer "samples"
    // Convert them and multiply the result by the calibrationFactor to get the
    // actual samples.
    // For filtered waveforms the bytes are 32-bit little-endian floats "samples"
    // No calibrationFactor is applied since it has been applied when written to .w file
    // FIXME- need to agree on the endianess of the .w data to be consistent
    // const segmentTypeToCompare = Number(ChannelSegmentType[segmentType]);
    const samples: number[] = Array.from(new Float32Array(bytesRead / FOUR_BYTES));
    if (dataType === 'f4') { // segmentType === ChannelSegmentType.FK_BEAM) {
        for (let i = 0; i < bytesRead; i += FOUR_BYTES) {
            samples[i / FOUR_BYTES] = byteBuffer.readFloatBE(i);
        }
    } else if (dataType === 's4') {
        for (let i = 0; i < bytesRead; i += FOUR_BYTES) {
            samples[i / FOUR_BYTES] = calibrationFactor * byteBuffer.readInt32BE(i);
        }
    } else {
        logger.error('Waveform DataType not supported: cannot read waveform data');
    }

    return samples;
}

/**
 * Generates a mock waveform, with generated samples.
 * IMPLEMENTS SamplePopulator
 * @param sampleCount The number of samples in the mock waveform
 */
function generateMockWaveformSamples(
    waveform: MockWaveform, startTime: number, segmentType: ChannelSegmentType) {

    logger.debug(`waveform.StartTime: ${waveform.startTime}`);
    logger.debug(`startTime: ${startTime}`);
    logger.debug(`dataSampleRate ${waveform.sampleRate}`);

    // Negative sample count indicates that the waveform falls outside the requested time range
    // in this case, do not generate waveform samples
    if (waveform.sampleCount > 0) {
        waveform.values = createMockSamples(waveform.sampleCount, EVENT_AMPLITUDE, NOISE_AMPLITUDE);
    }
}

/**
 *  Generates a mock waveform sample set
 * @param samples 
 * @param eventAmplitude 
 * @param noiseAmplitude 
 */
function createMockSamples(samples: number, eventAmplitude: number, noiseAmplitude: number): number[] {

    let currentEventAmplitude = 0;
    let currentEventPeak = 0;
    let eventBuildup = 0;
    const data = Array.from(new Float32Array(samples));

    for (let i = 1; i < samples; i++) {

        // tslint:disable-next-line:no-magic-numbers
        if (i % Math.round(samples / (getSecureRandomNumber() * 10)) === 0) {
            // tslint:disable-next-line:no-magic-numbers
            currentEventAmplitude = 0.05;
            currentEventPeak = getSecureRandomNumber() * eventAmplitude;
            eventBuildup = 1;
        }
        if (currentEventAmplitude >= currentEventPeak) {
            eventBuildup = -1;
        }
        if (eventBuildup === 1) {
            // tslint:disable-next-line:no-magic-numbers
            currentEventAmplitude += currentEventAmplitude * (1 / samples) * 125;
        } else if (eventBuildup === -1) {
            // tslint:disable-next-line:no-magic-numbers
            currentEventAmplitude -= currentEventAmplitude * (1 / samples) * 62;
        }
        if (currentEventAmplitude < 0) {
            currentEventAmplitude = 0;
        }
        data[i - 1] = currentEventAmplitude + noiseAmplitude - getSecureRandomNumber() * 2 * noiseAmplitude
            - getSecureRandomNumber() * 2 * currentEventAmplitude;
    }

    return data;
}
