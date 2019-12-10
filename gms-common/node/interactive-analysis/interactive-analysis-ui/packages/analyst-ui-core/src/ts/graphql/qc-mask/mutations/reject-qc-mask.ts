import ApolloClient from 'apollo-client';
import gql from 'graphql-tag';
import { FetchResult } from 'react-apollo';
import { qcMaskFragment } from '~graphql/qc-mask/gqls';
import { RejectQcMaskMutationArgs } from '../types';

export const rejectQcMaskMutation = gql`
mutation rejectQcMask($maskId: String!, $inputRationale: String!) {
    rejectQcMask(qcMaskId: $maskId, rationale: $inputRationale) {
      ...QcMaskFragment
    }
}
${qcMaskFragment}
`;

/**
 * Rejects a QC mask
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const rejectQcMask = async (
  client: ApolloClient<any>,
  variables: RejectQcMaskMutationArgs
): Promise<FetchResult<string>> =>
  client.mutate<string>({
    variables,
    mutation: rejectQcMaskMutation
  });
