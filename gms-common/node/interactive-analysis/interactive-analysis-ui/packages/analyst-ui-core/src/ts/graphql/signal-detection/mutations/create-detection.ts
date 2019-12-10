import ApolloClient from 'apollo-client';
import { FetchResult } from 'apollo-link';
import gql from 'graphql-tag';
import { associationChangeFragment } from '~graphql/event/gqls';
import { CreateDetectionsMutationArgs } from '../types';

export const createDetectionMutation = gql`
mutation createDetection($input: NewDetectionInput!) {
    createDetection(input: $input) {
      ...AssociationChangeFragment
    }
}
${associationChangeFragment}
`;

/**
 * Create a new detections
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const createDetection = async (
  client: ApolloClient<any>,
  variables: CreateDetectionsMutationArgs
): Promise<FetchResult<string>> =>
  client.mutate<string>({
    variables,
    mutation: createDetectionMutation
  });
