import { compose, graphql } from 'react-apollo';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';
import { systemConfig, userPreferences } from '~analyst-ui/config';
import { EventMutations,
  EventQueries, EventTypes, SignalDetectionMutations,
  SignalDetectionQueries, SignalDetectionTypes, StationQueries, StationTypes } from '~graphql/';
import { AnalystWorkspaceActions, AnalystWorkspaceOperations } from '~state';
import { AppState } from '~state/types';
import { Location } from './location-component';
import { LocationProps, LocationReduxProps } from './types';

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (state: AppState): Partial<LocationReduxProps> => ({
  currentTimeInterval: (state.analystWorkspaceState.currentStageInterval) ?
  state.analystWorkspaceState.currentStageInterval.interval.timeInterval : undefined,
  analystActivity: (state.analystWorkspaceState.currentStageInterval) ?
    state.analystWorkspaceState.currentStageInterval.interval.activityInterval.analystActivity : undefined,
  openEventId: state.analystWorkspaceState.openEventId,
  selectedSdIds: state.analystWorkspaceState.selectedSdIds,
  measurementMode: state.analystWorkspaceState.measurementMode,
  sdIdsToShowFk: state.analystWorkspaceState.sdIdsToShowFk
});

/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 * 
 * @param dispatch the redux dispatch event alerting the store has changed
 */
const mapDispatchToProps = (dispatch): Partial<LocationReduxProps> =>
  bindActionCreators(
      {
        setSelectedSdIds: AnalystWorkspaceActions.setSelectedSdIds,
        setOpenEventId: AnalystWorkspaceOperations.setOpenEventId,
        setSelectedEventIds: AnalystWorkspaceActions.setSelectedEventIds,
        setSdIdsToShowFk: AnalystWorkspaceActions.setSdIdsToShowFk,
        setMeasurementModeEntries: AnalystWorkspaceOperations.setMeasurementModeEntries,
      } as any,
      dispatch
  );

/**
 * A new redux apollo component, that's wrapping the Location component and injecting in the redux state
 * and apollo graphQL queries and mutations.
 */
export const ReduxApolloLocationContainer: React.ComponentClass<Pick<{}, never>> = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps),
  graphql(StationQueries.defaultStationsQuery, { name: 'defaultStationsQuery' }),
  graphql(EventMutations.updateEventsMutation, { name: 'updateEvents' }),
  graphql(EventMutations.updateFeaturePredictionsMutation, { name: 'updateFeaturePredictions' }),
  graphql(EventMutations.locateEventMutation, { name: 'locateEvent' }),
  graphql(EventQueries.eventsInTimeRangeQuery, {
    options: (props: LocationProps) => {
      const variables: EventTypes.EventsInTimeRangeQueryArgs = {
        timeRange: {
          startTime: Number(props.currentTimeInterval.startTimeSecs) -
          systemConfig.additionalEventDataToLoadInitially(props.analystActivity),
          endTime: Number(props.currentTimeInterval.endTimeSecs) +
            systemConfig.additionalDataToLoadOnInitialLoad(props.analystActivity)
        }
      };
      return {
        variables,
        refetchQueries: [
          'distanceToSourceForDefaultStationsQuery',
        ],
        awaitRefetchQueries: true
      };
    },
    skip: (props: LocationProps) => !props.currentTimeInterval,
    name: 'eventsInTimeRangeQuery'
  }),
  graphql(SignalDetectionQueries.signalDetectionsByStationQuery, {
    options: (props: LocationProps) => {
      // Get signal detections in the current interval
      const variables: SignalDetectionTypes.SignalDetectionsByStationQueryArgs = {
        stationIds: props.defaultStationsQuery.defaultStations.map(station => station.id),
        timeRange: {
          startTime: Number(props.currentTimeInterval.startTimeSecs) -
            systemConfig.additionalDataToLoadOnInitialLoad(props.analystActivity),
          endTime: Number(props.currentTimeInterval.endTimeSecs) +
            systemConfig.additionalDataToLoadOnInitialLoad(props.analystActivity),
        }
      };
      return {
        variables,
      };
    },
    skip: (props: LocationProps) => !props.currentTimeInterval ||
    !props.defaultStationsQuery.defaultStations,
    name: 'signalDetectionsByStationQuery'
}),
  graphql(StationQueries.distanceToSourceForDefaultStationsQuery, {
    options: (props: LocationProps) => {
      // get events before & after the current interval as well
      const variables: StationTypes.DistanceToSourceForDefaultStationsQueryArgs = {
        distanceToSourceInput: {
          sourceId: props.openEventId,
          sourceType: 'Event',
          distanceUnits: userPreferences.distanceUnits
        }
      };
      return {
        variables
      };
  },
    skip: (props: LocationProps) => !props.currentTimeInterval || !props.openEventId,
    name: 'distanceToSourceForDefaultStationsQuery'
}),
  graphql(SignalDetectionMutations.updateDetectionsMutation, { name: 'updateDetections' }),
  graphql(SignalDetectionMutations.rejectDetectionsMutation, { name: 'rejectDetectionHypotheses' }),
  graphql(EventMutations.changeSignalDetectionAssociationsMutation, { name: 'changeSignalDetectionAssociations' }),
  graphql(EventMutations.createEventMutation, { name: 'createEvent' }),
)(Location);
