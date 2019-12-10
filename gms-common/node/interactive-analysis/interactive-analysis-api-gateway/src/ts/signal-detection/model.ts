import { PhaseType } from '../channel-segment/model-spectra';
import { Units } from '../event/model';

/**
 * Model definitions for the signal detection data API
 */

/**
 * Represents a signal detection marking the arrival of a signal of interest on
 * channel within a time interval.
 */
export interface SignalDetection {
    id: string;
    monitoringOrganization: string;
    stationId: string;
    creationInfoId: string;
    signalDetectionHypotheses: SignalDetectionHypothesis[];
    currentHypothesis?: SignalDetectionHypothesis;
    modified?: boolean;
    associationModified?: boolean;
}

/**
 * Represents a proposed explanation for a Signal Detection
 */
export interface SignalDetectionHypothesis {
    id: string;
    rejected: boolean;
    parentSignalDetectionId: string;
    featureMeasurements: FeatureMeasurement[];
    creationInfoId: string;
    fkDataId?: string;
}

/**
 * SignalDetectionHypothesisHistory used by SD History Table
 */
export interface SignalDetectionHypothesisHistory {
    id: string;
    phase: string;
    rejected: boolean;
    arrivalTimeSecs: number;
    arrivalTimeUncertainty: number;
    creationInfoId: string;
}

/**
 * Represents a measurement of a signal detection feature,
 * including arrival time, azimuth, slowness and phase
 */
export interface FeatureMeasurement {
    id: string;
    channelSegmentId: string;
    measurementValue: FeatureMeasurementValue;
    featureMeasurementType: FeatureMeasurementTypeName;
    creationInfoId: string;
    uncertainty?: number;
    calculationTime?: string;
    definingRules?: [DefiningRule];
}

/**
 * Holds a featureMeasurementTypeName
 */

export interface FeatureMeasurementTypeNameField {
    featureMeasurementTypeName: FeatureMeasurementTypeName;
}

/**
 * Represents Feature Measurement Value (fields are dependent on type of FM)
 */
// tslint:disable-next-line:no-empty-interface
export interface FeatureMeasurementValue {
    // no common members
}
/**
 * Value used in Feature Measurements
 */
export interface DoubleValue {
    value: number;
    standardDeviation: number;
    units: Units;
}
/**
 * Represents Feature Measurement Value for a amplitude type.
 */
export interface AmplitudeMeasurementValue extends FeatureMeasurementValue {
    startTime: number;
    period: string;
    amplitude: DoubleValue;
}

/**
 * Represents Feature Measurement Value for a instant type.
 */
export interface InstantMeasurementValue extends FeatureMeasurementValue {
    value: number;
    standardDeviation: number;
}

/**
 * Represents Feature Measurement Value for a numeric type.
 */
export interface NumericMeasurementValue extends FeatureMeasurementValue {
    referenceTime: number;
    measurementValue: DoubleValue;
}

/**
 * Represents Feature Measurement Value for a phase type.
 */
export interface PhaseTypeMeasurementValue extends FeatureMeasurementValue {
    phase: PhaseType;
    confidence: number;
}

/**
 * Represents Feature Measurement Value with a generic string
 */
export interface StringMeasurementValue extends FeatureMeasurementValue {
    strValue: string;
}

/**
 * Represents Feature Measurement Value for a instant type.
 */
export interface InstantMeasurementValueOSD extends FeatureMeasurementValue {
    value: string;
    standardDeviation: string;
}

/**
 * Represents Feature Measurement Value for a numeric type.
 */
export interface NumericMeasurementValueOSD extends FeatureMeasurementValue {
    referenceTime: string;
    measurementValue: DoubleValue;
}

/**
 * Enumeration of feature measurement type names
 */
export enum FeatureMeasurementTypeName {
    ARRIVAL_TIME = 'ARRIVAL_TIME',
    RECEIVER_TO_SOURCE_AZIMUTH = 'RECEIVER_TO_SOURCE_AZIMUTH',
    SOURCE_TO_RECEIVER_AZIMUTH = 'SOURCE_TO_RECEIVER_AZIMUTH',
    SLOWNESS = 'SLOWNESS',
    AMPLITUDE = 'AMPLITUDE',
    PHASE = 'PHASE',
    EMERGENCE_ANGLE = 'EMERGENCE_ANGLE',
    PERIOD = 'PERIOD',
    RECTILINEARITY = 'RECTILINEARITY',
    SNR = 'SNR',
    AMPLITUDE_A5_OVER_2 = 'AMPLITUDE_A5_OVER_2',
    AMPLITUDE_ALR_OVER_2 = 'AMPLITUDE_ALR_OVER_2',
    AMPLITUDEh_ALR_OVER_2 = 'AMPLITUDEh_ALR_OVER_2',
    AMPLITUDE_ANL_OVER_2 = 'AMPLITUDE_ANL_OVER_2',
    FILTERED_BEAM = 'FILTERED_BEAM'

    /* Following have been copied from latest OSD java code: 11/09/2018
    SOURCE_TO_RECEIVER_DISTANCE,
    FIRST_MOTION,
    AMPLITUDE_A5_OVER_2,
    TODO: the following values are not in the guidance. find out if they are deprecated, or should be removed
    AZIMUTH,
    AMPLITUDE,
    F_STATISTIC,
    FK_QUALITY,
    PHASE;

    Saw these in FP Service Log as to acceptable entries
    SIGNAL_DURATION,
    */
}

/**
 * Enumeration of operation types used in defining rules
 */
export enum DefiningOperationType {
    Location = 'Location',
    Magnitude = 'Magnitude'
}

/**
 * Represents the defining relationship (isDefining: true|false) for an operation type (e.g. location, magnitude)
 */
export interface DefiningRule {
    operationType: DefiningOperationType;
    isDefining: boolean;
}

/**
 * Signal Detection Timing. Input object that groups ArrivalTime and AmplitudeMeasurement
 */
export interface SignalDetectionTimingInput {
    // The detection time (seconds since epoch) to assign to the new detection's initial hypothesis
    arrivalTime: number;

    // The uncertainty (seconds) associated with the time input
    timeUncertaintySec: number;

    // The Amplitude Measurement Value
    amplitudeMeasurement?: AmplitudeMeasurementValue;
}
/**
 * Input used to create a new signal detection with an initial hypothesis
 * and time feature measurement
 */
export interface NewDetectionInput {
    stationId: string;
    phase: string;
    // Signal Detection Timing Input for ArrivalTime and AmplitudeMeasurementjj
    signalDetectionTiming: SignalDetectionTimingInput;
    eventId?: string;
}

/**
 * Input used to update an existing signal detection
 */
export interface UpdateDetectionInput {
    phase?: string;
    // Signal Detection Timing Input for ArrivalTime and AmplitudeMeasurementjj
    signalDetectionTiming?: SignalDetectionTimingInput;
}
