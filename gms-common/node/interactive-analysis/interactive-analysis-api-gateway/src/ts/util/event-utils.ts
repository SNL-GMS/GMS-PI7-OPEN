import { cloneDeep } from 'lodash';
import { toEpochSeconds, toOSDTime, getDurationTime } from './time-utils';
import { EventHypothesis, LocationSolution, FeaturePrediction,
         PreferredLocationSolution, SignalDetectionSnapshot,
         EventSignalDetectionAssociationValues, LocationSolutionSet,
         SignalDetectionEventAssociation, EventLocation, LocationBehavior, Units } from '../event/model';
import { EventHypothesisOSD, LocationSolutionOSD,
         FeaturePredictionOSD } from '../event/model-osd';
import { getCreationInfo, getRandomLatLonOffset, getRandomLatitude,
         getRandomLongitude, getRandomResidual } from '../util/common-utils';
import { signalDetectionProcessor } from '../signal-detection/signal-detection-processor';
import { SignalDetection, SignalDetectionHypothesis,
         FeatureMeasurementTypeName,
         FeatureMeasurement,
         InstantMeasurementValue,
         InstantMeasurementValueOSD,
         FeatureMeasurementValue,
         NumericMeasurementValueOSD
        } from '../signal-detection/model';
import { findSlownessFeatureMeasurement, findAzimthFeatureMeasurement,
         findArrivalTimeFeatureMeasurement, findSlownessFeatureMeasurementValue,
         findAzimthFeatureMeasurementValue, findArrivalTimeFeatureMeasurementValue,
         findPhaseFeatureMeasurementValue } from './signal-detection-utils';
import { stationProcessor } from '../station/station-processor';
import * as uuid4 from 'uuid/v4';
import { ChannelSegmentProcessor } from '../channel-segment/channel-segment-processor';
import { gatewayLogger as logger } from '../log/gateway-logger';

/**
 * Convert the Location Solution returned from the OSD to API Gateway's Location Solution
 * @param locationSolutionOSD Location Solution OSD returned from OSD
 * @returns a converted LocationSolutionOSD -> LocationSolution
 */
export function convertLocationSolutionFromOSD(
    locationSolutionOSD: LocationSolutionOSD, associations: SignalDetectionEventAssociation[]): LocationSolution {
    const sourceLocation: LocationSolution = {
        ...locationSolutionOSD,
        location: {
            ...locationSolutionOSD.location,
            time: toEpochSeconds(locationSolutionOSD.location.time)
        },
        featurePredictions: convertFeaturePredictionsFromOSD(locationSolutionOSD.featurePredictions),
        snapshots: makeSignalDetectionSnapshots(associations, locationSolutionOSD.locationBehaviors)
    };
    return sourceLocation;
}

/**
 * Convert the Location Solution returned from the OSD to API Gateway's Location Solution
 * But with a empty array of snapshots to be filled in later
 * @param locationSolutionOSD Location Solution OSD returned from OSD
 * @returns a converted LocationSolutionOSD -> LocationSolution
 */
export function convertLocationSolutionFromOSDWithBlankSnapshots(
    locationSolutionOSD: LocationSolutionOSD): LocationSolution {
    const sourceLocation: LocationSolution = {
        ...locationSolutionOSD,
        location: {
            ...locationSolutionOSD.location,
            time: toEpochSeconds(locationSolutionOSD.location.time)
        },
        featurePredictions: convertFeaturePredictionsFromOSD(locationSolutionOSD.featurePredictions),
        snapshots: []
    };
    return sourceLocation;
}

/**
 * Takes a location solution without sd snapshots and populates them
 * @param locationSolutionOSD Location Solution OSD returned from OSD
 * @returns a converted LocationSolutionOSD -> LocationSolution
 */
