import ApolloClient from 'apollo-client';
import gql from 'graphql-tag';
import { FetchResult } from 'react-apollo';
import { associationChangeFragment } from '~graphql/event/gqls';
import { MarkFksReviewedMutationArgs } from '../types';

export const markFksReviewedMutation = gql`
mutation markFksReviewed($markFksReviewedInput: MarkFksReviewedInput!) {
    markFksReviewed (markFksReviewedInput: $markFksReviewedInput) {
      ...AssociationChangeFragment
    }
}
${associationChangeFragment}
`;

/**
 * Create a new detections
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const markFksReviewed = async (
    client: ApolloClient<any>,
    variables: MarkFksReviewedMutationArgs
  ): Promise<FetchResult<string>> =>
    client.mutate<string>({
      variables,
      mutation: markFksReviewedMutation
    });
