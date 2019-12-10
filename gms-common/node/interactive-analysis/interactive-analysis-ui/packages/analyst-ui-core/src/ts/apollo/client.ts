import { ApolloClient } from 'apollo-client';
import { ApolloLink } from 'apollo-link';
import { SubscriptionClient } from 'subscriptions-transport-ws';
import { GRAPHQL_PROXY_URI, IS_NODE_ENV_DEVELOPMENT, SUBSCRIPTIONS_PROXY_URI } from '../util/environment';
import { UILogger } from '../util/log/logger';
import { isWindowDefined } from '../util/window-util';
import { inMemoryCacheConfiguration } from './cache-confguration';
import { Link, SplitLink, WsClient, WsLink } from './links';

// we can't initialize the websocket client if we aren't running in the browser or a renderer process.
// this shouldn't run in the main electron process.
const windowIsDefined = isWindowDefined();

// The graphql URL
const graphqlProxyUri = GRAPHQL_PROXY_URI;
UILogger.info(`Configured with graphql uri: ${graphqlProxyUri}`);

// The subscriptions URL
const subscriptionsProxyUri = SUBSCRIPTIONS_PROXY_URI;
UILogger.info(`Configured with subscriptions uri: ${subscriptionsProxyUri}`);

const cache = inMemoryCacheConfiguration;

const configureClient = (
  link: ApolloLink
): ApolloClient<any> =>
  new ApolloClient<any>({
    link,
    defaultOptions: {
      query: {
        fetchPolicy: 'cache-first',
        errorPolicy: 'all'
      },
      mutate: {
        errorPolicy: 'all'
      },
      watchQuery: {
        fetchPolicy: 'cache-first',
        errorPolicy: 'all'
      }
    },
    queryDeduplication: true,
    cache,
    // enable apollo dev tools when running in develoment mode
    connectToDevTools: IS_NODE_ENV_DEVELOPMENT
  });

export const createConnection = (
  url: string,
  ws: string
):
  | {
    wsClient: SubscriptionClient;
    link: any;
  }
  | undefined => {

  const wsClient = windowIsDefined ? WsClient(ws) : undefined;
  const wsLink = windowIsDefined ? WsLink(wsClient) : undefined;

  const link = Link(url, false, false);
  if (link !== undefined) {
    return {
      wsClient,
      link: SplitLink(wsLink, link)
    };
  }
  return undefined;
};

/**
 * Create an apollo client with support for subscriptions
 */
export const createApolloClient = (
  url: string = graphqlProxyUri ? `${graphqlProxyUri}/graphql` : undefined,
  ws: string = subscriptionsProxyUri ? `${subscriptionsProxyUri}/subscriptions` : undefined
): {
    client: ApolloClient<any> | undefined;
    wsClient: SubscriptionClient | undefined;
  } => {
  try {
    const connection = createConnection(url, ws);
    if (connection !== undefined) {
      const client = configureClient(connection.link);
      return {
        client,
        wsClient: connection.wsClient
      };
    }
    // tslint:disable-next-line:no-console
    console.log(`Failed to create connection:
      url: ${url} ws:${ws}`);

    return {
      client: undefined,
      wsClient: undefined
    };
  } catch (error) {
    // tslint:disable-next-line:no-console
    console.log(`Failed to create Apollo Client:
      url: ${url} ws:${ws} :
      ${error}`);
    return {
      client: undefined,
      wsClient: undefined
    };
  }
};
