import { SignalDetectionEventAssociation,
         LocationRestraint, LocationUncertainty,
         LocationBehavior, FeaturePredictionComponent } from './model';
import { FeatureMeasurementTypeName, FeatureMeasurementValue } from '../signal-detection/model';

/**
 * OSD Representation of a potential seismic event
 */
export interface EventOSD {
    id: string;
    rejectedSignalDetectionAssociations: any[];
    monitoringOrganization: string;
    hypotheses: EventHypothesisOSD[];
    finalEventHypothesisHistory?: FinalEventHypothesisOSD[];
    preferredEventHypothesisHistory: PreferredEventHypothesisOSD[];
}
/**
 * OSD Representation of a finalized hypothesis. OSD only returns id.
 * Not going to make FinalEventHypothesis' fields optional 
 */
export interface FinalEventHypothesisOSD {
    eventHypothesisId: string;
}
/**
 * OSD Representation of a preferred event hypothesis. 
 * API Gateway model is not a claim check for the eventHypothesis
 */
export interface PreferredEventHypothesisOSD {
    eventHypothesisId: string;
    processingStageId: string;
}
/**
 * OSD representation of a proposed explanation for an event, such that the set of
 * event hypotheses grouped by an Event represents the history of that event.
 */
export interface EventHypothesisOSD {
    id: string;
    rejected: boolean;
    eventId: string;
    // TODO is this actually a list of ids?
    parentEventHypotheses: string[];
    locationSolutions: LocationSolutionOSD[];
    preferredLocationSolution: PreferredLocationSolutationOSD;
    associations: SignalDetectionEventAssociation[];
}

/**
 * OSD Event Location definition (the time field is a string not number)
 */
export interface EventLocationOSD {
    latitudeDegrees: number;
    longitudeDegrees: number;
    depthKm: number;
    time: string;
}

/**
 * OSD Preferred Location Solution
 */
export interface PreferredLocationSolutationOSD {
    locationSolution: LocationSolutionOSD;
}

/**
 * LocationSolutionOSD OSD returns Location Solution representation
 */
export interface LocationSolutionOSD {
    id: string;
    location: EventLocationOSD;
    featurePredictions: FeaturePredictionOSD[];
    locationRestraint: LocationRestraint;
    locationUncertainty?: LocationUncertainty;
    locationBehaviors: LocationBehavior [];
}

/**
 * FeaturePredictionOSD OSD returns representation
 */
export interface FeaturePredictionOSD {
    id: string; // not in std data
    phase: string;
    featurePredictionComponents: FeaturePredictionComponent[]; /* Java declares as a set not sure in JSON */
    extrapolated: boolean;
    predictionType: FeatureMeasurementTypeName;
    sourceLocation: EventLocationOSD;
    receiverLocation: Location;
    channelId?: string;
    stationId?: string;
    predictedValue?: FeatureMeasurementValue;
}
