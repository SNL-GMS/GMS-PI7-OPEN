import ApolloClient, { ApolloQueryResult } from 'apollo-client';
import gql from 'graphql-tag';
import { signalDetectionFragment } from '../gqls';
import { SignalDetection, SignalDetectionsByEventQueryArgs } from '../types';

export const signalDetectionsByEventIdQuery = gql`
query signalDetectionsByEventId($eventId: String!) {
  signalDetectionsByEventId(eventId: $eventId) {
    ...SignalDetectionFragment
  }
}
${signalDetectionFragment}
`;

export const signalDetectionsByEventId = async ({
    variables,
    client
  }: {
      variables: SignalDetectionsByEventQueryArgs;
      client: ApolloClient<any>;
    }): Promise<ApolloQueryResult<{ signalDetectionsByEvent?: SignalDetection[] }>> =>
    client.query<{ signalDetectionsByEvent?: SignalDetection[] }>({
      variables: {
        ...variables
      },
      query: signalDetectionsByEventIdQuery
    });
