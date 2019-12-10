import { ActionCreator, actionCreator } from '../util/action-helper';
import { ApolloClientState } from './types';

const setApolloClient: ActionCreator<ApolloClientState> = actionCreator<ApolloClientState>('SET_APOLLO_CLIENT');
const setSubscriptionClient: ActionCreator<ApolloClientState> =
  actionCreator<ApolloClientState>('SET_SUBSCRIPTION_CLIENT');

/**
 * Redux actions (public).
 */
export const Actions = {
  setApolloClient,
  setSubscriptionClient
};
