import ApolloClient from 'apollo-client';
import gql from 'graphql-tag';
import { FetchResult } from 'react-apollo';
import { associationChangeFragment } from '~graphql/event/gqls';
import { SetFkWindowLeadMutationArgs } from '../types';

export const setFkWindowLeadMutation = gql`
mutation setFkWindowLead($leadInput: FkLeadInput!) {
  setFkWindowLead (leadInput: $leadInput) {
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
export const setFkWindowLead = async (
    client: ApolloClient<any>,
    variables: SetFkWindowLeadMutationArgs
  ): Promise<FetchResult<string>> =>
    client.mutate<string>({
      variables,
      mutation: setFkWindowLeadMutation
    });