export function hydrateSnapshotsInLocationSolution(
    locationSolution: LocationSolution, eventHyp: EventHypothesis): LocationSolution {
    const newLocation: LocationSolution = {
        ...locationSolution,
        snapshots: makeSignalDetectionSnapshots(eventHyp.associations, locationSolution.locationBehaviors)
    };
    return newLocation;
}

/**
 * Convert the Location Solution to OSD compatiable Location Solution
 * @param locationSolution Location Solution to send to OSD
 * @returns a converted LocationSolution -> LocationSolutionOSD
 */
export function convertLocationSolutionToOSD(locationSolution: LocationSolution): LocationSolutionOSD {
    const sourceLocationOSD: LocationSolutionOSD = {
        id: locationSolution.id,
        locationRestraint: locationSolution.locationRestraint,
        locationUncertainty: locationSolution.locationUncertainty,
        locationBehaviors: locationSolution.locationBehaviors,
        location: {
            ...locationSolution.location,
            time: toOSDTime(locationSolution.location.time)
        },
        featurePredictions: convertFeaturePredictionsToOSD(locationSolution.featurePredictions)
    };
    return sourceLocationOSD;
}

/**
 * Convert the Feature Predictions returned from the OSD to API Gateway's
 * @param fpOSDs FeaturePredictionOSD[] returned from OSD
 * @returns a converted FeaturePredictionOSD[] -> FeaturePrediction[]
 */
export function convertFeaturePredictionsFromOSD(fpOSDs: FeaturePredictionOSD[]): FeaturePrediction[] {
    if (!fpOSDs) {
        return [];
    }
    const fps: FeaturePrediction[] = fpOSDs.map(fpOSD => {
        // If the fpOSD Id is not set. Set it.
        if (fpOSD && !fpOSD.id) {
            fpOSD.id = uuid4().toString();
        }
        // Check if the predicted value is populated.
        // If not create one (sometimes the COI streaming endpoints will return null)
        // TODO: As part of adding Az and Slow to FP need to figure out what default values (or reject) when not set
        let predictedValue: FeatureMeasurementValue;
        if (fpOSD.predictionType === FeatureMeasurementTypeName.ARRIVAL_TIME) {
            const predictedValueOSD: InstantMeasurementValueOSD = fpOSD.predictedValue as InstantMeasurementValueOSD;
            predictedValue = !predictedValueOSD ? createDefaultFeatureMeasurementValue(fpOSD.predictionType) :
                {
                    value: predictedValueOSD.value ? toEpochSeconds(predictedValueOSD.value) : 0,
                    standardDeviation: predictedValueOSD.standardDeviation ?
                        getDurationTime(predictedValueOSD.standardDeviation) : 0
                };
        } else if (fpOSD.predictionType === FeatureMeasurementTypeName.SLOWNESS
            || fpOSD.predictionType === FeatureMeasurementTypeName.RECEIVER_TO_SOURCE_AZIMUTH
            || fpOSD.predictionType === FeatureMeasurementTypeName.SOURCE_TO_RECEIVER_AZIMUTH) {
            const predictedValueOSD: NumericMeasurementValueOSD =
                fpOSD.predictedValue as NumericMeasurementValueOSD;
            predictedValue = !predictedValueOSD ? createDefaultFeatureMeasurementValue(fpOSD.predictionType) :
                {
                    referenceTime: toEpochSeconds(predictedValueOSD.referenceTime),
                    measurementValue: predictedValueOSD.measurementValue
                };
        } else {
            logger.warn(`Got unexpected feature measurement type of: ${fpOSD.predictionType}`);
        }

        // Create the Feature Prediction with converted from OSD
        const station = stationProcessor.getStationByChannelId(fpOSD.channelId);
        const fp: FeaturePrediction = {
            ...fpOSD,
            stationId: station ? station.id : undefined,
            sourceLocation: {
                ...fpOSD.sourceLocation,
                time: toEpochSeconds(fpOSD.sourceLocation.time)
            },
            predictionType: fpOSD.predictionType,
            predictedValue
        };
        return fp;
    })
    .filter(fp => fp);
    if (!fps) {
        return [];
    }
    return fps;
}

