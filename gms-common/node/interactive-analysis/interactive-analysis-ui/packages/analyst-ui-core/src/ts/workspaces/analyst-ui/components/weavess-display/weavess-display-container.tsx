import * as React from 'react';
import { compose, graphql } from 'react-apollo';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';
import {
  EventMutations,
  QcMaskMutations,
  SignalDetectionMutations,
} from '~graphql/';
import { AnalystWorkspaceActions, AnalystWorkspaceOperations } from '~state/';
import { AppState } from '~state/types';
import { WeavessDisplayComponentProps, WeavessDisplayProps } from './types';
import { WeavessDisplay } from './weavess-display-component';

// map parts of redux state into this component as props
const mapStateToProps = (state: AppState): Partial<WeavessDisplayProps> => ({
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
const mapDispatchToProps = (dispatch): Partial<WeavessDisplayProps> =>
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
 * higher-order component react-redux(react-apollo(WeavessDisplay))
 */
export const ReduxApolloWeavessDisplay: React.ComponentClass<WeavessDisplayComponentProps, never> = compose(
  graphql(SignalDetectionMutations.createDetectionMutation, { name: 'createDetection', withRef: true }),
  graphql(SignalDetectionMutations.updateDetectionsMutation, { name: 'updateDetections', withRef: true }),
  graphql(SignalDetectionMutations.rejectDetectionsMutation, { name: 'rejectDetectionHypotheses', withRef: true }),
  graphql(
    EventMutations.changeSignalDetectionAssociationsMutation,
    { name: 'changeSignalDetectionAssociations', withRef: true }),
  graphql(EventMutations.createEventMutation, { name: 'createEvent', withRef: true }),
  graphql(QcMaskMutations.createQcMaskMutation, { name: 'createQcMask', withRef: true }),
  graphql(QcMaskMutations.updateQcMaskMutation, { name: 'updateQcMask', withRef: true }),
  graphql(QcMaskMutations.rejectQcMaskMutation, { name: 'rejectQcMask', withRef: true }),
  ReactRedux.connect(mapStateToProps, mapDispatchToProps, null, { withRef: true })
)(WeavessDisplay);
