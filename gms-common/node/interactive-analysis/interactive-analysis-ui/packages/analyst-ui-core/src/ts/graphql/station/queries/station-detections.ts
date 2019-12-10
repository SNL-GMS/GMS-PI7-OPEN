import ApolloClient, { ApolloQueryResult } from 'apollo-client';
import gql from 'graphql-tag';
import { processingStationFragment } from '../gqls';
import { ProcessingStation } from '../types';

export const defaultStationsQuery = gql`
  query defaultStations {
    defaultStations {
      ...ProcessingStationFragment
    }
  }
  ${processingStationFragment}
`;

export const defaultStations = async ({
  client
}: {
  client: ApolloClient<any>;
}): Promise<ApolloQueryResult<{ defaultStations?: ProcessingStation[] }>> =>
  client.query<{ defaultStations?: ProcessingStation[] }>({
    variables: {},
    query: defaultStationsQuery
  });
