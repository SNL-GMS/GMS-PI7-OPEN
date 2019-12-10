import { CreationInfo, ProcessingContext } from '../common/model';
import { FeatureMeasurementTypeName, FeatureMeasurementValue } from '../signal-detection/model';
import { ProcessingChannel } from '../station/model';
import { LocationSolutionOSD } from './model-osd';
import { PhaseType } from '../channel-segment/model-spectra';

/**
 * Model definitions for the event-related data API
 */

/**
 * The enumerated status of an event
 */
// TODO In OSD, this class DNE
export enum EventStatus {
    ReadyForRefinement = 'ReadyForRefinement',
    OpenForRefinement = 'OpenForRefinement',
    AwaitingReview = 'AwaitingReview',
    Complete = 'Complete'
}
/**
 * Represents an event marking the occurrence of some transient
 * source of energy in the ground, oceans, or atmosphere
 */
export interface Event {
    id: string;
    monitoringOrganization: string;
    preferredEventHypothesisHistory: PreferredEventHypothesis[];
    finalEventHypothesisHistory?: EventHypothesis[];
    hypotheses: EventHypothesis[];
    status: EventStatus; // Not in OSD!
    activeAnalystUserNames: string[]; // Not in OSD!
    currentEventHypothesis: PreferredEventHypothesis;
    modified: boolean;
}

/**
 * Represents a proposed explanation for an event, such that the set of
 * event hypotheses grouped by an Event represents the history of that event.
 */
