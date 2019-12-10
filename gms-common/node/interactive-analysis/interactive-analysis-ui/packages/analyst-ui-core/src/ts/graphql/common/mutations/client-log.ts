import { ApolloClient } from 'apollo-client';
import { FetchResult } from 'apollo-link';
import gql from 'graphql-tag';
import { ClientLogMutationArgs } from '../types';

/**
 * Locate Event Mutation Definition
 */
export const clientLogMutation = gql`
mutation clientLog($input: ClientLogInput) {
    clientLog(clientLogInput: $input) {
      logLevel
      message
      time
    }
  }
`;

/**
 * Compute Event Location
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const clientLog = async (
  client: ApolloClient<any>,
  variables: ClientLogMutationArgs
): Promise<FetchResult<string>> =>
  client.mutate<string>({
    variables,
    mutation: clientLogMutation
  });
