import { PubSub } from 'graphql-subscriptions';
import * as config from 'config';
import { gatewayLogger as logger, gatewayLogger } from '../log/gateway-logger';
import * as model from './model';
import { workflowProcessor } from './workflow-processor';
import { signalDetectionProcessor } from '../signal-detection/signal-detection-processor';
import { performanceLogger } from '../log/performance-logger';
import { AssociationChange } from '../common/model';
// Create the publish/subscribe API for GraphQL subscriptions
export const pubsub = new PubSub();

/**
 * Resolvers for the Workflow User Interface API
 */

// Load subscription configuration settings
const subConfig = config.get('workflow.subscriptions');

/**
 * Publish newly-created ProcessingStageIntervals to the GraphQL subscription channel
 * and store them in the canned data list.
 * @param stageInterval 
 */
export function stageIntervalCreated(stageInterval: model.ProcessingStageInterval) {

    logger.info(`Publishing newly-created ProcessingStageInterval with ID: ${stageInterval.id} to GraphQL subscribers`);

    // Publish the new stage interval to the subscription channel
    pubsub.publish(subConfig.channels.stageIntervalCreated, { stageIntervalCreated: stageInterval })
        .catch(e => gatewayLogger.warn(e));
}

/**
 * Publish newly-created ProcessingIntervals to the GraphQL subscription channel
 * and store them in the canned data list.
 * @param interval 
 */
export function intervalCreated(interval: model.ProcessingInterval) {

    logger.info(`Publishing newly-created ProcessingInterval with ID: ${interval.id} to GraphQL subscribers`);

    // Publish the new interval to the subscription channel
    pubsub.publish(subConfig.channels.intervalCreated, { intervalCreated: interval })
        .catch(e => gatewayLogger.warn(e));
}

