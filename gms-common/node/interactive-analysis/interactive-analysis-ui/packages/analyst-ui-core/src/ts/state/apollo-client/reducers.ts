import { ApolloClient } from 'apollo-client';
import * as Redux from 'redux';
import { SubscriptionClient } from 'subscriptions-transport-ws';
import { Actions } from './actions';
import { SET_APOLLO_CLIENT, SET_SUBSCRIPTION_CLIENT
} from './types';

/**
 * Redux reducer for setting the apollo client.
 * 
 * @param state the state to set
 * @param action the redux action
 */
const setApolloClient = (
  state: ApolloClient<any> | null = null,
  action: SET_APOLLO_CLIENT
) => {
  if (Actions.setApolloClient.test(action)) {
    return action.payload;
  }
  return state;
};

/**
 * Redux reducer for setting the web service client.
 * 
 * @param state the state to set
 * @param action the redux action
 */
const setWsClient = (
  state: SubscriptionClient | null = null,
  action: SET_SUBSCRIPTION_CLIENT
) => {
  if (Actions.setSubscriptionClient.test(action)) {
    return action.payload;
  }
  return state;
};

/**
 * Apollo client reducer.
 */
export const Reducer:
  Redux.Reducer<{
    client: ApolloClient<any> | null;
    wsClient: SubscriptionClient | null;
  }, Redux.AnyAction> = Redux.combineReducers({
    client: setApolloClient,
    wsClient: setWsClient
  });