export interface EventHypothesis {
    id: string;
    eventId: string;
     // TODO is this actually a list of ids?
     parentEventHypotheses: string[];
     rejected: boolean;
    // This is a list of Sets. Each set (for now) will
    // contain types Depth, Surface and Unrestrained entries
    // For the OSD it is an array since there will only be
    // one set.
    locationSolutionSets: LocationSolutionSet[];
    preferredLocationSolution: PreferredLocationSolution;
    associations: SignalDetectionEventAssociation[];
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
 * The preferred hypothesis for the event at a given processing stage
 */
export interface PreferredEventHypothesis {
    processingStageId: string;
    eventHypothesis: EventHypothesis;
}

/**
 * Represents the linkage between Event Hypotheses and Signal Detection Hypotheses.
 * The rejected attribute is used to ensure that any rejected associations will not
 * be re-formed in subsequent processing stages.
 */
export interface SignalDetectionEventAssociation {
    id: string;
    signalDetectionHypothesisId: string;
    eventHypothesisId: string;
    rejected: boolean;
}

/**
 * Represents a final hypothesis for the event.
 */
export interface FinalEventHypothesis {
    eventHypothesis: EventHypothesis;
}

/**
 * Represents an estimate of the location of an event, defined as latitude, longitude, depth, and time.
 * A location solution is often determined by a location algorithm that minimizes the difference between
 * feature measurements (usually arrival time, azimuth, and slowness) and corresponding feature predictions.
 */
export interface LocationSolution {
    id: string;
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
 * Represents a Feature Prediction as part of the Location Solution. This should represent a 
 * predicted location of the event.
 */
export interface FeaturePrediction {
    id: string; // not in std data
    phase: string;
    featurePredictionComponents: FeaturePredictionComponent[]; /* Java declares as a set not sure in JSON */
    extrapolated: boolean;
    predictionType: FeatureMeasurementTypeName;
    sourceLocation: EventLocation;
    receiverLocation: Location;
    channelId?: string;
    stationId?: string;
    predictedValue?: FeatureMeasurementValue;
}

/**
 * Location Restraint for event Location Solution
 */
export interface LocationRestraint {
    depthRestraintType: DepthRestraintType;
    depthRestraintKm: number;
    latitudeRestraintType: RestraintType;
    latitudeRestraintDegrees: number;
    longitudeRestraintType: RestraintType;
    longitudeRestraintDegrees: number;
    timeRestraintType: RestraintType;
    timeRestraint: string;
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
 * Location Uncertainty for Location Solution
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
 * Ellipse for Location Solution
 */
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
/**
 * Ellipsoid for Location Solution
 */
export interface Ellipsoid {
    scalingFactorType: ScalingFactorType;
    kWeight: number;
    confidenceLevel: number;
    majorAxisLength: number;
    majorAxisTrend: number;
    majorAxisPlunge: number;
    intermediateAxisLength: number;
    intermediateAxisTrend: number;
    intermediateAxisPlunge: number;
    minorAxisLength: number;
    minorAxisTrend: number;
    minorAxisPlunge: number;
    depthUncertainty: number;
    timeUncertainty: string;
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

/**
 * Feature Prediction Component definition
 */
export interface FeaturePredictionComponent {
    value: DoubleValue;
    extrapolated: boolean;
    predictionComponentType: FeaturePredictionCorrectionType;
}

/**
 * Feature Prediction Corrections
 */
export interface FeaturePredictionCorrection {
    correctionType: FeaturePredictionCorrectionType;
    usingGlobalVelocity: boolean;
}

/**
 * Enumerated types for the Feature Prediction Corrections
 */
export enum FeaturePredictionCorrectionType {
    BASELINE_PREDICTION = 'BASELINE_PREDICTION',
    ELEVATION_CORRECTION = 'ELEVATION_CORRECTION',
    ELLIPTICITY_CORRECTION = 'ELLIPTICITY_CORRECTION'
}

/**
 * Double Value used in OSD common objects
 */
export interface DoubleValue {
    value: number;
    standardDeviation: number;
    units: Units;
}

/**
 * Units used in DoubleValue part of feature prediction
 */
export enum Units {
    DEGREES = 'DEGREES',
    SECONDS= 'SECONDS',
    SECONDS_PER_DEGREE = 'SECONDS_PER_DEGREE',
    UNITLESS = 'UNITLESS'
}

/**
 * Feature Prediction input definition for streaming call to compute FP
 */
export interface FeaturePredictionStreamingInput {
    featureMeasurementTypes: FeatureMeasurementTypeName[];
    sourceLocation: LocationSolutionOSD;
    receiverLocations: ProcessingChannel[];
    phase: string;
    model: string;
    corrections?: FeaturePredictionCorrection [];
    processingContext: ProcessingContext;
}

/**
 * Enumerated type of magnitude solution (surface wave, body wave, local, etc.)
 */
// TODO In OSD, this class DNE
export enum MagnitudeType {
    mb = 'mb',
    mbMLE = 'mbMLE',
    mbrel = 'mbrel',
    ms = 'ms',
    msMLE = 'msMLE',
    msVMAX = 'msVMAX',
    ml = 'ml'
}

/**
 * Represents an estimate of an event's magnitude based on detections from multiple stations.
 */
// TODO In OSD, this class DNE
export interface NetworkMagnitudeSolution {
    magnitudeType: MagnitudeType;
    magnitude: number;
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
 * Encapsulates a set of event field values to apply to an event.
 */
export interface UpdateEventInput {
    creatorId: string;
    processingStageId: string;
    status: EventStatus; // TODO: field DNE in OSD
    preferredHypothesisId: string;
    activeAnalystUserNames: string[]; // TODO: field DNE in OSD
}

/**
 * Enum of Event Location Algorithms
 */
export enum EventLocationAlgorithm {
    GeigersAlgorithm = 'Geigers',
    ApacheLmAlgorithm = 'ApacheLm'
}
/**
 * Input for locate event
 */
export interface LocateEventParameter {
    pluginInfo: {
        name: string;
        version: string;
    };
    eventLocationDefinition: EventLocationDefinition;
}

/**
 * Event Location Definition base definition (ApacheLM)
 */
export interface EventLocationDefinition {
    type: string;
    maximumIterationCount: number;
    convergenceThreshold: number;
    uncertaintyProbabilityPercentile: number;
    earthModel: string;
    applyTravelTimeCorrections: boolean;
    scalingFactorType: ScalingFactorType;
    kWeight: number;
    aprioriVariance: number;
    minimumNumberOfObservations: number;
    locationRestraints: LocationRestraint[];
}

/**
 * Geigers Event Location Definition. Additional fields to ApacheLm defintion
 */
export interface GeigersEventLocationDefinition extends EventLocationDefinition {
    dampingFactorStep: number;
    deltamThreshold: number;
    depthFixedIterationCount: number;
    lambda0: number;
    lambdaX: number;
    singularValueWFactor: number;
}
/**
 * Encapsulates input used to create a new event hypothesis.
 */
//
export interface CreateEventHypothesisInput {
    associatedSignalDetectionIds: string[];
    eventLocation: EventLocation;
    creatorId: string;
    processingStageId: string;
}

/**
 * Encapsulates input used to update an existing event hypothesis.
 */
export interface UpdateEventHypothesisInput extends CreateEventHypothesisInput {
    rejected: boolean;
}
/**
 * Bundles into a touple 
 */
export interface PreferredEventHypothesisHistoryAndHypothesis {
    preferredEventHypothesisHistory: PreferredEventHypothesis[];
    currentPrefEventHypo: PreferredEventHypothesis;
}
