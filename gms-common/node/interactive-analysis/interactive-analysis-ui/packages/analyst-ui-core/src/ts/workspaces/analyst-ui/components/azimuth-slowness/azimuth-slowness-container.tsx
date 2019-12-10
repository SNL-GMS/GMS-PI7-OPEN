
import * as React from 'react';
import { compose, graphql } from 'react-apollo';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';
import { systemConfig, userPreferences } from '~analyst-ui/config';
import {
  EventQueries,
  EventTypes,
  FkMutations,
  SignalDetectionQueries,
  SignalDetectionTypes,
  StationQueries,
  StationTypes,
  WaveformQueries
} from '~graphql/';
import { AnalystWorkspaceActions } from '~state/';
import { AppState } from '~state/types';
import { AzimuthSlowness } from './azimuth-slowness-component';
import { AzimuthSlownessProps, AzimuthSlownessReduxProps } from './types';

/**
 * Container component for Azimuth Slowness
 * Handles mapping of state/props through redux/apollo
 */

/**
 *  Mapping between the current redux state and props for the Azimuth Slowness Display
 */
const mapStateToProps = (state: AppState): Partial<AzimuthSlownessReduxProps> => ({
  currentStageInterval: state.analystWorkspaceState.currentStageInterval,
  currentTimeInterval: (state.analystWorkspaceState.currentStageInterval) ?
    state.analystWorkspaceState.currentStageInterval.interval.timeInterval : undefined,
  analystActivity: (state.analystWorkspaceState.currentStageInterval) ?
    state.analystWorkspaceState.currentStageInterval.interval.activityInterval.analystActivity : undefined,
  selectedSdIds: state.analystWorkspaceState.selectedSdIds,
  openEventId: state.analystWorkspaceState.openEventId,
  selectedSortType: state.analystWorkspaceState.selectedSortType,
  sdIdsToShowFk: state.analystWorkspaceState.sdIdsToShowFk,
  measurementMode: state.analystWorkspaceState.measurementMode,
  channelFilters: state.analystWorkspaceState.channelFilters,
  client: state.apolloClient.client
});

/**
 * Map actions dispatch callbacks into this component as props
 */
const mapDispatchToProps = (dispatch): Partial<AzimuthSlownessReduxProps> =>
  bindActionCreators(
    {
      setSelectedSdIds: AnalystWorkspaceActions.setSelectedSdIds,
      setSdIdsToShowFk: AnalystWorkspaceActions.setSdIdsToShowFk
    } as any,
    dispatch
  );

/**
 * The higher-order component react-redux(react-apollo(AzimuthSlowness))
 */
export const ReduxApolloAzimuthSlowness: React.ComponentClass<Pick<{}, never>> = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps),
  graphql(StationQueries.defaultStationsQuery, { name: 'defaultStationsQuery' }),
  graphql(WaveformQueries.defaultWaveformFiltersQuery, { name: 'defaultWaveformFiltersQuery' }),
  graphql(SignalDetectionQueries.signalDetectionsByStationQuery, {
    options: (props: AzimuthSlownessProps) => {
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
    skip: (props: AzimuthSlownessProps) => !props.currentTimeInterval ||
    !props.defaultStationsQuery.defaultStations,
    name: 'signalDetectionsByStationQuery'
  }),
  graphql(StationQueries.distanceToSourceForDefaultStationsQuery, {
    options: (props: AzimuthSlownessProps) => {
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
    skip: (props: AzimuthSlownessProps) => !props.currentTimeInterval || !props.openEventId,
    name: 'distanceToSourceForDefaultStationsQuery'
  }),
  graphql(EventQueries.eventsInTimeRangeQuery, {
    options: (props: AzimuthSlownessProps) => {
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
    skip: (props: AzimuthSlownessProps) => !props.currentTimeInterval,
    name: 'eventsInTimeRangeQuery'
  }),

  graphql(FkMutations.computeFksMutation, { name: 'computeFks' }),
  graphql(FkMutations.setFkWindowLeadMutation, { name: 'setWindowLead' }),
  graphql(FkMutations.markFksReviewedMutation, { name: 'markFksReviewed' })

)(AzimuthSlowness);
