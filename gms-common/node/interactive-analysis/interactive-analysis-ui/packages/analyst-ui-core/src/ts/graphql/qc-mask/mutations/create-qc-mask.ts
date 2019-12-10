import { ApolloClient } from 'apollo-client';
import { FetchResult } from 'apollo-link';
import gql from 'graphql-tag';
import { qcMaskFragment } from '~graphql/qc-mask/gqls';
import { CreateQcMaskMutationArgs } from '../types';

export const createQcMaskMutation = gql`
mutation createQcMask($channelIds: [String]!, $input: QcMaskInput!) {
    createQcMask(channelIds: $channelIds, input: $input) {
      ...QcMaskFragment
    }
}
${qcMaskFragment}
`;

/**
 * Creates a QC mask
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const createQcMask = async (
  client: ApolloClient<any>,
  variables: CreateQcMaskMutationArgs
): Promise<FetchResult<string>> =>
  client.mutate<string>({
    variables,
    mutation: createQcMaskMutation
  });
