import ApolloClient, { ApolloQueryResult } from 'apollo-client';
import gql from 'graphql-tag';
import { filteredChannelSegmentFragment } from '../gqls';
import { FilteredChannelSegment, GetFilteredWaveformSegmentQueryArgs } from '../types';

export const getFilteredWaveformSegmentsByChannelsQuery = gql`
query getFilteredWaveformSegmentsByChannels($timeRange: TimeRange!, $channelIds: [String]!, $filterIds: [String]) {
  getFilteredWaveformSegmentsByChannels(timeRange: $timeRange, channelIds: $channelIds, filterIds: $filterIds) {
    ...FilteredChannelSegmentFragment
  }
}
${filteredChannelSegmentFragment}
`;

export const getFilteredWaveformSegmentsByChannels = async ({
  variables,
  client
}: {
  variables: GetFilteredWaveformSegmentQueryArgs;
  client: ApolloClient<any>;
}): Promise<ApolloQueryResult<{ getFilteredWaveformSegmentsByChannels?: FilteredChannelSegment[] }>> =>
  client.query<{ getFilteredWaveformSegmentsByChannels?: FilteredChannelSegment[] }>({
    variables: {
      ...variables
    },
    query: getFilteredWaveformSegmentsByChannelsQuery
  });
