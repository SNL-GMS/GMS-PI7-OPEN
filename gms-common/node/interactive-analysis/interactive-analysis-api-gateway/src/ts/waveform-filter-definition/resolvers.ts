import { PubSub } from 'graphql-subscriptions';
import * as config from 'config';
import * as model from './model';
import { WaveformFilterDefinitionProcessor } from './waveform-filter-definition-processor';
import { gatewayLogger } from '../log/gateway-logger';

/**
 * Resolvers for the signal detection API gateway
 */

// Create the publish/subscribe API for GraphQL subscriptions
export const pubsub = new PubSub();

// Load configuration settings
const settings = config.get('waveformFilterDefinition');

// GraphQL Resolvers
export const resolvers = {

    // Query resolvers
    Query: {
        defaultWaveformFilters: async (): Promise<model.WaveformFilter[]> =>
            WaveformFilterDefinitionProcessor.Instance().getDefaultFilters(),
    },

    Mutation: {
        updateWfFilter: async (_, { input }) => {
            const wfFilter: model.WaveformFilter[] =
                WaveformFilterDefinitionProcessor.Instance().updateWaveformFilters(input);
            // Publish the updated signal detection to the subscription channel
            pubsub.publish(
                settings.subscriptions.channels.wfFiltersUpdated,
                {
                    wfFiltersUpdated: wfFilter,
                }
            ).catch(e => gatewayLogger.warn(e));
            return wfFilter;
        }
    }
};
