import ApolloClient, { ApolloQueryResult } from 'apollo-client';
import gql from 'graphql-tag';
import { channelSegmentFragment } from '~graphql/channel-segment/gqls';
import { filteredChannelSegmentFragment } from '../gqls';
import { GetWaveformSegmentQueryArgs, RawAndFilteredChannelSegments } from '../types';

export const getWaveformSegmentsByChannelsQuery = gql`
query getWaveformSegmentsByChannels($timeRange: TimeRange!, $channelIds: [String]!, $filterIds: [String]) {
  getWaveformSegmentsByChannels(timeRange: $timeRange, channelIds: $channelIds, filterIds: $filterIds) {
    channelId,
    raw {
      ...ChannelSegmentFragment
    }
    filtered {
      ...FilteredChannelSegmentFragment
    }
  }
}
${channelSegmentFragment}
${filteredChannelSegmentFragment}
`;

export const getWaveformSegmentsByChannels = async ({
    variables,
    client
  }: {
    variables: GetWaveformSegmentQueryArgs;
    client: ApolloClient<any>;
  }): Promise<ApolloQueryResult<{ getWaveformSegmentsByChannels?: RawAndFilteredChannelSegments[] }>> =>
    client.query<{ getWaveformSegmentsByChannels?: RawAndFilteredChannelSegments[] }>({
      variables: {
        ...variables
      },
      query: getWaveformSegmentsByChannelsQuery
    });
