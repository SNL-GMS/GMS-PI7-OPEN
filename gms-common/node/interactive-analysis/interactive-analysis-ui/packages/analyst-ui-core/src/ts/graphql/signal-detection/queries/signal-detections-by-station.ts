import ApolloClient, { ApolloQueryResult } from 'apollo-client';
import gql from 'graphql-tag';
import { signalDetectionFragment } from '../gqls';
import { SignalDetection, SignalDetectionsByStationQueryArgs } from '../types';

export const signalDetectionsByStationQuery = gql`
query signalDetectionsByStation($stationIds: [String]!, $timeRange: TimeRange!) {
  signalDetectionsByStation(stationIds: $stationIds, timeRange: $timeRange) {
    ...SignalDetectionFragment
  }
}
${signalDetectionFragment}
`;

export const signalDetectionsByStation = async ({
    variables,
    client
  }: {
      variables: SignalDetectionsByStationQueryArgs;
      client: ApolloClient<any>;
    }): Promise<ApolloQueryResult<{ signalDetectionsByStation?: SignalDetection[] }>> =>
    client.query<{ signalDetectionsByStation?: SignalDetection[] }>({
      variables: {
        ...variables
      },
      query: signalDetectionsByStationQuery
    });
