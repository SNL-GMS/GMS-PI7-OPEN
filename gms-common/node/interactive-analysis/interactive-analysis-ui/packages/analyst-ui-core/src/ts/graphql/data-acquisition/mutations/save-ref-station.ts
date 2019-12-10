import ApolloClient from 'apollo-client';
import gql from 'graphql-tag';
import { FetchResult } from 'react-apollo';
import { SaveReferenceStationMutationArgs } from '../types';

export const saveReferenceStationMutation = gql`
mutation saveReferenceStation($input: ReferenceStation!) {
  saveReferenceStation(input: $input) {
      result
    }
}
`;

/**
 * Saves a Reference Station
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const saveReferenceStation = async (
  client: ApolloClient<any>,
  variables: SaveReferenceStationMutationArgs
): Promise<FetchResult<string>> =>
  client.mutate<string>({
    variables,
    mutation: saveReferenceStationMutation
  });
