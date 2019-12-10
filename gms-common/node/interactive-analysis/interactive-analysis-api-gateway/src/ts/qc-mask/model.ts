import { TimeRange } from '../common/model';

/**
 * qc-mask categories 
 */
export enum QcMaskCategory {
    AnalystDefined = 'ANALYST_DEFINED',
    ChannelProcessing = 'CHANNEL_PROCESSING',
    DataAuthentication = 'DATA_AUTHENTICATION',
    Rejected = 'REJECTED',
    StationSOH = 'STATION_SOH',
    WaveformQuality = 'WAVEFORM_QUALITY'
}

/**
 * Qc-Mask types
 */
export enum QcMaskType {
    SensorProblem = 'SENSOR_PROBLEM',
    StationProblem = 'STATION_PROBLEM',
    Calibration = 'CALIBRATION',
    StationSecurity = 'STATION_SECURITY',
    Timing = 'TIMING',
    RepairableGap = 'REPAIRABLE_GAP',
    RepeatableAdjacentAmplitudeValue = 'REPEATED_ADJACENT_AMPLITUDE_VALUE',
    LongGap = 'LONG_GAP',
    Spike = 'SPIKE'
}

/**
 * Represents a QC Mask
 */
export interface QcMask {
    id: string;
    channelId: string;
    qcMaskVersions: QcMaskVersion[];
}

/**
 * Represents a QC Mask version entry
 */
export interface QcMaskVersion {
    version: number;
    parentQcMasks: ParentQcMask[];
    channelSegmentIds: string[];
    category: string;
    type: string;
    rationale: string;
    startTime: string;
    endTime: string;
    creationInfoId: string;
}

/**
 * Represents a QC Mask parent
 */
interface ParentQcMask {
    qcMaskId: string;
    qcMaskVersionId: number;
}

/**
 * Represents a QcMask Input
 */
export interface QcMaskInput {
    timeRange: TimeRange;
    category: QcMaskCategory;
    type: QcMaskType;
    rationale: string;
}
