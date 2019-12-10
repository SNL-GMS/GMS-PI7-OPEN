import * as React from 'react';
import { compose, graphql } from 'react-apollo';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';
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
import { SignalDetections } from './signal-detections-component';
import { SignalDetectionsProps, SignalDetectionsReduxProps } from './types';

// Map parts of redux state into this component as props
const mapStateToProps = (state: AppState): Partial<SignalDetectionsReduxProps> => ({
  currentTimeInterval: (state.analystWorkspaceState.currentStageInterval) ?
    state.analystWorkspaceState.currentStageInterval.interval.timeInterval : undefined,
  analystActivity: (state.analystWorkspaceState.currentStageInterval) ?
    state.analystWorkspaceState.currentStageInterval.interval.activityInterval.analystActivity : undefined,
  selectedSdIds: state.analystWorkspaceState.selectedSdIds,
  openEventId: state.analystWorkspaceState.openEventId,
  measurementMode: state.analystWorkspaceState.measurementMode,
  sdIdsToShowFk: state.analystWorkspaceState.sdIdsToShowFk

});

// Map actions dispatch callbacks into this component as props
const mapDispatchToProps = (dispatch): Partial<SignalDetectionsReduxProps> =>
  bindActionCreators(
    {
      setSelectedSdIds: AnalystWorkspaceActions.setSelectedSdIds,
      setSdIdsToShowFk: AnalystWorkspaceActions.setSdIdsToShowFk,
      setMeasurementModeEntries: AnalystWorkspaceOperations.setMeasurementModeEntries,
    } as any,
    dispatch
  );

/**
 * Higher-order component react-redux(react-apollo(SignalDetectionList))
 */
export const ReduxApolloSignalDetectionsContainer: React.ComponentClass<Pick<{}, never>> = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps),
  graphql(StationQueries.defaultStationsQuery, { name: 'defaultStationsQuery' }),
  graphql(EventQueries.eventsInTimeRangeQuery, {
    options: (props: SignalDetectionsProps) => {
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
      };
    },
    skip: (props: SignalDetectionsProps) => !props.currentTimeInterval,
    name: 'eventsInTimeRangeQuery'
  }),
  graphql(SignalDetectionQueries.signalDetectionsByStationQuery, {
    options: (props: SignalDetectionsProps) => {
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
    skip: (props: SignalDetectionsProps) => !props.currentTimeInterval ||
      !props.defaultStationsQuery.defaultStations,
    name: 'signalDetectionsByStationQuery'
  }),
  graphql(SignalDetectionMutations.updateDetectionsMutation, { name: 'updateDetections' }),
  graphql(SignalDetectionMutations.rejectDetectionsMutation, { name: 'rejectDetectionHypotheses' }),
  graphql(EventMutations.changeSignalDetectionAssociationsMutation, { name: 'changeSignalDetectionAssociations' }),
  graphql(EventMutations.createEventMutation, { name: 'createEvent' }),

)(SignalDetections);
