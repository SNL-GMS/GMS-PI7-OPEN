import * as lodash from 'lodash';
import * as React from 'react';
import { compose, graphql } from 'react-apollo';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';
import { autoOpenEvent } from '~analyst-ui/common/actions/event-actions';
import { systemConfig } from '~analyst-ui/config';
import { userPreferences } from '~analyst-ui/config/';
import {
  EventMutations,
  EventQueries,
  EventTypes,
  QcMaskMutations,
  QcMaskQueries,
  QcMaskTypes,
  SignalDetectionMutations,
  SignalDetectionQueries,
  SignalDetectionTypes,
  StationQueries,
  StationTypes,
  WaveformQueries
} from '~graphql/';
import { AnalystWorkspaceActions, AnalystWorkspaceOperations } from '~state/';
import { AppState } from '~state/types';
import { WaveformDisplayProps, WaveformDisplayReduxProps } from './types';
import { WaveformDisplay } from './waveform-display-component';

// map parts of redux state into this component as props
const mapStateToProps = (state: AppState): Partial<WaveformDisplayReduxProps> => ({
  apolloClient: state.apolloClient.client,
  currentTimeInterval: (state.analystWorkspaceState.currentStageInterval) ?
    state.analystWorkspaceState.currentStageInterval.interval.timeInterval : undefined,
  createSignalDetectionPhase: state.analystWorkspaceState.createSignalDetectionPhase,
  analystActivity: (state.analystWorkspaceState.currentStageInterval) ?
    state.analystWorkspaceState.currentStageInterval.interval.activityInterval.analystActivity : undefined,
  currentOpenEventId: state.analystWorkspaceState.openEventId,
  selectedSdIds: state.analystWorkspaceState.selectedSdIds,
  waveformSortType: state.analystWorkspaceState.selectedSortType,
  measurementMode: state.analystWorkspaceState.measurementMode,
  sdIdsToShowFk: state.analystWorkspaceState.sdIdsToShowFk,
  channelFilters: state.analystWorkspaceState.channelFilters
});

// map actions dispatch callbacks into this component as props
const mapDispatchToProps = (dispatch): Partial<WaveformDisplayReduxProps> =>
  bindActionCreators(
    {
      setMode: AnalystWorkspaceOperations.setMode,
      setCreateSignalDetectionPhase: AnalystWorkspaceActions.setCreateSignalDetectionPhase,
      setOpenEventId: AnalystWorkspaceOperations.setOpenEventId,
      setSelectedSdIds: AnalystWorkspaceActions.setSelectedSdIds,
      setSdIdsToShowFk: AnalystWorkspaceActions.setSdIdsToShowFk,
      setSelectedSortType: AnalystWorkspaceActions.setSelectedSortType,
      setChannelFilters: AnalystWorkspaceActions.setChannelFilters,
      setMeasurementModeEntries: AnalystWorkspaceOperations.setMeasurementModeEntries,
    } as any,
    dispatch
  );

/**
 * higher-order component react-redux(react-apollo(WaveformDisplay))
 */
