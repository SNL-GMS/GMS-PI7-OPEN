
import ApolloClient, { ApolloQueryResult } from 'apollo-client';
import gql from 'graphql-tag';
import { channelSegmentFragment } from '~graphql/channel-segment/gqls';
import { ChannelSegment } from '~graphql/channel-segment/types';
import { GetRawWaveformSegmentQueryArgs, Waveform } from '../types';

export const getRawWaveformSegmentsByChannelsQuery = gql`
query getRawWaveformSegmentsByChannels($timeRange: TimeRange!, $channelIds: [String]!) {
  getRawWaveformSegmentsByChannels(timeRange: $timeRange, channelIds: $channelIds) {
    ...ChannelSegmentFragment
  }
}
${channelSegmentFragment}
`;

export const getRawWaveformSegmentsByChannels = async ({
    variables,
    client
  }: {
    variables: GetRawWaveformSegmentQueryArgs;
    client: ApolloClient<any>;
  }): Promise<ApolloQueryResult<{ getRawWaveformSegmentsByChannels?: ChannelSegment<Waveform>[] }>> =>
    client.query<{ getRawWaveformSegmentsByChannels?: ChannelSegment<Waveform>[] }>({
      variables: {
        ...variables
      },
      query: getRawWaveformSegmentsByChannelsQuery
    });
