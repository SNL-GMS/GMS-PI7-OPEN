import ApolloClient from 'apollo-client';
import gql from 'graphql-tag';
import { FetchResult } from 'react-apollo';
import { associationChangeFragment } from '~graphql/event/gqls';
import { ComputeFksMutationArgs } from '../types';

export const computeFksMutation = gql`
mutation computeFks($fkInput: [FkInput]!) {
    computeFks (fkInput: $fkInput) {
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
export const computeFks = async (
    client: ApolloClient<any>,
    variables: ComputeFksMutationArgs
  ): Promise<FetchResult<string>> =>
    client.mutate<string>({
      variables,
      mutation: computeFksMutation
    });
