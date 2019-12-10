import * as Immutable from 'immutable';
import * as Redux from 'redux';
import { PhaseType } from '../../graphql/common/types';
import { WaveformFilter } from '../../graphql/waveform/types';
import { Actions, Internal } from './actions';
import * as Types from './types';

/**
 * Redux reducer for setting the mode.
 * 
 * @param state the state to set
 * @param action the redux action
 */
const setMode = (state: Types.Mode = Types.Mode.DEFAULT, action: Types.SET_MODE): Types.Mode => {
  if (Internal.setMode.test(action)) {
    return (action.payload) ? action.payload : null;
  }
  return state;
};

/**
 * Redux reducer for setting the current stage interval.
 * 
 * @param state the state to set
 * @param action the redux action
 */
const setCurrentStageInterval = (
  state: Types.StageInterval = null,
  action: Types.SET_CURRENT_STAGE_INTERVAL
): Types.StageInterval => {
  if (Internal.setCurrentStageInterval.test(action)) {
    return (action.payload) ? action.payload : null;
  }
  return state;
};

/**
 * Redux reducer for setting the signal detection creation phase.
 * The selected phase type that will be used for the creation of
 * a new a signal detection.
 * 
 * @param state the state to set
 * @param action the redux action
 */
const setCreateSignalDetectionPhase = (
  state: PhaseType = PhaseType.P,
  action: Types.SET_CREATE_SIGNAL_DETECTION_PHASE
): PhaseType => {
  if (Actions.setCreateSignalDetectionPhase.test(action)) {
    return (action.payload) ? action.payload : null;
  }
  return state;
};

/**
 * Redux reducer for setting the selected event ids.
 * 
 * @param state the state to set
 * @param action the redux action
 */
const setSelectedEventIds = (
  state: string[] = [],
  action: Types.SET_SELECTED_EVENT_HYP_IDS
): string[] => {
  if (Actions.setSelectedEventIds.test(action)) {
    return (action.payload) ? action.payload : null;
  }
  return state;
};

/**
 * Redux reducer for setting the current open event id.
 * 
 * @param state the state to set
 * @param action the redux action
 */
const setOpenEventId = (
  state: string = null,
  action: Types.SET_OPEN_EVENT_HYP_ID
): string => {
  if (Internal.setOpenEventId.test(action)) {
    return (action.payload) ? action.payload : null;
  }
  return state;
};

/**
 * Redux reducer for setting the selected signal detection ids.
 * 
 * @param state the state to set
 * @param action the redux action
 */
const setSelectedSdIds = (
  state: string[] = [],
  action: Types.SET_SELECTED_SD_IDS
): string[] => {
  if (Actions.setSelectedSdIds.test(action)) {
    return (action.payload) ? action.payload : null;
  }
  return state;
};

/**
 * Redux reducer for setting the signal detection ids that
 * have been marked to show FK.
 * 
 * @param state the state to set
 * @param action the redux action
 */
const setSdIdsToShowFk = (
  state: string[] = [],
  action: Types.SET_FK_SDS_TO_SHOW
): string[] => {
  if (Actions.setSdIdsToShowFk.test(action)) {
    return (action.payload) ? action.payload : null;
  }
  return state;
};

/**
 * Redux reducer for setting the selected sort type.
 * 
 * @param state the state to set
 * @param action the redux action
 */
const setSelectedSortType = (
  state: Types.WaveformSortType = Types.WaveformSortType.stationName,
  action: Types.SET_SORT_TYPE
): Types.WaveformSortType => {
  if (Actions.setSelectedSortType.test(action)) {
    return (action.payload) ? action.payload : null;
  }
  return state;
};

/**
 * Redux reducer for setting the channel filters.
 * (selected waveform filter for a give channel id)
 * 
 * @param state the state to set
 * @param action the redux action
 */
const setChannelFilters = (
  state: Immutable.Map<string, WaveformFilter> = Immutable.Map<string, WaveformFilter>(),
  action: Types.SET_CHANNEL_FILTERS
): Immutable.Map<string, WaveformFilter> => {
  if (Actions.setChannelFilters.test(action)) {
    return (action.payload) ? action.payload : null;
  }
  return state;
};

/**
 * Redux reducer for setting the measurement mode entries.
 * Map of signal detection ids to boolean value indicating if the
 * amplitude measurement should be displayed (visible).
 * 
 * @param state the state to set
 * @param action the redux action
 */
const setMeasurementModeEntries = (
  state: Immutable.Map<string, boolean> = Immutable.Map<string, boolean>(),
  action: Types.SET_MEASUREMENT_MODE_ENTRIES
): Immutable.Map<string, boolean> => {
  if (Internal.setMeasurementModeEntries.test(action)) {
    return (action.payload) ? action.payload : null;
  }
  return state;
};

/**
 * Measurement mode reducer.
 */
const measurementModeReducer: Redux.Reducer<Types.MeasurementMode, Redux.AnyAction> =
  Redux.combineReducers({
    mode: setMode,
    entries: setMeasurementModeEntries,
  });

/**
 * Analyst workspace reducer.
 */
export const Reducer:
  Redux.Reducer<Types.AnalystWorkspaceState, Redux.AnyAction> = Redux.combineReducers({
    currentStageInterval: setCurrentStageInterval,
    createSignalDetectionPhase: setCreateSignalDetectionPhase,
    selectedEventIds: setSelectedEventIds,
    openEventId: setOpenEventId,
    selectedSdIds: setSelectedSdIds,
    sdIdsToShowFk: setSdIdsToShowFk,
    selectedSortType: setSelectedSortType,
    channelFilters: setChannelFilters,
    measurementMode: measurementModeReducer
  });