// GraphQL Resolvers
logger.info('Creating GraphQL resolvers for the workflow API...');
export const resolvers = {

    // Query resolvers
    Query: {
        analysts: async () => workflowProcessor.getAllAnalysts(),
        stages: async () => workflowProcessor.getStages(),
        stage: async (_, { id }) => workflowProcessor.getStage(id),
        intervalsInRange: async (_, { timeRange }) =>
            workflowProcessor.getIntervalsInRange(timeRange.startTime, timeRange.endTime),
        interval: async (_, { id }) => workflowProcessor.getInterval(id),
        stageIntervals: async () => workflowProcessor.getStageIntervals(),
        stageInterval: async (_, { id }) => workflowProcessor.getStageInterval(id),
        activities: async () => workflowProcessor.getActivities(),
        activity: async (_, { id }) => workflowProcessor.getActivity(id),
        activityIntervals: async () => workflowProcessor.getActivityIntervals(),
        activityInterval: async (_, { id }) => workflowProcessor.getActivityInterval(id),
        stageIntervalsInRange: async (_, { timeRange }) =>
            workflowProcessor.getStageIntervalsInRange(timeRange.startTime, timeRange.endTime),
    },

    // Mutation resolvers
    Mutation: {
        // Mark the processing stage interval, updating the status
        markStageInterval: async (_, { stageIntervalId, input }) => {
            // Apply the marking input to the processing stage with the provided ID
            const stageInterval = await workflowProcessor.markStageInterval(stageIntervalId, input);
            // Publish the updated stages
            pubsub.publish(subConfig.channels.stagesChanged, { stagesChanged: await workflowProcessor.getStages() })
                .catch(e => gatewayLogger.warn(e));
            return stageInterval;
        },

        // Mark the processing activity interval, updating the status
        markActivityInterval: async (_, { activityIntervalId, input }) => {
            performanceLogger.performance('markActivityInterval', 'enteringResolver');
            gatewayLogger.info(`Mark Activity called with status: ${input.status}`);
            const previousActivityId = workflowProcessor.getCurrentOpenActivityId();
            // Container used to return SignalDetections that were saved
            const associationChange: AssociationChange = {
                events: [],
                sds: []
            };

            // Apply the marking input to the processing activity with the provided ID
            const activityInterval = await workflowProcessor.markActivityInterval(activityIntervalId, input);
            if (input.status === model.IntervalStatus.Complete) {
                const savedSDs =
                    await signalDetectionProcessor.saveSignalDetections();
                associationChange.sds = associationChange.sds.concat(savedSDs);
            } else if (input.status === model.IntervalStatus.InProgress) {
                if (previousActivityId !== '') {
                    // Only clear the modified signal detections if switching stages
                    const newStageIntervalId =
                        (await workflowProcessor.getActivityInterval(activityIntervalId)).stageIntervalId;
                    const prevStageIntervalId =
                        (await workflowProcessor.getActivityInterval(previousActivityId)).stageIntervalId;
                    if (newStageIntervalId !== prevStageIntervalId) {
                        // TODO we should handle this differently in the future but per Jamie,
                        // for now we can just save when we switch intervals
                        const savedSDs = await signalDetectionProcessor.saveSignalDetections();
                        associationChange.sds = associationChange.sds.concat(savedSDs);
                        await workflowProcessor.loadSDsAndEvents(activityInterval);
                    }
                } else {
                    await workflowProcessor.loadSDsAndEvents(activityInterval);
                }

            }
            // Publish the updated stages
            const changedStages = await workflowProcessor.getStages();
            pubsub.publish(subConfig.channels.stagesChanged, { stagesChanged:  changedStages})
                .catch(e => gatewayLogger.warn(e));
            // load sds and events if inProgress
            performanceLogger.performance('markActivityInterval', 'leavingResolver');
            return {
                activityInterval,
                associationChange
            };
        },

        // set a new time interval for the workflow
        setTimeInterval: async (_, { startTimeSec, endTimeSec }) => {
            workflowProcessor.setNewTimeRange(startTimeSec, endTimeSec);
            // Publish the updated stages
            pubsub.publish(subConfig.channels.stagesChanged, { stagesChanged: await workflowProcessor.getStages() })
                .catch(e => gatewayLogger.warn(e));
            return workflowProcessor.getStages();
        },
    },

    // Subscription Resolvers
    Subscription: {
        stagesChanged: {
            subscribe: async () => pubsub.asyncIterator(subConfig.channels.stagesChanged),
        }
    },

    // Field resolvers for ProcessingStage
    ProcessingStage: {
        activities: async (stage: model.ProcessingStage) =>
            await workflowProcessor.getActivitiesByStage(stage.id),
        // Field intervals accepts an optional TimeRange parameter with startTime and endTime
        // date objects. If the parameter is provided, filter the stage intervals based on the
        // TimeRange bounds.
        intervals: async (stage: model.ProcessingStage, { timeRange }) =>
            await workflowProcessor.getIntervalsByStage(stage.id, timeRange),
    },

    // Field resolvers for ProcessingInterval
    ProcessingInterval: {
        stageIntervals: async (interval: model.ProcessingInterval) =>
            workflowProcessor.getStageIntervalsByInterval(interval.id),
    },

    // Field resolvers for ProcessingStageInterval
    ProcessingStageInterval: {
        activityIntervals: async (stageInterval: model.ProcessingStageInterval) =>
            workflowProcessor.getActivityIntervalsByStageInterval(stageInterval.id),
        completedBy: async (stageInterval: model.ProcessingStageInterval) =>
            workflowProcessor.getAnalyst(stageInterval.completedByUserName),
    },

    // Field resolvers for ProcessingActivityInterval
    ProcessingActivityInterval: {
        activity: async (activityInterval: model.ProcessingActivityInterval) =>
            workflowProcessor.getActivity(activityInterval.activityId),
        activeAnalysts: async (activityInterval: model.ProcessingActivityInterval) =>
            workflowProcessor.getAnalysts(activityInterval.activeAnalystUserNames),
        completedBy: async (activityInterval: model.ProcessingActivityInterval) =>
            workflowProcessor.getAnalyst(activityInterval.completedByUserName),
    },
};