/**
 * Creates a default Feature Measurement if none is returned from the service
 * @param predictionType FeatureMeasurementTypeName of feature prediction
 * @returns FeatureMeasurementValue based on the prediction type
 */
export function createDefaultFeatureMeasurementValue(
    predictionType: FeatureMeasurementTypeName): FeatureMeasurementValue {
    let predictedValue: FeatureMeasurementValue;
    if (predictionType === FeatureMeasurementTypeName.ARRIVAL_TIME) {
        predictedValue = {
            value: 0,
            standardDeviation: 0
        };
    } else if (predictionType === FeatureMeasurementTypeName.SLOWNESS
        || predictionType === FeatureMeasurementTypeName.RECEIVER_TO_SOURCE_AZIMUTH) {
        predictedValue = {
            referenceTime: 0,
            measurementValue: {
                value: 0,
                standardDeviation: 0,
                units: Units.UNITLESS
            }
        };
    }
    return predictedValue;
}

/**
 * Convert the Feature Predictions being sent to the OSD
 * @param fps FeaturePrediction[] to be sent to OSD
 * @returns a converted FeaturePrediction[] -> FeaturePredictionOSD[]
 */
export function convertFeaturePredictionsToOSD(
    sourceFPs: FeaturePrediction[]): FeaturePredictionOSD[] {
    if (!sourceFPs) {
        return [];
    }
    const fps = cloneDeep(sourceFPs);
    const fpOSDs: FeaturePredictionOSD[] = fps.map(fp => {
        const newFP: FeaturePredictionOSD = {
            ...fp,
            sourceLocation: {
                ...fp.sourceLocation,
                time: toOSDTime(fp.sourceLocation.time)
            },
            predictionType: fp.predictionType,
            predictedValue: {
                ...fp.predictedValue,
                value: toOSDTime((fp.predictedValue as InstantMeasurementValue).value)
            }
        };
        return newFP;
    // TODO fix to handle all types of FP Types
    }).filter(fp => fp.predictionType === FeatureMeasurementTypeName.ARRIVAL_TIME);
    return fpOSDs;
}

/**
 * Convert the Event Hypothesis to an OSD compatiable for sending as an argument
 * @param eventHypothesis to be sent to OSD
 * @returns a converted eventHypothesis -> eventHypothesisOSD
 */
export function convertEventHypothesisToOSD(eventHypothesis: EventHypothesis): EventHypothesisOSD {
    // Only convert the LocationSolution Set that the Preferred Location Solution points to
    // one of it's members i.e. (Depth, Surface or Unrestrained)
    // Call the util to find the correct set
    const locationSolutionSet = findPreferredLocationSolutionSet(eventHypothesis);
    const locationSolutionsOSD: LocationSolutionOSD[] =
        locationSolutionSet.locationSolutions.map(convertLocationSolutionToOSD);
    const eventHypOSD: EventHypothesisOSD = {
        id: eventHypothesis.id,
        rejected: eventHypothesis.rejected,
        eventId: eventHypothesis.eventId,
        parentEventHypotheses: eventHypothesis.parentEventHypotheses ?  eventHypothesis.parentEventHypotheses : [],
        associations: eventHypothesis.associations,
        locationSolutions: locationSolutionsOSD,
        preferredLocationSolution: {
            locationSolution: locationSolutionsOSD.
            find(ls => ls.id === eventHypothesis.preferredLocationSolution.locationSolution.id)
        }
    };
    return eventHypOSD;
}

/**
 * Converts event hypothesis from OSD to gateway version
 * @param eventHypothesisOSD event hypothesis in OSD form
 * @returns EventHypthesis in gateway form
 */
