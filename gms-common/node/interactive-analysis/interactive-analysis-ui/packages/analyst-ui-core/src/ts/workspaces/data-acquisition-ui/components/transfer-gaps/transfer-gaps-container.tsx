import { compose, graphql } from 'react-apollo';
import { DataAcquisitionQueries, DataAcquisitionTypes } from '~graphql/';
import { userPreferences } from '../../config/user-preferences';
import { TransferGaps } from './transfer-gaps-component';
import { TransferGapsProps } from './types';
/**
 * A new apollo component, that's wrapping the TransferGaps component
 * and apollo graphQL queries.
 */
export const ApolloTransferGapsContainer = compose(
  graphql(DataAcquisitionQueries.transferredFilesByTimeRangeQuery, {
    options: (props: TransferGapsProps) => {
      // Get transferred file gaps by time range
      const variables: DataAcquisitionTypes.TransferredFilesByTimeRangeQueryArgs = {
        timeRange: {
          startTime: 0,
          endTime: 9999999,
        }
      };
      const pollInterval = userPreferences.transferredFilesGapsUpdateTime;
      return {
        variables,
        pollInterval,
        fetchPolicy: 'network-only'
      };
    },
    name: 'transferredFilesByTimeRangeQuery',
  })
)(TransferGaps);
