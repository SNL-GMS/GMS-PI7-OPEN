import ApolloClient, { ApolloQueryResult } from 'apollo-client';
import gql from 'graphql-tag';
import { qcMaskFragment } from '../gqls';
import { QcMask, QcMasksByChannelIdQueryArgs } from '../types';

export const qcMasksByChannelIdQuery = gql`
query qcMasksByChannelId($timeRange: TimeRange!, $channelIds: [String]) {
    qcMasksByChannelId(timeRange: $timeRange, channelIds: $channelIds) {
      ...QcMaskFragment
    }
}
${qcMaskFragment}
`;

/**
 * Returns an array of ProcessingStage
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const qcMasksByChannelId = async ({
  variables,
  client
}: {
    variables: QcMasksByChannelIdQueryArgs;
    client: ApolloClient<any>;
  }): Promise<ApolloQueryResult<{ qcMasksByChannelId?: QcMask[] }>> =>
  client.query<{ qcMasksByChannelId?: QcMask[] }>({
    variables: {
      ...variables
    },
    query: qcMasksByChannelIdQuery
  });
