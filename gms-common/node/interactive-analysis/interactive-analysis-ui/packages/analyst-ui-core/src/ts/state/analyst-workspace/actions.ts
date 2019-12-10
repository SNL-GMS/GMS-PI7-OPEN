// tslint:disable: max-line-length
import * as Immutable from 'immutable';
import { PhaseType } from '../../graphql/common/types';
import { WaveformFilter } from '../../graphql/waveform/types';
import { ActionCreator, actionCreator } from '../util/action-helper';
import { Mode, StageInterval, WaveformSortType } from './types';

const setMode: ActionCreator<Mode> = actionCreator('SET_MODE');
const setCurrentStageInterval: ActionCreator<StageInterval> = actionCreator('SET_CURRENT_STAGE_INTERVAL');
const setCreateSignalDetectionPhase: ActionCreator<PhaseType> = actionCreator('SET_CREATE_SIGNAL_DETECTION_PHASE');
const setOpenEventId: ActionCreator<string> = actionCreator('SET_OPEN_EVENT_ID');
const setSelectedEventIds: ActionCreator<string[]> = actionCreator('SET_SELECTED_EVENT_IDS');
const setSelectedSdIds: ActionCreator<string[]> = actionCreator(' SET_SELECTED_SD_IDS');
const setSdIdsToShowFk: ActionCreator<string[]> = actionCreator('SET_SD_IDS_TO_SHOW_FK');
const setSelectedSortType: ActionCreator<WaveformSortType> = actionCreator('SET_SORT_TYPE');
const setChannelFilters: ActionCreator<Immutable.Map<string, WaveformFilter>> = actionCreator('SET_CHANNEL_FILTERS');
const setMeasurementModeEntries: ActionCreator<Immutable.Map<string, boolean>> = actionCreator('SET_MEASUREMENT_MODE_ENTRIES');

/**
 * Redux internal actions: should only be called by `operations`. (private - but not strictly forced)
 */
export const Internal = {
  setMode,
  setCurrentStageInterval,
  setOpenEventId,
  setMeasurementModeEntries
};

/**
 * Redux actions (public).
 */
export const Actions = {
  setCreateSignalDetectionPhase,
  setSelectedSortType,
  setSelectedSdIds,
  setSelectedEventIds,
  setSdIdsToShowFk,
  setChannelFilters,
};
