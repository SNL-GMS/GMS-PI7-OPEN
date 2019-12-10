import ApolloClient, { ApolloQueryResult } from 'apollo-client';
import gql from 'graphql-tag';
import { fkFrequencyThumbnailBySDIdFragment } from '~graphql/fk/gqls';
import { ComputeFrequencyFkThumbnailsInput, FkFrequencyThumbnailBySDId } from '../types';

export const computeFkFrequencyThumbnailQuery = gql`
query computeFkFrequencyThumbnails($fkInput: FkInput!) {
  computeFkFrequencyThumbnails (fkInput: $fkInput) {
      ...FkFrequencyThumbnailBySDIdFragment
    }
}
${fkFrequencyThumbnailBySDIdFragment}
`;

/**
 * Create a new detections
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const computeFkFrequencyThumbnails = async (
    client: ApolloClient<any>,
    variables: ComputeFrequencyFkThumbnailsInput
  ): Promise<ApolloQueryResult<{ computeFkFrequencyThumbnails?: FkFrequencyThumbnailBySDId }>> =>
    client.query<{ computeFkFrequencyThumbnails?: FkFrequencyThumbnailBySDId }>({
      variables,
      query: computeFkFrequencyThumbnailQuery
    });