export function convertEventHypothesisFromOSD(eventHypothesisOSD: EventHypothesisOSD): EventHypothesis {
    // Convert all the location solutions from OSD to API Gateway
    const locationSolutionsDehydrated: LocationSolution[] =
        eventHypothesisOSD.locationSolutions.map(convertLocationSolutionFromOSDWithBlankSnapshots);
    // Now set the Preferred to point at the correct (newly converted) Location Solution
    const prefLocSolutionId = eventHypothesisOSD.preferredLocationSolution.locationSolution.id;
    const preferredLocationSolutionDehydrated: PreferredLocationSolution = {
        locationSolution: findPrefLocSolution(prefLocSolutionId, locationSolutionsDehydrated),
        creationInfo: getCreationInfo('1')
    };
    const eventHyp: EventHypothesis = {
        id: eventHypothesisOSD.id,
        rejected: eventHypothesisOSD.rejected,
        eventId: eventHypothesisOSD.eventId,
        parentEventHypotheses: eventHypothesisOSD.parentEventHypotheses,
        associations: eventHypothesisOSD.associations,
        locationSolutionSets: [],
        preferredLocationSolution: preferredLocationSolutionDehydrated
    };
    // After the event hypothesis has been created, we can add the snapshots back in
    const locationSolutions =
        locationSolutionsDehydrated.map(lsD => hydrateSnapshotsInLocationSolution(lsD, eventHyp));
    const preferredLocationSolution: PreferredLocationSolution = {
        locationSolution: findPrefLocSolution(prefLocSolutionId, locationSolutions),
        creationInfo: getCreationInfo('1')
    };
    eventHyp.preferredLocationSolution = preferredLocationSolution;
    const newSets = createLocationSolutionSet(eventHyp, locationSolutions);
    eventHyp.locationSolutionSets.push(newSets);
    return eventHyp;
 }

/**
 * Find the list of location solutions pointed to by the Preferred Location Solution
 * @param eventHypothesis Which contains the Location Solution Sets (Surface, Depth, Unrestrained)
 * @returns A single set (list of Location Solutions) pointed to bye Preferred Location Solution
 */
export function findPreferredLocationSolutionSet(eventHypothesis: EventHypothesis): LocationSolutionSet {
    const preferredLSId = eventHypothesis.preferredLocationSolution.locationSolution.id;
    let locationSolutionSet: LocationSolutionSet;
    eventHypothesis.locationSolutionSets.forEach(lsSet => {
        if (lsSet.locationSolutions.find(ls => ls.id === preferredLSId) !== undefined) {
            locationSolutionSet = lsSet;
        }
    });
    return locationSolutionSet;
 }

/**
 * Find the Preferred Location Solution in EventHypothesis
 * @param eventHypothesis Which contains the Preferred Location Solution
 * @returns A single location solution pointed to by the preferred location solution id
 */
export function findPrefLocSolutionUsingEventHypo(eventHypothesis: EventHypothesis):
    LocationSolution | undefined {
    if (!eventHypothesis) {
        return undefined;
    }
    const lsSet = findPreferredLocationSolutionSet(eventHypothesis);
    return findPrefLocSolution(eventHypothesis.preferredLocationSolution.locationSolution.id, lsSet.locationSolutions);
}

/**
 * Find the Preferred Location Solution in Location Solution Set
 * @param locationSolutionSet Which contains the Location Solution list of (Surface, Depth, Unrestrained)
 * @returns A single location solution pointed to by the preferred location solution id
 */
export function findPrefLocSolution(prefLocSolutionId: string, locSolutions: LocationSolution[]):
    LocationSolution | undefined {
    if (!prefLocSolutionId || !locSolutions) {
        return undefined;
    }
    return locSolutions.find(ls => ls.id === prefLocSolutionId);
 }

/**
 * Creates a location solution set
 * @param eventHyp event hypothesis
 * @param lsList list of location solutions 
 * @return location solution set
 */
