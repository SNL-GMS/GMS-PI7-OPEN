import { ApolloClient } from 'apollo-client';
import { FetchResult } from 'apollo-link';
import gql from 'graphql-tag';
import { associationChangeFragment } from '~graphql/event/gqls';
import { UpdateDetectionsMutationArgs } from '../types';

export const updateDetectionsMutation = gql`
mutation updateDetections($detectionIds: [String]!, $input: UpdateDetectionInput!) {
    updateDetections(detectionIds: $detectionIds, input: $input) {
      ...AssociationChangeFragment
    }
}
${associationChangeFragment}
`;

/**
 * Updates the detections
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const updateDetections = async (
    client: ApolloClient<any>,
    variables: UpdateDetectionsMutationArgs
  ): Promise<FetchResult<string>> =>
    client.mutate<string>({
      variables,
      mutation: updateDetectionsMutation
    });
