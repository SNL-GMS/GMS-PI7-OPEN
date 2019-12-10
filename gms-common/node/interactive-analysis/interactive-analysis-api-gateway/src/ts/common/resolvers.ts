import { gatewayLogger as logger } from '../log/gateway-logger';
import { PubSub } from 'graphql-subscriptions';
import * as model from './model';
/**
 * Resolvers for the common API gateway
 */

// GraphQL Resolvers
logger.info('Creating common API Gateway GraphQL resolvers...');
export const pubsub = new PubSub();
export const resolvers = {

    // Field resolvers for Timeseries
    Timeseries: {
        /**
         * Special interface resolver to determine the implementing type based on field content
         */
        __resolveType(obj, context, info) {
            if (obj.values) {
                return 'Waveform';
            }
            return undefined;
        }
    },

    Mutation: {
        clientLog: async (_, { clientLogInput }) => {
            const clientLog: model.ClientLog = {
              logLevel: clientLogInput.logLevel,
              message: clientLogInput.message,
              time: clientLogInput.time ? clientLogInput.time : ''
            };
            const logTag = '[CLIENT]';
            switch (clientLog.logLevel) {
              case model.LogLevel.INFO:
                logger.info(`${logTag} ${clientLogInput.time} - ${clientLogInput.message}`);
                break;
              case model.LogLevel.ERROR:
                logger.error(`${logTag} ${clientLogInput.time} - ${clientLogInput.message}`);
                break;
              case model.LogLevel.DEBUG:
                logger.debug(`${logTag} ${clientLogInput.time} - ${clientLogInput.message}`);
                break;
              case model.LogLevel.DATA:
                logger.data(`${logTag} ${clientLogInput.time} - ${clientLogInput.message}`);
                break;
              case model.LogLevel.WARNING:
                logger.warn(`${logTag} ${clientLogInput.time} - ${clientLogInput.message}`);
                break;
              default:
          }
            return clientLog;
        }
    }
};
