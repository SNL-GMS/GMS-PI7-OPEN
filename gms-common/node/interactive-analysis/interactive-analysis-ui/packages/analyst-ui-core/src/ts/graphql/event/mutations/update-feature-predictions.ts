import { ApolloClient } from 'apollo-client';
import { FetchResult } from 'apollo-link';
import gql from 'graphql-tag';
import { eventFragment } from '~graphql/event/gqls';
import { UpdateFeaturePredictionsMutationArgs } from '../types';

export const updateFeaturePredictionsMutation = gql`
mutation updateFeaturePredictionsMutation($eventId: String!) {
    updateFeaturePredictions(eventId: $eventId) {
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
export const updateFeaturePredictions = async (
  client: ApolloClient<any>,
  variables: UpdateFeaturePredictionsMutationArgs
): Promise<FetchResult<string>> =>
  client.mutate<string>({
    variables,
    mutation: updateFeaturePredictionsMutation
  });
