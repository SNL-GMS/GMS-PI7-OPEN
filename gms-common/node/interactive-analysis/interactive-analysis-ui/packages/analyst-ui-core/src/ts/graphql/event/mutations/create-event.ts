import { ApolloClient } from 'apollo-client';
import { FetchResult } from 'apollo-link';
import gql from 'graphql-tag';
import { associationChangeFragment, eventFragment } from '~graphql/event/gqls';
import { CreateEventMutationArgs } from '../types';

export const createEventMutation = gql`
mutation createEvent(
  $signalDetectionHypoIds:[String]!)
 {
  createEvent(signalDetectionHypoIds: $signalDetectionHypoIds) {
    event {
      ...EventFragment
    }
    associationChange {
      ...AssociationChangeFragment
    }
  }
}
${eventFragment}
${associationChangeFragment}
`;

/**
 * Changes the signal detection associations
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const createEvent = async (
  client: ApolloClient<any>,
  variables: CreateEventMutationArgs
): Promise<FetchResult<string>> =>
  client.mutate<string>({
    variables,
    mutation: createEventMutation
  });
