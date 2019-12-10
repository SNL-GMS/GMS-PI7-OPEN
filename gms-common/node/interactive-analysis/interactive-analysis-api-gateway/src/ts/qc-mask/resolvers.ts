import { PubSub } from 'graphql-subscriptions';
import * as config from 'config';
import * as model from './model';
import { qcMaskProcessor } from './qc-mask-processor';
import { toEpochSeconds } from '../util/time-utils';

/**
 * Resolvers for the signal detection API gateway
 */

// Create the publish/subscribe API for GraphQL subscriptions
export const pubsub = new PubSub();

// Load configuration settings
const settings = config.get('qcMask');

// GraphQL Resolvers
export const resolvers = {

    // Query resolvers
    Query: {
        qcMasksByChannelId: async (_, { timeRange, channelIds }) => qcMaskProcessor.getQcMasks(timeRange, channelIds)
    },

    // Mutation Resolvers
    Mutation: {
        createQcMask: async (_, { channelIds, input }) => {
            // Create QC Masks
            const masksCreated: model.QcMask[] = await qcMaskProcessor.createQcMasks(channelIds, input);

            // Publish the newly created masks to the subscription channel
            // tslint:disable-next-line: no-floating-promises
            pubsub.publish(settings.subscriptions.channels.qcMasksCreated, {qcMasksCreated: masksCreated});
            return masksCreated;
        },
        updateQcMask: async (_, { qcMaskId, input }) => {
            // Update the mask with the inputs provided
            const maskUpdated: model.QcMask = await qcMaskProcessor.updateQcMask(qcMaskId, input);

            // Publish the newly created masks to the subscription channel
            // pubsub.publish(settings.subscriptions.channels.qcMasksUpdated, {qcMasksUpdated: [maskUpdated]});
            return [maskUpdated];
        },
        rejectQcMask: async (_, { qcMaskId, rationale }) => {
            // Reject QC Mask
            const rejectedMask: model.QcMask = await qcMaskProcessor.rejectQcMask(qcMaskId, rationale);

            // Publish the newly created masks to the subscription channel
            // pubsub.publish(settings.subscriptions.channels.qcMasksUpdated, {qcMasksUpdated: [rejectedMask]});
            return [rejectedMask];
        },
    },

    // Subscription resolvers
    Subscription: {
        // Subscription for new QcMasks
        qcMasksCreated: {
            subscribe: async () => pubsub.asyncIterator(settings.subscriptions.channels.qcMasksCreated)
        },
    },

    // Field Resolvers
    QcMask: {
        currentVersion: async (qcMask: model.QcMask) => qcMask.qcMaskVersions[qcMask.qcMaskVersions.length - 1]

    },
    QcMaskVersion: {
        startTime: async (qcMaskVersion: model.QcMaskVersion) => toEpochSeconds(qcMaskVersion.startTime),
        endTime: async (qcMaskVersion: model.QcMaskVersion) => toEpochSeconds(qcMaskVersion.endTime),
        creationInfo: async (qcMaskVersion: model.QcMaskVersion) =>
                      qcMaskProcessor.getCreationInfo(qcMaskVersion.creationInfoId)
    }

};
