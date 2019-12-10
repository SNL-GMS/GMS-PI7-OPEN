import ApolloClient from 'apollo-client';
import { FetchResult } from 'apollo-link';
import gql from 'graphql-tag';
import { associationChangeFragment } from '~graphql/event/gqls';
import { RejectDetectionsMutationArgs } from '../types';

/**
 * Input to the reject detection hypotheses mutation
 */
export interface RejectDetectionsInput {
  detectionIds: string[];
}

export const rejectDetectionsMutation = gql`
mutation rejectDetections($detectionIds: [String]!) {
    rejectDetections(detectionIds: $detectionIds) {
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
export const rejectDetections = async (
  client: ApolloClient<any>,
  variables: RejectDetectionsMutationArgs
): Promise<FetchResult<string>> =>
  client.mutate<string>({
    variables,
    mutation: rejectDetectionsMutation
  });