export function createLocationSolutionSet(eventHyp: EventHypothesis, lsList: LocationSolution[]): LocationSolutionSet {
    return {
         id: uuid4().toString(),
         count: eventHyp.locationSolutionSets.length,
         locationSolutions: lsList,
     };
 }

 /**
  * Creates snapshots of the current sd associations for the passed in event hypothesis
  * This populates the location solution history used by the Event Location Solution UI
  * @param eventHypo event hypothesis
  * @returns snapshots of the associations
  */
export function makeSignalDetectionSnapshots(associations: SignalDetectionEventAssociation[],
                                             locationBehaviors: LocationBehavior[]): SignalDetectionSnapshot[] {
    const validAssociations = associations.filter(assoc => {
        const sd: SignalDetection =
        signalDetectionProcessor.getSignalDetectionByHypoId(assoc.signalDetectionHypothesisId);
        return !assoc.rejected && sd;
    });
    const snapshots = validAssociations.map(sdAssoc =>
        createSignalDetectionSnapshot(locationBehaviors, sdAssoc));
    return snapshots.filter(snap => snap !== undefined && snap !== null);
}

/**
 * Creates snapshots of the current sd associations for the passed in event hypothesis
 * This populates the location solution history used by the Event Location Solution UI
 * @param eventHypo event hypothesis
 * @returns snapshots of the associations
 */
export function createSignalDetectionSnapshot(
    locationBehaviors: LocationBehavior[], sdAssoc: SignalDetectionEventAssociation):
    SignalDetectionSnapshot {

    const sd = signalDetectionProcessor.getSignalDetectionByHypoId(sdAssoc.signalDetectionHypothesisId);
    const sdHyp = signalDetectionProcessor.getSignalDetectionHypothesisById(sdAssoc.signalDetectionHypothesisId);
    if (!sdAssoc.rejected && sdHyp) {
        const slowFmVal = findSlownessFeatureMeasurementValue(sdHyp.featureMeasurements);
        const azFmVal = findAzimthFeatureMeasurementValue(sdHyp.featureMeasurements);
        const arrivalFmVal = findArrivalTimeFeatureMeasurementValue(sdHyp.featureMeasurements);
        const channelName = getChannelName(sdHyp);
        return {
            signalDetectionId: sdHyp.parentSignalDetectionId,
            signalDetectionHypothesisId: sdHyp.id,
            stationId: sd.stationId,
            stationName: stationProcessor.getStationById(sd.stationId).name,
            channelName,
            phase: findPhaseFeatureMeasurementValue(sdHyp.featureMeasurements).phase,
            time: getAssociationValues(FeatureMeasurementTypeName.ARRIVAL_TIME,
                                       sdHyp.featureMeasurements, arrivalFmVal.value, locationBehaviors),
            slowness: getAssociationValues(FeatureMeasurementTypeName.SLOWNESS, sdHyp.featureMeasurements,
                                           slowFmVal.measurementValue.value, locationBehaviors),
            azimuth: getAssociationValues(FeatureMeasurementTypeName.RECEIVER_TO_SOURCE_AZIMUTH,
                                          sdHyp.featureMeasurements, azFmVal.measurementValue.value, locationBehaviors)
        };
    }
    return undefined;
}

/**
 * Helper function to set the Azimiuth values in the new SD Table Row
 * @param row LocationSDRow to populate
 * @param sd Signal Detection to find location behavior to populate from Slowness
 * @param locationBehaviors LocationBehavors list from current selected event
 */
export function getAssociationValues(fmType: FeatureMeasurementTypeName, fms: FeatureMeasurement[],
                                     fmValue: number, locationBehaviors: LocationBehavior[]):
                                     EventSignalDetectionAssociationValues {
    let fm: FeatureMeasurement;
    if (fmType === FeatureMeasurementTypeName.ARRIVAL_TIME) {
        fm = findArrivalTimeFeatureMeasurement(fms);
    } else if (fmType === FeatureMeasurementTypeName.SLOWNESS) {
        fm = findSlownessFeatureMeasurement(fms);
    } else if (fmType === FeatureMeasurementTypeName.SOURCE_TO_RECEIVER_AZIMUTH ||
               fmType === FeatureMeasurementTypeName.RECEIVER_TO_SOURCE_AZIMUTH) {
        fm = findAzimthFeatureMeasurement(fms);
    }
    const locBehavior = locationBehaviors.find(lb => lb.featureMeasurementId === fm.id);
    return {
        defining: locBehavior && locBehavior.defining ? locBehavior.defining : false,
        observed: fmValue,
        correction: undefined,
        residual: locBehavior && locBehavior.residual ? locBehavior.residual : undefined
    };
}

