import ApolloClient from 'apollo-client';
import gql from 'graphql-tag';
import { FetchResult } from 'react-apollo';
import { qcMaskFragment } from '~graphql/qc-mask/gqls';
import { UpdateQcMaskMutationArgs } from '../types';

export const updateQcMaskMutation = gql`
mutation updateQcMask($maskId: String!, $input: QcMaskInput!) {
    updateQcMask(qcMaskId: $maskId, input: $input) {
      ...QcMaskFragment
    }
}
${qcMaskFragment}
`;

/**
 * Updates a QC mask
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const updateQcMask = async (
  client: ApolloClient<any>,
  variables: UpdateQcMaskMutationArgs
): Promise<FetchResult<string>> =>
  client.mutate<string>({
    variables,
    mutation: updateQcMaskMutation
  });