export const ReduxApolloWaveformDisplay: React.ComponentClass<Pick<{}, never>> = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps),
  graphql(StationQueries.defaultStationsQuery, { name: 'defaultStationsQuery' }),
  graphql(WaveformQueries.defaultWaveformFiltersQuery, { name: 'defaultWaveformFiltersQuery' }),
  // Order matters need to initialize updateEvents mutation before graphql autoOpenEvent below is executed
  graphql(EventMutations.updateEventsMutation, { name: 'updateEvents' }),
  graphql(EventQueries.eventsInTimeRangeQuery, {
    options: (props: WaveformDisplayProps) => {
      // get events before & after the current interval as well
      const variables: EventTypes.EventsInTimeRangeQueryArgs = {
        timeRange: {
          startTime: props.currentTimeInterval.startTimeSecs -
            systemConfig.additionalEventDataToLoadInitially(props.analystActivity),
          endTime: props.currentTimeInterval.endTimeSecs +
            systemConfig.additionalDataToLoadOnInitialLoad(props.analystActivity),
        }
      };
      return {
        variables,
        refetchQueries: [
          'distanceToSourceForDefaultStationsQuery',
        ],
        awaitRefetchQueries: true,
        onCompleted: (data: { eventsInTimeRange: EventTypes.Event[] }) => {
          autoOpenEvent(
            data.eventsInTimeRange,
            props.currentTimeInterval,
            props.currentOpenEventId,
            props.analystActivity,
            (event: EventTypes.Event) => props.setOpenEventId(event),
            props.updateEvents
          );
        }
      };
    },
    skip: (props: WaveformDisplayProps) => !props.currentTimeInterval,
    name: 'eventsInTimeRangeQuery'
  }),
  graphql(SignalDetectionQueries.signalDetectionsByStationQuery, {
    options: (props: WaveformDisplayProps) => {
      // get signal detections in the current interval
      const variables: SignalDetectionTypes.SignalDetectionsByStationQueryArgs = {
        stationIds: props.defaultStationsQuery.defaultStations.map(station => station.id),
        timeRange: {
          startTime: props.currentTimeInterval.startTimeSecs -
            systemConfig.additionalDataToLoadOnInitialLoad(props.analystActivity),
          endTime: props.currentTimeInterval.endTimeSecs +
            systemConfig.additionalDataToLoadOnInitialLoad(props.analystActivity),
        }
      };
      return {
        variables
      };
    },
    skip: (props: WaveformDisplayProps) =>
      !props.currentTimeInterval || !props.defaultStationsQuery.defaultStations,
    name: 'signalDetectionsByStationQuery'
  }),
  graphql(QcMaskQueries.qcMasksByChannelIdQuery, {
    options: (props: WaveformDisplayProps) => {
      const channelIds: string[] = [];
      if (props.defaultStationsQuery.defaultStations) {
        props.defaultStationsQuery.defaultStations.forEach(station => {
          channelIds.push(station.defaultChannel.id);
          // tslint:disable-next-line:max-line-length
          channelIds.push(...lodash.flatten(station.sites.map(site => site.channels.map(channel => channel.id))));
        });
      }
      // get signal detections in the current interval
      const variables: QcMaskTypes.QcMasksByChannelIdQueryArgs = {
        timeRange: {
          startTime: props.currentTimeInterval.startTimeSecs,
          endTime: props.currentTimeInterval.endTimeSecs,
        },
        channelIds
      };
      return {
        variables
      };
    },
    skip: (props: WaveformDisplayProps) =>
      !props.currentTimeInterval || !props.defaultStationsQuery.defaultStations,
    name: 'qcMasksByChannelIdQuery'
  }),
  graphql(StationQueries.distanceToSourceForDefaultStationsQuery, {
    options: (props: WaveformDisplayProps) => {
      // get events before & after the current interval as well
      const variables: StationTypes.DistanceToSourceForDefaultStationsQueryArgs = {
        distanceToSourceInput: {
          sourceId: props.currentOpenEventId,
          sourceType: 'Event',
          distanceUnits: userPreferences.distanceUnits
        }
      };
      return {
        variables
      };
    },
    skip: (props: WaveformDisplayProps) => !props.currentTimeInterval || !props.currentOpenEventId,
    name: 'distanceToSourceForDefaultStationsQuery'
  }),
  graphql(SignalDetectionMutations.createDetectionMutation, { name: 'createDetection' }),
  graphql(SignalDetectionMutations.updateDetectionsMutation, { name: 'updateDetections' }),
  graphql(SignalDetectionMutations.rejectDetectionsMutation, { name: 'rejectDetectionHypotheses' }),
  graphql(EventMutations.changeSignalDetectionAssociationsMutation, { name: 'changeSignalDetectionAssociations' }),
  graphql(EventMutations.createEventMutation, { name: 'createEvent' }),

  graphql(QcMaskMutations.createQcMaskMutation, { name: 'createQcMask' }),
  graphql(QcMaskMutations.updateQcMaskMutation, { name: 'updateQcMask' }),
  graphql(QcMaskMutations.rejectQcMaskMutation, { name: 'rejectQcMask' })

)(WaveformDisplay);
