import { PubSub } from 'graphql-subscriptions';
import * as config from 'config';
import { max } from 'lodash';
import * as model from './model';
import { signalDetectionProcessor } from '../signal-detection/signal-detection-processor';
import { SignalDetectionHypothesis } from '../signal-detection/model';
import { workflowProcessor } from '../workflow/workflow-processor';
import { eventProcessor } from './event-processor';
import { findArrivalTimeFeatureMeasurementValue } from '../util/signal-detection-utils';
import { AssociationChange, EventAndAssociationChange } from '../common/model';
import { performanceLogger } from '../log/performance-logger';

/**
 * Resolvers for the event API gateway
 */

// Create the publish/subscribe API for GraphQL subscriptions
export const pubsub = new PubSub();

// Load configuration settings
const settings = config.get('event');

// GraphQL Resolvers
export const resolvers = {

    // Query resolvers
    Query: {
        eventsInTimeRange: async (_, { timeRange }) =>
            await eventProcessor.getEventsInTimeRange(timeRange),

        eventById: async (_, {eventId}) =>
            await eventProcessor.getEventById(eventId)
    },

    // Mutation resolvers
    Mutation: {
        createEvent: async (_, { signalDetectionHypoIds }): Promise<EventAndAssociationChange> => {
            // Update the event directly
            const eventAndChanges = eventProcessor.createEvent(signalDetectionHypoIds);
            // Publish the updated event and sds to the subscription channel
            await pubsub.publish(
                settings.subscriptions.channels.eventsCreated,
                { eventsCreated: [eventAndChanges.event] });
            return eventAndChanges;
        },
        // Update an existing event (without creating a new event hypothesis)
        updateEvents: async (_, { eventIds, input }) => {
            // Update the event directly
            performanceLogger.performance('updateEvents', 'enteringResolver');

            const events: model.Event[] =
                await eventProcessor.updateEvents(eventIds, input);
            performanceLogger.performance('updateEvents', 'leavingResolver');

            return events;
        },

        // TODO: What is this for (currently not used in UI)?
        // TODO: Future use?
        // Lookup (call streaming service) the feature predictions by Event Id
        updateFeaturePredictions: async (_, {eventId}) => {
            const event = await eventProcessor.getEventById(eventId);
            if (event) {
                return await eventProcessor.computeFeaturePredictions(event);
            }
            return undefined;
        },

        // Mutation to (un)associate Signal Detections to event hypothesis. Returns the updated event
        changeSignalDetectionAssociations: async (_, { eventHypothesisId, signalDetectionHypoIds, associate }):
            Promise<AssociationChange> => eventProcessor.changeSignalDetectionAssociations(
            eventHypothesisId, signalDetectionHypoIds, associate),

        // Mutation to Locate Event
        locateEvent: async (_, { eventHypothesisId,  preferredLocationSolutionId, locationBehaviors }):
            Promise<model.EventHypothesis> => eventProcessor.locateEvent(
            eventHypothesisId,  preferredLocationSolutionId, locationBehaviors)
    },

    // Subscription Resolvers
    Subscription: {
            // Subscription for events created
            eventsCreated: {
                subscribe: async () => pubsub.asyncIterator(settings.subscriptions.channels.eventsCreated)
            }
    },

    // Field resolvers for Event
    Event: {
        hypotheses: async (event: model.Event) =>
            eventProcessor.getHypothesesForEvent(event.id),

        preferredHypothesisForStage: async (event: model.Event, { stageId }) =>
            eventProcessor.getPreferredHypothesisForStage(event.id, stageId),

        currentEventHypothesis: async (event: model.Event) =>
            eventProcessor.getCurrentEventHypothesisByEventId(event.id),

        activeAnalysts: async (event: model.Event) =>
             workflowProcessor.getAnalysts(event.activeAnalystUserNames),

        hasConflict: async (event: model.Event) =>
            eventProcessor.checkForEventConflict(event),

        conflictingSdHypIds: async (event: model.Event) =>
            eventProcessor.getConflictingSdHyps(event)
    },

    // Field resolvers for PreferredEventHypothesis
    PreferredEventHypothesis: {
        processingStage: async (preferredHypothesis: model.PreferredEventHypothesis) =>
            workflowProcessor.getStage(preferredHypothesis.processingStageId),
    },

    // Field resolvers for EventHypothesis
    EventHypothesis: {
        event: async (hypothesis: model.EventHypothesis) =>
            eventProcessor.getEventById(hypothesis.eventId),
        signalDetectionAssociations: async (hypothesis: model.EventHypothesis) =>
            hypothesis.associations,
        associationsMaxArrivalTime: async (hypothesis: model.EventHypothesis) => {
            // Get the associations and before returning set the max arrival time
            const associations: model.SignalDetectionEventAssociation[] = hypothesis.associations;
            // If not defined or empty return
            if (!associations || associations.length === 0) {
                return 0;
            }
            const detectionTimes: number[] = associations
                .filter(association => !association.rejected)
                .map(association => {
                    // Lookup the SD Hypothesis from the association's SDH id
                    const sdHypo: SignalDetectionHypothesis = signalDetectionProcessor.
                        getSignalDetectionHypothesisById(association.signalDetectionHypothesisId);
                    if (!sdHypo || !sdHypo.featureMeasurements) {
                        return undefined;
                    }
                    // Find all the arrival time featuremeasurement times
                    const arrivalTimeMeasurementValue =
                        findArrivalTimeFeatureMeasurementValue(sdHypo.featureMeasurements);
                    if (!arrivalTimeMeasurementValue || !arrivalTimeMeasurementValue.value) {
                        return undefined;
                    }
                    return arrivalTimeMeasurementValue.value;
                });

            // Set max arrival time value in the Event Hypo before returning
            if (detectionTimes && detectionTimes.length <= 0) {
                return 0;
            }
            const maxDetectionTime = max(detectionTimes);
            if (maxDetectionTime === undefined || maxDetectionTime === null || isNaN(maxDetectionTime)) {
                return 0;
            }
            return maxDetectionTime;
        },
    },

    // Field resolvers for SignalDetectionEventAssociation
    SignalDetectionEventAssociation: {
        signalDetectionHypothesis: async (association: model.SignalDetectionEventAssociation) => {
            const sdHypo =  signalDetectionProcessor.getSignalDetectionHypothesisById(
                association.signalDetectionHypothesisId);
            return sdHypo;
        },
        // eventHypothesis: async (association: model.SignalDetectionEventAssociation, timeRange) =>
        //     eventProcessor.getEventHypothesisById(association.eventHypothesisId)
    },

    // Field resolvers for LocationSolution
    LocationSolution: {
        locationType: async () => 'standard'
    }
};
