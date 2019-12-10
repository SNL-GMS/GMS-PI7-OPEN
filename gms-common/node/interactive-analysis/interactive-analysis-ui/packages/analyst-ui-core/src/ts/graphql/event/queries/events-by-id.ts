import ApolloClient, { ApolloQueryResult } from 'apollo-client';
import gql from 'graphql-tag';
import { eventFragment } from '../gqls';
import { Event, EventsInTimeRangeQueryArgs } from '../types';

export const eventsByIdQuery = gql`
query eventsById($timeRange: TimeRange!) {
  eventsById(timeRange: $timeRange) {
    ...EventFragment
  }
}
${eventFragment}
`;

export const eventsById = async ({
  variables,
  client
}: {
    variables: EventsInTimeRangeQueryArgs;
    client: ApolloClient<any>;
  }): Promise<ApolloQueryResult<{ signalDetectionByStation?: Event }>> =>
  client.query<{ signalDetectionByStation?: Event }>({
    variables: {
      ...variables
    },
    query: eventsByIdQuery
  });
