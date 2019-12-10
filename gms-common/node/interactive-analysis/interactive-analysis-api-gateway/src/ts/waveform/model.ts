import { TimeSeries, OSDTimeSeries, OSDChannelSegment, isOSDWaveformChannelSegment } from '../channel-segment/model';

/**
 * Waveform time series
 */
export interface Waveform extends TimeSeries {
    values: number[];
}

/**
 * OSD representation of a Waveform
 */
export interface OSDWaveform extends OSDTimeSeries {
    values: number[];
}

/**
 * Waveform file info
 */
export interface WaveformFileInfo {
    sampleCount: number;
    fOff: number;
    waveformFile: string;
    dataType: string;
}

/**
 * Represents a time-ordered set of waveform samples output from a channel
 * (derived or raw) within a specified time range.
 */
export interface MockWaveform extends OSDWaveform {
    sampleCount: number;
    fOff: number;
    waveformFile: string;
    dataType: string;
}

/**
 * Channel Calbration object definition supports calibration defined in calibration.json.
 * Used in reading waveforms in waveform mock backend
 */
export interface OSDChannelCalibration {
        id: string;
        channelId: string;
        calibrationInterval: number;
        calibrationFactor: number;
        calibrationFactorError: number;

        calibrationPeriod: number;
        timeShift: number;
        actualTime: string;
        systemTime: string;
        informationSource: {
            originatingOrganization: string;
            informationTime: string;
            reference: string;
        };
        comment: string;
}
/**
 * Checks whether object that is a waveform is also a MockWaveform.
 * @param object object of type Waveform
 * @returns a boolean
 */
export function isMockWaveform(object: OSDWaveform): object is MockWaveform {
    return 'sampleCount' in object && 'waveformFile' in object && 'fOff' in object;
}

/**
 * Checks if mock waveform channel segment
 * @param object OSD representation of a channel segment
 * @returns a boolean
 */
export function isMockWaveformChannelSegment(
    object: OSDChannelSegment<OSDTimeSeries>): object is OSDChannelSegment<MockWaveform> {
    const mock = isOSDWaveformChannelSegment(object) &&
        object.timeseries.every(isMockWaveform);
    return mock;
}
