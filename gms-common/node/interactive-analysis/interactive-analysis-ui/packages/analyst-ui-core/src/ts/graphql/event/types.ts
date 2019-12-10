import { GraphqlQueryControls } from 'react-apollo';
import { FeatureMeasurementValue, SignalDetection } from '~graphql/signal-detection/types';
import { SignalDetectionTypes } from '..';
import { CreationInfo, PhaseType } from '../common/types';

// ***************************************
// Mutations
// ***************************************

export interface AssociationChange {
  events: Event[];
  sds: SignalDetection[];
}
export interface UpdateEventsInput {
  creatorId: string;
  processingStageId: string;
  status?:
    | 'ReadyForRefinement'
    | 'OpenForRefinement'
    | 'AwaitingReview'
    | 'Complete';
  preferredHypothesisId?: string;
  activeAnalystUserNames?: string[];
}

export interface UpdateEventsMutationArgs {
  eventIds: string[];
  input: UpdateEventsInput;
}
export interface UpdateFeaturePredictionsMutationArgs {
  eventId: string;
}

/***
 * Locate Event Mutation Args
 */
export interface LocateEventMutationArgs {
  eventHypothesisId: string;
  preferredLocationSolutionId: string;
  locationBehaviors: LocationBehavior[];
}
export interface ChangeSignalDetectionAssociationsMutationArgs {
  eventHypothesisId: string;
  signalDetectionHypoIds: string[];
  associate: boolean;
}
export interface CreateEventMutationArgs {
  signalDetectionHypoIds: string[];
}

// ***************************************
// Subscriptions
// ***************************************

export interface EventUpdatedSubscription {
  eventsUpdated: {
      id: string;
      status: string;
      activeAnalysts: {
          userName: string;
      }[];
  }[];
}

export interface EventsCreatedSubscription {
  eventsCreated: Event[];
}

// ***************************************
// Queries
// ***************************************

export interface EventsInTimeRangeQueryArgs {
  timeRange: {
    startTime: number;
    endTime: number;
  };
}

// tslint:disable-next-line:max-line-length interface-over-type-literal
export type EventsInTimeRangeQueryProps =  { eventsInTimeRangeQuery: GraphqlQueryControls<{}> & {eventsInTimeRange: Event[]}};

export interface EventsByIdQueryArgs {
  eventId: string;
}

// tslint:disable-next-line:max-line-length interface-over-type-literal
export type EventsByIdQueryProps =  { eventsByIdQuery: GraphqlQueryControls<{}> & {eventsById: Event[]}};

// ***************************************
// Model
// ***************************************

export enum EventStatus {
  ReadyForRefinement = 'ReadyForRefinement',
  OpenForRefinement = 'OpenForRefinement',
  AwaitingReview = 'AwaitingReview',
  Complete = 'Complete',
}

export interface SignalDetectionEventAssociation {
  id: string;
  rejected: boolean;
  signalDetectionHypothesis: {
    id: string;
    rejected: boolean;
    creationInfo: CreationInfo;
  };
  eventHypothesisId: string;
}

export interface EventHypothesis {
  id: string;
  rejected: boolean;
  event: {
    id: string;
    status: EventStatus;
  };
  associationsMaxArrivalTime: number;
  signalDetectionAssociations: SignalDetectionEventAssociation[];
  // Not in OSD, only has a single set and is called locationSolutions
  locationSolutionSets: LocationSolutionSet[];
  preferredLocationSolution: PreferredLocationSolution;
}
export interface Ellipse {
  scalingFactorType: ScalingFactorType;
  kWeight: number;
  confidenceLevel: number;
  majorAxisLength: string;
  majorAxisTrend: number;
  minorAxisLength: string;
  minorAxisTrend: number;
  depthUncertainty: number;
  timeUncertainty: string;
}

export interface Ellipsoid {
  scalingFactorType: ScalingFactorType;
  kWeight: number;
  confidenceLevel: number;
  majorAxisLength: string;
  majorAxisTrend: number;
  majorAxisPlunge: number;
  intermediateAxisLength: number;
  intermediateAxisTrend: number;
  intermediateAxisPlunge: number;
  minorAxisLength: string;
  minorAxisTrend: number;
  minorAxisPlunge: number;
  depthUncertainty: number;
  timeUncertainty: string;
}

/**
 * Represents a preference relationship between an event hypothesis and a location solution.
 * Creation information is included in order to capture provenance of the preference.
 */
