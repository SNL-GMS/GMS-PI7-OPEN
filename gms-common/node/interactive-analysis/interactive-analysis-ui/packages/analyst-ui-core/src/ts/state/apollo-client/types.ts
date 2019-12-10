import { ApolloClient } from 'apollo-client';
import { SubscriptionClient } from 'subscriptions-transport-ws';
import { ActionWithPayload } from '../util/action-helper';

export type SET_APOLLO_CLIENT = ActionWithPayload<ApolloClient<any>>;
export type SET_SUBSCRIPTION_CLIENT = ActionWithPayload<SubscriptionClient>;

/**
 * Apollo client state.
 */
export interface ApolloClientState {
  client: ApolloClient<any> | undefined;
  wsClient: SubscriptionClient | undefined;
}
