import * as express from 'express';
import * as path from 'path';
import * as config from 'config';
import * as waveformProcessor from './waveform/waveform-processor';
import { execute, subscribe } from 'graphql';
import { createServer } from 'http';
import { SubscriptionServer } from 'subscriptions-transport-ws';
import { schema } from './schema';
import * as bodyParser from 'body-parser';
import * as msgpack from 'msgpack-lite';

// tslint:disable-next-line:no-magic-numbers
const parserLimitSize = 1024 * 1024 * 2000; // 2 gig

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const compression = require('compression');

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const timeout = require('connect-timeout');

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const { ApolloServer } = require('apollo-server-express');

// using the gateway logger
import { gatewayLogger as logger } from './log/gateway-logger';
import { request } from 'https';
import { performanceLogger } from './log/performance-logger';
const objectPath = path.relative(process.cwd(), __filename);

// Load configuration settings
const gqlConfig = config.get('server.graphql');

// GraphQL Path
const graphqlPath = gqlConfig.http.graphqlPath;
logger.info(`graphql path ${graphqlPath}`);

// GraphQL HTTP server port
const httpPort = gqlConfig.http.port;
logger.info(`httpPort ${httpPort}`);

// GraphQL Websocket port
const wsPort = gqlConfig.ws.port;
logger.info(`wsPort ${wsPort}`);

/**
 * Handle request timeouts.
 * @param req the request
 * @param res the response
 * @param next handle the next opporation
 */
const haltOnTimeout = (req, res, next) => {
    if (!req.timedout) {
        next();
    } else {
        // tslint:disable-next-line:no-console
        logger.error(`Error: request timeout: ${request.toString()}`);
    }
};

const app: express.Express = express();

// https://expressjs.com/en/resources/middleware/timeout.html
// note the use of haltOnTimedout after every middleware; it will stop the request flow on a timeout
app.use(timeout('300s'));
logger.info(`setting express server timeout to 300s`);

app.use(compression());
logger.info(`configuring express server compression`);
app.use(haltOnTimeout);

app.use(bodyParser.json({limit: parserLimitSize, type: 'application/json'}));
logger.info(`configuring express server json body parser`);
app.use(haltOnTimeout);

logger.info('register /waveforms');
app.get('/waveforms', waveformProcessor.waveformSegmentRequestHandler);

logger.info('register /waveforms/raw');
app.get('/waveforms/raw', waveformProcessor.waveformRawSegmentRequestHandler);

logger.info('register /waveforms/filter');
app.get('/waveforms/filtered', waveformProcessor.waveformFilteredSegmentRequestHandler);

const server = new ApolloServer(
    {
        schema,
        tracing: process.env.NODE_ENV !== 'production',
        cacheControl: true,
        introspection: process.env.NODE_ENV !== 'production',
        debug: process.env.NODE_ENV !== 'production',
        persistedQueries: true,
        playground: {
            endpoint: graphqlPath,
            subscriptionEndpoint: `ws://${gqlConfig.ws.host}:${wsPort}${gqlConfig.ws.path}`,
        },
        // tslint:disable-next-line:arrow-return-shorthand
        formatResponse: (response, options) => {
            // !IMPORTANT TODO - sometimes options.request is undefined
            // !there is no adequately explained reason that options.request isn't defined - look into this issue
            if (options && options.request) {
                const headers = options.request.headers.get('Accept');
                if (headers !== null &&
                    (headers.includes('application/msgpack'))) {
                    response.data =  msgpack.encode(response.data);
                    return response;
                }
            }
            // Example of data to extract data request out of
            // "data": {
            //     "clientLog": {
            //       "logLevel": "DATA",
            //       "message": "query defaultStations (in 3340 ms)",
            //       "time": "2019-03-28T14:36:55.285Z",
            //       "__typename": "ClientLog"
            //     }
            //   },
            for (const property in response) {
                if (response.hasOwnProperty(property) && property === 'data') {
                    const dataRequestName = Object.keys(response[property])[0];
                    if (!dataRequestName.includes('clientLog')) {
                        performanceLogger.performance(dataRequestName, 'returningFromServer');
                    }
                }
            }
            return response;
        },
        formatError: error => {
            logger.error(error);
            return new Error(`Internal server error: ${error}`);
          },
    });
server.applyMiddleware({ app });

// Listen for GraphQL requests over HTTP
app.listen(httpPort, () => { logger.info(`listening on port ${httpPort}`); });

// Create the Websocket server supporting GraphQL subscriptions
// tslint:disable-next-line:no-shadowed-variable
const websocketServer = createServer((request, response) => {
    const responseCode = 404;
    response.writeHead(responseCode);
    response.end();
});

// Listen for GraphQL subscription connections
websocketServer.listen(wsPort, () => logger.info(
    `Websocket Server is listening on port ${wsPort}`
));

// Create the subscription server
SubscriptionServer.create(
    {
        schema,
        execute,
        subscribe,
    },
    {
        server: websocketServer,
        path: gqlConfig.ws.path,
    },
);

logger.info('intervalService started', { module: objectPath });
