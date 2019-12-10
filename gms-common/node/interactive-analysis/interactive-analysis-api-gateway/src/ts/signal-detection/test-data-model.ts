import { CreationInfo } from '../common/model';
import { DefiningRule } from './model';

/**
 * Model definitions for the signal detection data API
 */

/**
 * Represents a signal detection marking the arrival of a signal of interest on
 * channel within a time interval.
 */
export interface TDSignalDetection {
    id: string;
    monitoringOrganization: string;
    stationId: string;
    hypotheses: TDSignalDetectionHypothesis[];
    currentHypothesis: TDSignalDetectionHypothesis;
    modified: boolean;
    associationModified: boolean;
}

/**
 * Represents a proposed explanation for a Signal Detection
 */
export interface TDSignalDetectionHypothesis {
    id: string;
    phase: string;
    rejected: boolean;
    signalDetectionId: string;
    arrivalTimeMeasurement: TimeFeatureMeasurement;
    azSlownessMeasurement: AzSlownessFeatureMeasurement;
    creationInfo: CreationInfo;
}

/**
 * Enumeration of feature measurement types
 */
export enum TDFeatureType {
    ArrivalTime = 'ArrivalTime',
    AzimuthSlowness = 'AzimuthSlowness',
    Amplitude = 'Amplitude'
}

/**
 * Concrete feature measurement type for signal detection arrival time
 */
export interface TimeFeatureMeasurement {
    id: string;
    hypothesisId: string;
    featureType: TDFeatureType;
    definingRules: DefiningRule[];
    timeSec: number;
    uncertaintySec: number;
    channelSegmentId: string;
}

/**
 * Concrete feature measurement type for signal detection azimuth/slowness
 */
export interface AzSlownessFeatureMeasurement {
    id: string;
    hypothesisId: string;
    featureType: TDFeatureType;
    azimuthDefiningRules: DefiningRule[];
    slownessDefiningRules: DefiningRule[];
    azimuthDeg: number;
    slownessSecPerDeg: number;
    azimuthUncertainty: number;
    slownessUncertainty: number;
    fkDataId: string;
    channelSegmentId: string;
}
