import ApolloClient from 'apollo-client';
import gql from 'graphql-tag';
import { FetchResult } from 'react-apollo';
import { associationChangeFragment } from '~graphql/event/gqls';
import { ComputeBeamMutationArgs } from '~graphql/fk/types';

export const computeBeamMutation = gql`
mutation computeBeam($signalDetectionId: String!) {
    computeFks (signalDetectionId: $signalDetectionId) {
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
export const computeBeam = async (
    client: ApolloClient<any>,
    variables: ComputeBeamMutationArgs
  ): Promise<FetchResult<string>> =>
    client.mutate<string>({
      variables,
      mutation: computeBeamMutation
    });