/**
 * Helper function to set the Azimiuth values in the new SD Table Row
 * @param row LocationSDRow to populate
 * @param sd Signal Detection to find location behavior to populate from Slowness
 * @param locationBehaviors LocationBehavors list from current selected event
 */
export function getChannelName(sdHyp: SignalDetectionHypothesis): string {
    const arrivalTimeFM = findArrivalTimeFeatureMeasurement(sdHyp.featureMeasurements);
    if (arrivalTimeFM) {
        const segment = ChannelSegmentProcessor.Instance().getInCacheChannelSegmentById(arrivalTimeFM.channelSegmentId);
        if (segment) {
            return segment.name;
        }
    }
    return 'fkb';
}

/**
 * Create a new Event Location that simulates movement for create event and
 * locate event (called from Mock Backend)
 * TODO: move to mock backend when Create Event no longer needs it.
 * @param sdHypIds ids of sds that will be associated to the event
 * @param startTime of event to set in EventLocation
 * @returns A new EventLocation with some random movement simulated
 */
export function getNewRandomEventLocation(sdHypIds: string[], startTime: number): EventLocation {
    let eventLocation: EventLocation;
    const defaultEventDepth = 10;
    const defaultTimeOffset = 30;

    // If there are associations, the event location should be set to the earliest arriving SD
    if (sdHypIds.length > 0) {
        // Loop through all associations and find the earliest arriving SD
        let earliestArrivalHyp = signalDetectionProcessor.getSignalDetectionHypothesisById(
            sdHypIds[0]);
        sdHypIds.forEach(hypId => {
            const sdHyp = signalDetectionProcessor.getSignalDetectionHypothesisById(hypId);
            if (sdHyp) {
                if (findArrivalTimeFeatureMeasurementValue(sdHyp.featureMeasurements).value <
                    findArrivalTimeFeatureMeasurementValue(earliestArrivalHyp.featureMeasurements).value) {
                        earliestArrivalHyp = sdHyp;
                    }
                }
        });

        // Copy the location from the station, and set event time as the arrival time
        const earliestArrivingSd =
            signalDetectionProcessor.getSignalDetectionById(earliestArrivalHyp.parentSignalDetectionId);
        const station = stationProcessor.getStationById(earliestArrivingSd.stationId);
        const arrivalTimeEpoch = findArrivalTimeFeatureMeasurementValue(
            earliestArrivalHyp.featureMeasurements).value;
        const latLon = getRandomLatLonOffset();
        eventLocation = {
            latitudeDegrees: station.location.latDegrees + latLon.lat,
            longitudeDegrees: station.location.lonDegrees + latLon.lon,
            depthKm: defaultEventDepth,
            time: arrivalTimeEpoch - defaultTimeOffset
        };
    } else {
        // No associations found - randomize the lat/lon and set time to the start of the interval
        eventLocation = {
            latitudeDegrees: getRandomLatitude(),
            longitudeDegrees: getRandomLongitude(),
            depthKm: defaultEventDepth,
            time: startTime + 1
        };
    }
    return eventLocation;
}

/**
 * Maps location behaviors and produces random residual values
 * @param locationBehaviors
 * @returns a location behavior[]
 */
export function randomizeResiduals(locationBehaviors: LocationBehavior[]): LocationBehavior[] {
    return locationBehaviors.map(lb => ({
        ...lb,
        residual: getRandomResidual()
    }));
}
