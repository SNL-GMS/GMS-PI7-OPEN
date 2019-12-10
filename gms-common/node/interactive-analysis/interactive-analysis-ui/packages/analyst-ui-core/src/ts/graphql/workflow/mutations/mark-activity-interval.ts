import { ApolloClient } from 'apollo-client';
import { FetchResult } from 'apollo-link';
import gql from 'graphql-tag';
import { associationChangeFragment } from '~graphql/event/gqls';
import { MarkActivityIntervalMutationArgs } from '../types';

export const markActivityIntervalMutation = gql`
mutation markActivityInterval($activityIntervalId: String!, $input: IntervalStatusInput!) {
    markActivityInterval(activityIntervalId: $activityIntervalId, input: $input) {
      activityInterval {
        id
      }
      associationChange {
        ...AssociationChangeFragment
      }
    }
}
${associationChangeFragment}
`;

/**
 * Marks a activity interval
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const markActivityInterval = async (
  client: ApolloClient<any>,
  variables: MarkActivityIntervalMutationArgs
): Promise<FetchResult<string>> =>
  client.mutate<string>({
    variables,
    mutation: markActivityIntervalMutation
  });
