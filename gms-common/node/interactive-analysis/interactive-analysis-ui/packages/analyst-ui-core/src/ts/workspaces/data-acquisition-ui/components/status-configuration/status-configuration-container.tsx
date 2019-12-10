import { compose, graphql } from 'react-apollo';
import { StationQueries } from '~graphql/';
import { StatusConfigurations } from './status-configuration-component';

/**
 * A new apollo component, that's wrapping the StationInformation component and injecting
 * apollo graphQL queries and mutations.
 */
export const ApolloStatusConfigurationContainer = compose(
  graphql(StationQueries.defaultStationsQuery, { name: 'defaultStationsQuery' })
)(StatusConfigurations);
