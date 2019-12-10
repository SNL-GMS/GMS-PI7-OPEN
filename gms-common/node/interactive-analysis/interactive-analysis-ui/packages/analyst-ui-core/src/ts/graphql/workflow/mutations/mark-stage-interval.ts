import { ApolloClient } from 'apollo-client';
import { FetchResult } from 'apollo-link';
import gql from 'graphql-tag';
import { MarkStageIntervalMutationArgs } from '../types';

export const markStageIntervalMutation = gql`
mutation markStageInterval($stageIntervalId: String!, $input: IntervalStatusInput!) {
    markStageInterval(stageIntervalId: $stageIntervalId, input: $input) {
        id
    }
}
`;

/**
 * Marks a stage interval
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const markStageInterval = async (
  client: ApolloClient<any>,
  variables: MarkStageIntervalMutationArgs
): Promise<FetchResult<string>> =>
  client.mutate<string>({
    variables,
    mutation: markStageIntervalMutation
  });
