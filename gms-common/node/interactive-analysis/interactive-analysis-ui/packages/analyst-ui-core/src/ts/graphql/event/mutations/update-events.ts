import { ApolloClient } from 'apollo-client';
import { FetchResult } from 'apollo-link';
import gql from 'graphql-tag';
import { eventFragment } from '~graphql/event/gqls';
import { UpdateEventsMutationArgs } from '../types';

export const updateEventsMutation = gql`
mutation updateEvents($eventIds: [String]!, $input: UpdateEventInput!) {
  updateEvents(eventIds: $eventIds, input: $input) {
    ...EventFragment
  }
}
${eventFragment}
`;

/**
 * Updates the detections
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const updateEvents = async (
  client: ApolloClient<any>,
  variables: UpdateEventsMutationArgs
): Promise<FetchResult<string>> =>
  client.mutate<string>({
    variables,
    mutation: updateEventsMutation
  });