export interface PreferredLocationSolution {
  locationSolution: LocationSolution;
  creationInfo: CreationInfo; // in OSD: DNE
}
/**
 * Location Uncertainty for event Location Solution
 */
export interface LocationUncertainty {
  xy: number;
  xz: number;
  xt: number;
  yy: number;
  yz: number;
  yt: number;
  zz: number;
  zt: number;
  tt: number;
  stDevOneObservation: number;
  ellipses: Ellipse [];
  ellipsoids: Ellipsoid [];
}

/**
 * Represents an estimate of the location of an event, defined as latitude, longitude, depth, and time.
 * A location solution is often determined by a location algorithm that minimizes the difference between
 * feature measurements (usually arrival time, azimuth, and slowness) and corresponding feature predictions.
 */
export interface LocationSolution {
  id: string;
  locationType: string;
  location: EventLocation;
  featurePredictions: FeaturePrediction[];
  locationRestraint: LocationRestraint;
  locationUncertainty?: LocationUncertainty;
  locationBehaviors: LocationBehavior[];
  snapshots: SignalDetectionSnapshot[];
}

/**
 * Location Solution Set
 * defines a list of location solutions for an event hypotheis
 * including a snapshot of association when solutions were created
 */
export interface LocationSolutionSet {
  id: string;
  count: number;
  locationSolutions: LocationSolution[];
}

/**
 * Snapshot of state of associations when location solution was created
 */
export interface SignalDetectionSnapshot {
  signalDetectionId: string;
  signalDetectionHypothesisId: string;
  stationId: string;
  stationName: string;
  channelName: string;
  phase: PhaseType;
  time: EventSignalDetectionAssociationValues;
  slowness: EventSignalDetectionAssociationValues;
  azimuth: EventSignalDetectionAssociationValues;
}

/**
 * Generic interface for snapshot values of a signal detection association
 */
export interface EventSignalDetectionAssociationValues {
  defining: boolean;
  observed: number;
  residual: number;
  correction: number;
}
/**
 * Location Behavior for event Location Solution
 */
export interface LocationBehavior {
  residual: number;
  weight: number;
  defining: boolean;
  featurePredictionId: string;
  featureMeasurementId: string;
}

/**
 * Location Restraint for event Location Solution
 */
export interface LocationRestraint {
    depthRestraintType: DepthRestraintType;
    depthRestraintKm: number;
    latitudeRestraintType: RestraintType;
    latitudeRestraintDegrees?: number;
    longitudeRestraintType: RestraintType;
    longitudeRestraintDegrees?: number;
    timeRestraintType: RestraintType;
    timeRestraint?: string;
}
/**
 * 
 * Event Location definition
 */
export interface EventLocation {
  latitudeDegrees: number;
  longitudeDegrees: number;
  depthKm: number;
  time: number;
}
/**
 * Feature Prediction - part of preferred event hypothesis
 */
export interface FeaturePrediction {
  id: string;
  predictedValue: FeatureMeasurementValue;
  predictionType: SignalDetectionTypes.FeatureMeasurementTypeName;
  phase: string;
  channelId?: string;
  stationId?: string;
}

/**
 * Double Value used in OSD common objects
 */
export interface DoubleValue {
  value: number;
  standardDeviation: number;
  // units: Units;
}
export interface PreferredEventHypothesis {
  processingStage: {
    id: string;
  };
  eventHypothesis: EventHypothesis;
}

export interface Event {
  id: string;
  status: EventStatus;
  modified: boolean;
  hasConflict: boolean;
  currentEventHypothesis: PreferredEventHypothesis;
  activeAnalysts: {
    userName: string;
  }[];
  conflictingSdHypIds: string[];
}

/**
 * RestraintType for Location Restraint
 */
export enum RestraintType {
  UNRESTRAINED = 'UNRESTRAINED',
  FIXED = 'FIXED'
}

/**
 * DepthRestraintType for Location Restraint
 */
export enum DepthRestraintType {
  UNRESTRAINED = 'UNRESTRAINED',
  FIXED_AT_DEPTH = 'FIXED_AT_DEPTH',
  FIXED_AT_SURFACE = 'FIXED_AT_SURFACE'
}
/**
 * ScalingFactorType in  Ellipse anbd Ellipsoid
 */
export enum ScalingFactorType {
  CONFIDENCE = 'CONFIDENCE',
  COVERAGE = 'COVERAGE',
  K_WEIGHTED = 'K_WEIGHTED'
}
