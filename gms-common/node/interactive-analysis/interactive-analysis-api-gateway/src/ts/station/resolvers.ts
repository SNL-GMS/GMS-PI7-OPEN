import * as model from './model';
import { stationProcessor } from './station-processor';
import { qcMaskProcessor } from '../qc-mask/qc-mask-processor';
import { PubSub } from 'graphql-subscriptions';
import { gatewayLogger as logger } from '../log/gateway-logger';

/**
 * Resolvers for the waveform API gateway
 */

// Create the publish/subscribe API for GraphQL subscriptions
export const pubsub = new PubSub();

// GraphQL Resolvers
logger.info('Creating GraphQL resolvers for the processing channel API...');
export const resolvers = {

    // Query resolvers
    Query: {
        // Retrieve the collection of processing stations that are part of the provided network
        stationsByNetwork: async (_, { networkName }) => stationProcessor.getStationsByNetworkName(networkName),

        // Retrieve the default set of stations configured to be included in the waveform dispay
        defaultStations: async () =>
            stationProcessor.getDefaultStations(),

        // Retrieve a collection of processing channels corresponding to the provided list of IDs
        channelsById: async (_, { ids }) => stationProcessor.getChannelsById(ids),

        // Distance to a source (e.g. event, user-specified location)
        distanceToSourceForDefaultStations: async (_, { distanceToSourceInput }) =>
            await stationProcessor.getDTSForDefaultStations(distanceToSourceInput)
    },

    // Field resolvers for Channel
    ProcessingChannel: {
        site: async (channel: model.ProcessingChannel) => stationProcessor.getSiteById(channel.siteId),

        qcMasks: async (channel: model.ProcessingChannel, { timeRange }) =>
            qcMaskProcessor.getQcMasks(timeRange, [channel.id])
    },

    // Field resolvers for Site
    ProcessingSite: {
        station: async (site: model.ProcessingSite) => stationProcessor.getStationById(site.stationId),
        channels: async (site: model.ProcessingSite) => stationProcessor.getChannelsBySite(site.id),
        defaultChannel: async (site: model.ProcessingSite) =>
            stationProcessor.getDefaultChannelForStation(site.stationId)
    },

    // Field resolvers for Station
    ProcessingStation: {
        sites: async (station: model.ProcessingStation) => stationProcessor.getSitesByStation(station.id),
        defaultChannel: async (station: model.ProcessingStation) =>
            stationProcessor.getDefaultChannelForStation(station.id),
        networks: async (station: model.ProcessingStation) => stationProcessor.getNetworksByIdList(station.networkIds)
    },

    // Field resolvers for Network
    ProcessingNetwork: {
        stations: async (network: model.ProcessingNetwork) => stationProcessor.getStationsByNetworkId(network.id)
    }
};
