import { PubSub } from 'graphql-subscriptions';
import * as config from 'config';
import * as model from './model';
import { stationProcessor } from '../station/station-processor';
import { signalDetectionProcessor } from './signal-detection-processor';
import { CreationInfo, DetectionAndAssociationChange, AssociationChange } from '../common/model';
import { findPhaseFeatureMeasurementValue,
         findArrivalTimeFeatureMeasurementValue } from '../util/signal-detection-utils';
import { eventProcessor } from '../event/event-processor';
import { ChannelSegmentProcessor } from '../channel-segment/channel-segment-processor';
import { ChannelSegmentType } from '../channel-segment/model';
import { PhaseType } from '../channel-segment/model-spectra';
import { performanceLogger } from '../log/performance-logger';
import { gatewayLogger } from '../log/gateway-logger';

/**
 * Resolvers for the signal detection API gateway
 */

// Create the publish/subscribe API for GraphQL subscriptions
export const pubsub = new PubSub();

// Load configuration settings
const settings = config.get('signalDetection');

// GraphQL Resolvers
export const resolvers = {
    // Query resolvers
    Query: {
        signalDetectionsByDefaultStations: async (_, { timeRange }) =>
            signalDetectionProcessor.getSignalDetectionsForDefaultStations(timeRange),
        signalDetectionsByStation: async (_, { stationIds, timeRange }) =>
        signalDetectionProcessor.getSignalDetectionsByStation(stationIds, timeRange),
        signalDetectionsById: async (_, { detectionIds }) =>
            signalDetectionProcessor.getSignalDetectionsById(detectionIds),
        signalDetectionsByEventId: async (_, { eventId }) =>
            signalDetectionProcessor.getSignalDetectionsByEventId(eventId),
        loadSignalDetectionsByStation: async (_, { stationIds, timeRange }) =>
            stationIds ?
                signalDetectionProcessor.loadSignalDetections(timeRange, stationIds) :
                signalDetectionProcessor.getSignalDetectionsForDefaultStations(timeRange)
    },

    // Mutation resolvers
    Mutation: {
        // Create a new signal detection
        createDetection: async (_, { input }) => {
            performanceLogger.performance('createDetection', 'enteringResolver');
            // Create & store the hypothesis, detection & feature measurement
            const detection: model.SignalDetection = signalDetectionProcessor.createDetection(input);
            let associationChange: AssociationChange;
            // Publish the newly-created signal detection to the subscription channel
            if (input.eventId && detection.currentHypothesis) {
                const event = await eventProcessor.getEventById(input.eventId);
                associationChange = eventProcessor.changeSignalDetectionAssociations(
                    event.currentEventHypothesis.eventHypothesis.id, [detection.currentHypothesis.id], true);
                pubsub.publish(
                    settings.subscriptions.channels.detectionsCreated,
                    { detectionsCreated: [detection] }).catch(e => gatewayLogger.warn(e));
            } else {
                associationChange = {
                    events: [],
                    sds: [detection]
                };
            }
            performanceLogger.performance('createDetection', 'leavingResolver');
            return associationChange;
        },

        // Update an existing signal detection
        updateDetection: async (_, { detectionId, input }) => {
            // Update the hypothesis
            const detectionsAndChanges: DetectionAndAssociationChange =
                signalDetectionProcessor.updateDetection(detectionId, input);
            return detectionsAndChanges.associationChange;
        },
        // Update a collection of existing signal detections
        updateDetections: async (_, { detectionIds, input }) => {
            // Update the hypothesis
            performanceLogger.performance('updateDetections', 'enteringResolver');
            const detectionsAndChanges: DetectionAndAssociationChange =
                signalDetectionProcessor.updateDetections(detectionIds, input);
            performanceLogger.performance('updateDetections', 'leavingResolver');
            return detectionsAndChanges.associationChange;
        },
        // Reject a collection of existing signal detections
        // tslint:disable-next-line:arrow-return-shorthand
        rejectDetections: async (_, { detectionIds }) =>
            // Update the detections with the reject
            signalDetectionProcessor.rejectDetections(detectionIds)
    },

    // Subscription Resolvers
    Subscription: {
        // Subscription for newly-created signal detection hypotheses
        detectionsCreated: {
            // Set up the subscription to filter results down to those detections that overlap
            // a time range provided by the subscriber upon creating the subscription
            subscribe:
                async () => pubsub.asyncIterator(settings.subscriptions.channels.detectionsCreated)
        }
    },

    // Field resolvers for SignalDetection
    SignalDetection: {
        station: async (signalDetection: model.SignalDetection) =>
            stationProcessor.getStationById(signalDetection.stationId),
        // dirty: async (sd: model.SignalDetection) => sd.modified || sd.associationModified,
        creationInfo: async (creationInfoId: string): Promise<CreationInfo> =>
            signalDetectionProcessor.getCreationInfo(creationInfoId),
        hasConflict: async (signalDetection: model.SignalDetection) =>
            eventProcessor.checkForSDHypConflict(signalDetection.currentHypothesis.id),
        signalDetectionHypothesisHistory: async (signalDetection: model.SignalDetection):
            Promise<model.SignalDetectionHypothesisHistory[]> =>
            signalDetection.signalDetectionHypotheses.map(sdh => {
                const arrivalTimeFMValue = findArrivalTimeFeatureMeasurementValue(sdh.featureMeasurements);
                if (!arrivalTimeFMValue) {
                    return;
                }
                const phaseFMValue = findPhaseFeatureMeasurementValue(sdh.featureMeasurements);
                if (!phaseFMValue) {
                    phaseFMValue.phase = PhaseType.P;
                }
                return {
                    id: sdh.id,
                    phase: findPhaseFeatureMeasurementValue(sdh.featureMeasurements).phase,
                    rejected: sdh.rejected,
                    arrivalTimeSecs: findArrivalTimeFeatureMeasurementValue(sdh.featureMeasurements).value,
                    arrivalTimeUncertainty:
                        findArrivalTimeFeatureMeasurementValue(sdh.featureMeasurements).standardDeviation,
                    creationInfoId: sdh.creationInfoId
                };
            })
    },

    // SignalDetectionHypothesisHistory resolver for CreatinInfo
    // TODO: Need to fix creation info as a global resolver not each individual...
    SignalDetectionHypothesisHistory: {
        creationInfo: async (creationInfoId: string): Promise<CreationInfo> =>
            signalDetectionProcessor.getCreationInfo(creationInfoId),
    },

    // Field resolvers for SignalDetectionHypothesis
    SignalDetectionHypothesis: {
        signalDetection: async (hypothesis: model.SignalDetectionHypothesis) =>
            signalDetectionProcessor.getSignalDetectionById(hypothesis.parentSignalDetectionId),
        creationInfo: async (creationInfoId: string) =>
            signalDetectionProcessor.getCreationInfo(creationInfoId)
    },

    // Field resolvers for Azimuth FeatureMeasurement to populate the Fk Data
    FeatureMeasurement: {
        channelSegment: async (fm: model.FeatureMeasurement) => {
            // Only populate Beam and FkPowerSpectra channel segments,
            // which come from FeatureMeasurementTypes RECEIVER_TO_SOURCE_AZIMUTH or ARRIVAL_TIME
            if (fm.channelSegmentId && (fm.featureMeasurementType ===
                model.FeatureMeasurementTypeName.RECEIVER_TO_SOURCE_AZIMUTH ||
                fm.featureMeasurementType ===
                model.FeatureMeasurementTypeName.ARRIVAL_TIME) ||
                fm.featureMeasurementType ===
                model.FeatureMeasurementTypeName.FILTERED_BEAM) {
                const sd = signalDetectionProcessor.getSignalDetectionByFmId(fm.id);
                const channelSegment =
                    await ChannelSegmentProcessor.Instance().getChannelSegment(fm.channelSegmentId, sd);
                if (channelSegment) {
                    // Make sure the channel segment is the correct type for FM
                    if (fm.featureMeasurementType === model.FeatureMeasurementTypeName.RECEIVER_TO_SOURCE_AZIMUTH &&
                        channelSegment.type === ChannelSegmentType.FK_SPECTRA) {

                        // Clone the channel segment to not return the FkSpectrum list,
                        // but instead return the lead and peak spectrum
                        const timeseries = channelSegment.timeseries.map(ts =>
                            ({
                                ...ts,
                                spectrums: []
                            }));
                        const clonedCS = {
                            ...channelSegment,
                            timeseries
                        };
                        return clonedCS;
                    } else if (fm.featureMeasurementType === model.FeatureMeasurementTypeName.ARRIVAL_TIME &&
                        (channelSegment.type === ChannelSegmentType.FK_BEAM ||
                         channelSegment.type === ChannelSegmentType.DETECTION_BEAM)) {
                        return channelSegment;
                    } else if (fm.featureMeasurementType === model.FeatureMeasurementTypeName.FILTERED_BEAM &&
                        (channelSegment.type === ChannelSegmentType.FK_BEAM ||
                            channelSegment.type === ChannelSegmentType.FILTER)) {
                        return channelSegment;
                    }
                }
            }
            return undefined;
        },
        creationInfo: async (creationInfoId: string) =>
            signalDetectionProcessor.getCreationInfo(creationInfoId),
        measurementValue: async (fm: model.FeatureMeasurement) => fm.measurementValue
    },

    // Field resolvers for FeatureMeasurementValue
    FeatureMeasurementValue: {
        /**
         * Special interface resolver to determine the implementing type based on field content
         */
        __resolveType(obj, context, info) {
            if (obj) {
                if (obj.startTime !== undefined && obj.period !== undefined && obj.amplitude !== undefined) {
                    return 'AmplitudeMeasurementValue';
                } else if (obj.value !== undefined && obj.standardDeviation !== undefined) {
                    return 'InstantMeasurementValue';
                } else if (obj.referenceTime !== undefined && obj.measurementValue !== undefined) {
                    return 'NumericMeasurementValue';
                } else if (obj.phase !== undefined && obj.confidence !== undefined) {
                    return 'PhaseTypeMeasurementValue';
                } else if (obj.strValue !== undefined) {
                    return 'StringMeasurementValue';
                }
            }
            return undefined;
        }
    }
};
