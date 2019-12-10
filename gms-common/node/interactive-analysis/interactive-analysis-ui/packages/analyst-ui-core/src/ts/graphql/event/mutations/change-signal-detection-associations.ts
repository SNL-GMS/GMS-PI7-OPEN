import { ApolloClient } from 'apollo-client';
import { FetchResult } from 'apollo-link';
import gql from 'graphql-tag';
import { eventFragment } from '~graphql/event/gqls';
import { signalDetectionFragment } from '~graphql/signal-detection/gqls';
import { ChangeSignalDetectionAssociationsMutationArgs } from '../types';

export const changeSignalDetectionAssociationsMutation = gql`
mutation changeSignalDetectionAssociations(
  $eventHypothesisId: String!,
  $signalDetectionHypoIds:[String]!,
  $associate: Boolean!)
 {
  changeSignalDetectionAssociations(eventHypothesisId: $eventHypothesisId,
  signalDetectionHypoIds: $signalDetectionHypoIds,
  associate: $associate) {
    events {
      ...EventFragment
    }
    sds {
      ...SignalDetectionFragment
    }
  }
 }
  ${eventFragment}
  ${signalDetectionFragment}
`;

/**
 * Changes the signal detection associations
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const changeSignalDetectionAssociations = async (
  client: ApolloClient<any>,
  variables: ChangeSignalDetectionAssociationsMutationArgs
): Promise<FetchResult<string>> =>
  client.mutate<string>({
    variables,
    mutation: changeSignalDetectionAssociationsMutation
  });
