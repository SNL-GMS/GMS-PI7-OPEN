import ApolloClient, { ApolloQueryResult } from 'apollo-client';
import gql from 'graphql-tag';
import { distanceToSourceFragment } from '../gqls';
import { DistanceToSource, DistanceToSourceForDefaultStationsQueryArgs } from '../types';

export const distanceToSourceForDefaultStationsQuery = gql`
  query distanceToSourceForDefaultStations($distanceToSourceInput: DistanceToSourceInput!) {
    distanceToSourceForDefaultStations(distanceToSourceInput: $distanceToSourceInput) {
      ...DistanceToSourceFragment
    }
  }
  ${distanceToSourceFragment}
`;

export const distanceToSourceForDefaultStations = async ({
  variables,
  client
}: {
  variables: DistanceToSourceForDefaultStationsQueryArgs;
  client: ApolloClient<any>;
}): Promise<ApolloQueryResult<{ distanceToSourceForDefaultStations?: DistanceToSource[] }>> =>
  client.query<{ distanceToSourceForDefaultStations?: DistanceToSource[] }>({
    variables: {
      ...variables
    },
    query: distanceToSourceForDefaultStationsQuery
  });
