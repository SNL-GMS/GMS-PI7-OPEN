import { compose, graphql } from 'react-apollo';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';
import { StationQueries } from '~graphql/';
import { DataAcquisitionWorkspaceActions } from '~state/data-acquisition-workspace';
import { AppState } from '~state/types';
import { StationInformation } from './station-information-component';
import { StationInformationReduxProps } from './types';

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (state: AppState): Partial<StationInformationReduxProps> => ({
  selectedStationIds: state.dataAcquisitionWorkspaceState.selectedStationIds,
  selectedProcessingStation: state.dataAcquisitionWorkspaceState.selectedProcessingStation,
  unmodifiedProcessingStation: state.dataAcquisitionWorkspaceState.unmodifiedProcessingStation
});

/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 *
 * @param dispatch the redux dispatch event alerting the store has changed
 */
const mapDispatchToProps = (dispatch): Partial<StationInformationReduxProps> =>
  bindActionCreators(
    {
      setSelectedStationIds: DataAcquisitionWorkspaceActions.setSelectedStationIds,
      setSelectedProcessingStation: DataAcquisitionWorkspaceActions.setSelectedProcessingStation,
      setUnmodifiedProcessingStation: DataAcquisitionWorkspaceActions.setUnmodifiedProcessingStation
    } as any,
    dispatch
  );

/**
 * A new apollo component that's wrapping the StationConfiguration component and injecting
 * apollo graphQL queries and mutations.
 */
export const ReduxApolloStationInformationContainer: React.ComponentClass<Pick<{}, never>> = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps),
  graphql(StationQueries.defaultStationsQuery, { name: 'defaultStationsQuery' })
)(StationInformation);
