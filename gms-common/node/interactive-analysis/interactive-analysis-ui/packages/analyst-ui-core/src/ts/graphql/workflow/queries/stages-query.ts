import { ApolloClient, ApolloQueryResult } from 'apollo-client';
import gql from 'graphql-tag';
import { processingStageFragment } from '../gqls';
import { ProcessingStage } from '../types';

export const stagesQuery = gql`
query stages {
  stages {
      ...ProcessingStageFragment
  }
}
${processingStageFragment}
`;

/**
 * Returns an array of ProcessingStage
 * 
 * @param client apollo client
 * @param variables mutation variables
 */
export const stages = async ({
    client
  }: {
    client: ApolloClient<any>;
  }): Promise<ApolloQueryResult<{ stages?: ProcessingStage[] }>> =>
    client.query<{ stages?: ProcessingStage[] }>({
      query: stagesQuery
    });
