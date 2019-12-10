import ApolloClient, { ApolloQueryResult } from 'apollo-client';
import gql from 'graphql-tag';
import { eventFragment } from '../gqls';
import { Event, EventsInTimeRangeQueryArgs } from '../types';

export const eventsInTimeRangeQuery = gql`
query eventsInTimeRange($timeRange: TimeRange!) {
  eventsInTimeRange(timeRange: $timeRange) {
    ...EventFragment
  }
}
${eventFragment}
`;

export const eventsInTimeRange = async ({
  variables,
  client
}: {
    variables: EventsInTimeRangeQueryArgs;
    client: ApolloClient<any>;
  }): Promise<ApolloQueryResult<{ signalDetectionByStation?: Event[] }>> =>
  client.query<{ signalDetectionByStation?: Event[] }>({
    variables: {
      ...variables
    },
    query: eventsInTimeRangeQuery
  });
