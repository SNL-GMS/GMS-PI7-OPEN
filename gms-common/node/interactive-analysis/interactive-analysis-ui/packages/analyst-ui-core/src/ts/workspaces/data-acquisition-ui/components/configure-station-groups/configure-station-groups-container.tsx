import { compose, graphql } from 'react-apollo';
import { StationQueries } from '~graphql/';
import { ConfigureStationGroups } from './configure-station-groups-component';

export const ApolloConfigureStationGroupsContainer: React.ComponentClass<Pick<{}, never>> = compose(
  graphql(StationQueries.defaultStationsQuery, { name: 'defaultStationsQuery' })
)(ConfigureStationGroups);
