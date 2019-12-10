import ApolloClient, { ApolloQueryResult } from 'apollo-client';
import gql from 'graphql-tag';
import { signalDetectionFragment } from '../gqls';
import { SignalDetection, SignalDetectionsByDefaultStationsQueryArgs } from '../types';

export const signalDetectionsByDefaultStationsQuery = gql`
query signalDetectionsByDefaultStations($timeRange: TimeRange!) {
  signalDetectionsByDefaultStations(timeRange: $timeRange) {
    ...SignalDetectionFragment
  }
}
${signalDetectionFragment}
`;

export const signalDetectionsByDefaultStations = async ({
    variables,
    client
  }: {
      variables: SignalDetectionsByDefaultStationsQueryArgs;
      client: ApolloClient<any>;
    }): Promise<ApolloQueryResult<{ signalDetectionsByDefaultStations?: SignalDetection[] }>> =>
    client.query<{ signalDetectionsByDefaultStations?: SignalDetection[] }>({
      variables: {
        ...variables
      },
      query: signalDetectionsByDefaultStationsQuery
    });
