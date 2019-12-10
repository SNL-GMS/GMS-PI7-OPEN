import { compose, graphql } from 'react-apollo';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';
import { autoOpenEvent } from '~analyst-ui/common/actions/event-actions';
import { systemConfig } from '~analyst-ui/config';
import {
  EventMutations,
  EventQueries,
  EventTypes,
  SignalDetectionMutations,
  SignalDetectionQueries,
  SignalDetectionTypes,
  StationQueries
} from '~graphql/';
import { AnalystWorkspaceActions, AnalystWorkspaceOperations } from '~state/';
import { AppState } from '~state/types';
import { Map } from './map-component';
import { MapProps, MapReduxProps } from './types';

// map parts of redux state into this component as props
const mapStateToProps = (state: AppState): Partial<MapReduxProps> => ({
  currentTimeInterval: (state.analystWorkspaceState.currentStageInterval) ?
    state.analystWorkspaceState.currentStageInterval.interval.timeInterval : undefined,
  analystActivity: (state.analystWorkspaceState.currentStageInterval) ?
    state.analystWorkspaceState.currentStageInterval.interval.activityInterval.analystActivity : undefined,
  selectedEventIds: state.analystWorkspaceState.selectedEventIds,
  openEventId: state.analystWorkspaceState.openEventId,
  selectedSdIds: state.analystWorkspaceState.selectedSdIds,
  measurementMode: state.analystWorkspaceState.measurementMode,
  sdIdsToShowFk: state.analystWorkspaceState.sdIdsToShowFk

});

// map actions dispatch callbacks into this component as props
const mapDispatchToProps = (dispatch): Partial<MapReduxProps> =>
  bindActionCreators(
    {
      setSelectedEventIds: AnalystWorkspaceActions.setSelectedEventIds,
      setSelectedSdIds: AnalystWorkspaceActions.setSelectedSdIds,
      setOpenEventId: AnalystWorkspaceOperations.setOpenEventId,
      setSdIdsToShowFk: AnalystWorkspaceActions.setSdIdsToShowFk,
      setMeasurementModeEntries: AnalystWorkspaceOperations.setMeasurementModeEntries,
    } as any,
    dispatch
  );

/**
 * higher-order component react-redux(react-apollo(Map))
 */
export const ReduxApolloMap: React.ComponentClass<Pick<{}, never>> = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps),
  graphql(StationQueries.defaultStationsQuery, { name: 'defaultStationsQuery' }),
  // Order matters need to initialize updateEvents mutation before graphql autoOpenEvent below is executed
  graphql(EventMutations.updateEventsMutation, { name: 'updateEvents' }),
  graphql(EventQueries.eventsInTimeRangeQuery, {
    options: (props: MapProps) => {
      const variables: EventTypes.EventsInTimeRangeQueryArgs = {
        timeRange: {
          startTime: Number(props.currentTimeInterval.startTimeSecs) -
            systemConfig.additionalEventDataToLoadInitially(props.analystActivity),
          endTime: Number(props.currentTimeInterval.endTimeSecs) +
            systemConfig.additionalDataToLoadOnInitialLoad(props.analystActivity),
        }
      };
      return {
        variables,
        refetchQueries: [
          'signalDetectionsByStationQuery',
        ],
        onCompleted: (data: { eventsInTimeRange: EventTypes.Event[] }) => {
          autoOpenEvent(
            data.eventsInTimeRange,
            props.currentTimeInterval,
            props.openEventId,
            props.analystActivity,
            (event: EventTypes.Event) => props.setOpenEventId(event),
            props.updateEvents
          );
        }
      };
    },
    skip: (props: MapProps) => !props.currentTimeInterval,
    name: 'eventsInTimeRangeQuery'
  }),
  graphql(SignalDetectionQueries.signalDetectionsByStationQuery, {
    options: (props: MapProps) => {
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
        variables
      };
    },
    skip: (props: MapProps) => !props.currentTimeInterval || !props.defaultStationsQuery.defaultStations,
    name: 'signalDetectionsByStationQuery'
  }),
  graphql(SignalDetectionMutations.updateDetectionsMutation, { name: 'updateDetections' }),
  graphql(SignalDetectionMutations.rejectDetectionsMutation, { name: 'rejectDetectionHypotheses' }),
  graphql(EventMutations.changeSignalDetectionAssociationsMutation, { name: 'changeSignalDetectionAssociations' }),
  graphql(EventMutations.createEventMutation, { name: 'createEvent' }),

)(Map);
