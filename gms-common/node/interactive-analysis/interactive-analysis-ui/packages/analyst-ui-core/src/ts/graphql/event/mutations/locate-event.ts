import { ApolloClient } from 'apollo-client';
import { FetchResult } from 'apollo-link';
import gql from 'graphql-tag';
import { eventHypothesisFragment } from '~graphql/event/gqls';
import { LocateEventMutationArgs } from '../types';

/**
 * Locate Event Mutation Definition
 */
export const locateEventMutation = gql`
mutation locateEvent($eventHypothesisId: String!, $preferredLocationSolutionId: String!,
  $locationBehaviors: [LocationBehaviorInput]!) {
    locateEvent(eventHypothesisId: $eventHypothesisId, preferredLocationSolutionId: $preferredLocationSolutionId,
    locationBehaviors: $locationBehaviors) {
      id
      rejected
      ...EventHypothesisFragment
    }
  }
  ${eventHypothesisFragment}
`;

/**
 * Compute Event Location
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const locateEvent = async (
  client: ApolloClient<any>,
  variables: LocateEventMutationArgs
): Promise<FetchResult<string>> =>
  client.mutate<string>({
    variables,
    mutation: locateEventMutation
  });
