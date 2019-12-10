import { ApolloClient } from 'apollo-client';
import { FetchResult } from 'apollo-link';
import gql from 'graphql-tag';
import { processingStageFragment } from '../gqls';
import { ProcessingStage, SetTimeIntervalMutationArgs } from '../types';

export const setTimeIntervalMutation = gql`
mutation setTimeInterval($startTimeSec: Int!, $endTimeSec: Int!) {
  setTimeInterval(startTimeSec: $startTimeSec, endTimeSec: $endTimeSec) {
      ...ProcessingStageFragment
  }
}
${processingStageFragment}
`;

/**
 * Sets time interval
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const setTimeInterval = async (
  client: ApolloClient<any>,
  variables: SetTimeIntervalMutationArgs
): Promise<FetchResult<ProcessingStage>> =>
  client.mutate<ProcessingStage>({
    variables,
    mutation: setTimeIntervalMutation
  });
