import { GraphqlQueryControls } from 'react-apollo';
import { ChannelSegment, TimeSeries } from '~graphql/channel-segment/types';
import { StationTypes } from '../';
import { CreationInfo, Location, PhaseType, TimeRange, Units } from '../common/types';

// ***************************************
// Mutations
// ***************************************
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

export interface CreateDetectionsMutationArgs {
  input: NewDetectionInput;
}

export interface UpdateDetectionsMutationArgs {
  detectionIds: string[];
  input: UpdateDetectionInput;
}

export interface RejectDetectionsInput {
  detectionIds: string[];
}

export interface RejectDetectionsMutationArgs {
  detectionIds: string[];
}

// ***************************************
// Subscriptions
// ***************************************

export interface DetectionsCreatedSubscription {
  detectionsCreated: SignalDetection[];
}

// ***************************************
// Queries
// ***************************************

export interface SignalDetectionsByDefaultStationsQueryArgs {
  timeRange: TimeRange;
}

export interface SignalDetectionsByStationQueryArgs {
  stationIds: string[];
  timeRange: TimeRange;
}

// tslint:disable-next-line:max-line-length interface-over-type-literal
export type SignalDetectionsByStationQueryProps =  { signalDetectionsByStationQuery: GraphqlQueryControls<{}> & {signalDetectionsByStation: SignalDetection[]}};

export interface SignalDetectionsByEventQueryArgs {
  eventId: string;
}

// tslint:disable-next-line:max-line-length interface-over-type-literal
export type SignalDetectionsByEventQueryProps =  { signalDetectionsByEventIdQuery: GraphqlQueryControls<{}> & {signalDetectionsByEventId: SignalDetection[]}};

// ***************************************
// Model
// ***************************************
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
 * Represents a measurement of a signal detection feature,
 * including arrival time, azimuth, slowness and phase
 */
export interface FeatureMeasurement {
  id: string;
  channelSegment?: ChannelSegment<TimeSeries>;
  measurementValue: FeatureMeasurementValue;
  featureMeasurementType: FeatureMeasurementTypeName;
  creationInfo?: CreationInfo;
  uncertainty?: number;
  calculationTime?: string;
  definingRules?: [DefiningRule];
  // channelSegmentFkData?: FkPowerSpectra;
}

/**
 * Represents Feature Measurement Value (fields are dependent on type of FM)
 */
// tslint:disable-next-line:no-empty-interface
export interface FeatureMeasurementValue {
  // no common members
}

/**
 * Represents Feature Measurement Value for a double type.
 */
export interface DoubleValue {
  value: number;
  standardDeviation: number;
  units: Units;
}

/**
 * Represents Feature Measurement Value for a aplitude type.
 */
export interface AmplitudeMeasurementValue extends FeatureMeasurementValue {
  startTime: number;
  period: number;
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
 * Represents Feature Measurement Value for a numeric type.
 */
export interface StringMeasurementValue extends FeatureMeasurementValue {
  strValue: string;
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
}

export interface SignalDetectionHypothesis {
  id: string;
  rejected: boolean;
  featureMeasurements: FeatureMeasurement[];
  creationInfo: CreationInfo;
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
  creationInfo: CreationInfo;
}

export interface SignalDetection {
  id: string;
  monitoringOrganization: string;
  station: {
    id: string;
    name?: string;
    location: Location;
    defaultChannel: StationTypes.ProcessingChannel;
    sites: {
      id: string;
      name?: string;
      channels: StationTypes.ProcessingChannel[];
    }[];
  };
  currentHypothesis: SignalDetectionHypothesis;
  signalDetectionHypothesisHistory: SignalDetectionHypothesisHistory[];
  modified: boolean;
  hasConflict: boolean;
  associationModified: boolean;
  creationInfo: CreationInfo;
}
