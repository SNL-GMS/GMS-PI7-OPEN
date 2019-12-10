import ApolloClient, { ApolloQueryResult } from 'apollo-client';
import gql from 'graphql-tag';
import { waveformFilterFragment } from '../gqls';
import { WaveformFilter } from '../types';

export const defaultWaveformFiltersQuery = gql`
query defaultWaveformFilters {
    defaultWaveformFilters {
      ...WaveformFilterFragment
    }
}
${waveformFilterFragment}
`;

export const waveformFilter = async ({
    client
  }: {
    client: ApolloClient<any>;
  }): Promise<ApolloQueryResult<{ defaultWaveformFilters?: WaveformFilter[] }>> =>
    client.query<{ defaultWaveformFilters?: WaveformFilter[] }>({
      query: defaultWaveformFiltersQuery
    });
