import * as React from 'react';
import { compose, graphql } from 'react-apollo';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';
import { autoOpenEvent } from '~analyst-ui/common/actions/event-actions';
import { systemConfig } from '~analyst-ui/config';
import { EventMutations, EventQueries,
  EventTypes, SignalDetectionQueries, SignalDetectionTypes, StationQueries } from '~graphql/';
import { AnalystWorkspaceActions, AnalystWorkspaceOperations } from '~state/';
import { AppState } from '~state/types';
import { SignalDetectionsProps } from '../signal-detections/types';
import { Events } from './events-component';
import { EventsProps, EventsReduxProps } from './types';

// Map parts of redux state into this component as props
const mapStateToProps = (state: AppState): Partial<EventsReduxProps> => ({
  currentTimeInterval: (state.analystWorkspaceState.currentStageInterval) ?
    state.analystWorkspaceState.currentStageInterval.interval.timeInterval : undefined,
  analystActivity: (state.analystWorkspaceState.currentStageInterval) ?
    state.analystWorkspaceState.currentStageInterval.interval.activityInterval.analystActivity : undefined,
  openEventId: state.analystWorkspaceState.openEventId,
  selectedEventIds: state.analystWorkspaceState.selectedEventIds
});

// Map actions dispatch callbacks into this component as props
const mapDispatchToProps = (dispatch): Partial<EventsReduxProps> =>
  bindActionCreators(
    {
      setOpenEventId: AnalystWorkspaceOperations.setOpenEventId,
      setSelectedEventIds: AnalystWorkspaceActions.setSelectedEventIds
    } as any,
    dispatch
  );

/**
 * Higher-order component react-redux(react-apollo(EventList))
 */
export const ReduxApolloEventsContainer: React.ComponentClass<Pick<{}, never>> = compose(
  ReactRedux.connect(mapStateToProps, mapDispatchToProps),
  // Order matters need to initialize updateEvents mutation before graphql autoOpenEvent below is executed
  graphql(StationQueries.defaultStationsQuery, { name: 'defaultStationsQuery' }),
  graphql(EventMutations.updateEventsMutation, { name: 'updateEvents' }),
  graphql(EventQueries.eventsInTimeRangeQuery, {
    options: (props: EventsProps) => {
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
    skip: (props: EventsProps) => !props.currentTimeInterval,
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
)(Events);
