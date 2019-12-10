import * as Immutable from 'immutable';
import * as lodash from 'lodash';
import { EventTypes } from '~graphql/';
import { AppState } from '~state/types';
import { Actions, Internal } from './actions';
import { AnalystWorkspaceState, Mode, StageInterval, WaveformSortType } from './types';

/**
 * Redux operation for setting the mode.
 * 
 * @param mode the mode to set
 */
const setMode = (mode: Mode) => (
  dispatch: any, getState: () => AppState
) => {
  dispatch(Internal.setMode(mode));
};

/**
 * Redux operation for setting the measurement mode entries.
 * 
 * @param entries the measurement mode entries to set
 */
const setMeasurementModeEntries = (
  entries: Immutable.Map<string, boolean>) => (
  dispatch: any, getState: () => AppState
) => {
  dispatch(Internal.setMeasurementModeEntries(entries));
};

/**
 * Redux operation for setting the current stage interval.
 * 
 * @param stageInterval the stage interval to set
 */
const setCurrentStageInterval = (stageInterval: StageInterval) => (
  dispatch: any, getState: () => AppState
) => {
  const state: AnalystWorkspaceState = getState().analystWorkspaceState;

  const hasCurrentIntervalChanged =
    !state.currentStageInterval || !stageInterval || (state.currentStageInterval.id !== stageInterval.id ||
      !lodash.isEqual(state.currentStageInterval.interval.timeInterval, stageInterval.interval.timeInterval));

  if (!lodash.isEqual(state.currentStageInterval, stageInterval)) {
    dispatch(Internal.setCurrentStageInterval(stageInterval));
  }

  // clear out the following
  // if the processing stage interval id (or time interval) has changed
  if (hasCurrentIntervalChanged) {
    if (state.selectedSdIds.length !== 0) {
      dispatch(Actions.setSelectedSdIds([]));
    }

    if (state.openEventId !== undefined && state.openEventId !== null) {
      dispatch(Internal.setOpenEventId(undefined));
    }

    if (state.selectedEventIds.length !== 0) {
      dispatch(Actions.setSelectedEventIds([]));
    }

    if (state.sdIdsToShowFk.length !== 0) {
      dispatch(Actions.setSdIdsToShowFk([]));
    }

    if (state.measurementMode.mode !== Mode.DEFAULT) {
      dispatch(setMode(Mode.DEFAULT));
    }

    if (state.measurementMode.entries.size !== 0) {
      dispatch(Internal.setMeasurementModeEntries(Immutable.Map()));
    }

    if (state.selectedSortType !== WaveformSortType.stationName) {
      dispatch(Actions.setSelectedSortType(WaveformSortType.stationName));
    }
  }
};

/**
 * Redux operation for setting the current open event id.
 * 
 * @param event the event to set
 */
const setOpenEventId = (event: EventTypes.Event | undefined) => (
  dispatch: any, getState: () => AppState
) => {
  const state: AnalystWorkspaceState = getState().analystWorkspaceState;

  if (state.currentStageInterval && event) {
    if (state.openEventId !== event.id) {
      dispatch(Internal.setOpenEventId(event.id));
    }

    if (!lodash.isEqual(state.selectedEventIds, [event.id])) {
      dispatch(Actions.setSelectedEventIds([event.id]));
    }

    if (state.selectedSortType !== WaveformSortType.distance) {
      dispatch(Actions.setSelectedSortType(WaveformSortType.distance));
    }

  } else {
    if (state.openEventId !== undefined && state.openEventId !== null) {
      dispatch(Internal.setOpenEventId(undefined));
    }

    if (state.selectedEventIds.length !== 0) {
      dispatch(Actions.setSelectedEventIds([]));
    }

    if (state.selectedSortType !== WaveformSortType.stationName) {
      dispatch(Actions.setSelectedSortType(WaveformSortType.stationName));
    }

    if (state.measurementMode.entries.size !== 0) {
      dispatch(Internal.setMeasurementModeEntries(Immutable.Map()));
    }
  }

  if (state.selectedSdIds.length !== 0) {
    dispatch(Actions.setSelectedSdIds([]));
  }

  if (state.sdIdsToShowFk.length !== 0) {
    dispatch(Actions.setSdIdsToShowFk([]));
  }

  if (state.measurementMode.mode !== Mode.DEFAULT) {
    dispatch(setMode(Mode.DEFAULT));
  }
};

/**
 * Redux operations (public).
 */
export const Operations = {
  setCurrentStageInterval,
  setOpenEventId,
  setMode,
  setMeasurementModeEntries
};
