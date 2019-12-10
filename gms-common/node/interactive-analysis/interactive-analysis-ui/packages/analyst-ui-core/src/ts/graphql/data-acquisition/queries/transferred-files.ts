import ApolloClient, { ApolloQueryResult } from 'apollo-client';
import gql from 'graphql-tag';
import { fileGapsFragment } from '../gqls';
import { FileGap, TransferredFilesByTimeRangeQueryArgs } from '../types';

export const transferredFilesByTimeRangeQuery = gql`
  query transferredFilesByTimeRange($timeRange: TimeRange!) {
    transferredFilesByTimeRange(timeRange: $timeRange) {
      ...FileGapsFragment
    }
  }
  ${fileGapsFragment}
`;

export const transferredFilesByTimeRange = async ({
    variables,
    client
  }: {
      variables: TransferredFilesByTimeRangeQueryArgs;
      client: ApolloClient<any>;
    }): Promise<ApolloQueryResult<{ transferredFilesByTimeRange?: FileGap[] }>> =>
    client.query<{ transferredFilesByTimeRange?: FileGap[] }>({
      variables: {
        ...variables
      },
      query: transferredFilesByTimeRangeQuery
    });
