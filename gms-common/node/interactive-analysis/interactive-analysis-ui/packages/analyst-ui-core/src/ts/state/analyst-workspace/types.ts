import * as Immutable from 'immutable';
import { CommonTypes, SignalDetectionTypes, WaveformTypes } from '../../graphql/';
import { ActionWithPayload } from '../util/action-helper';

export type SET_MODE = ActionWithPayload<Mode>;
export type SET_CURRENT_STAGE_INTERVAL = ActionWithPayload<StageInterval>;
export type SET_CREATE_SIGNAL_DETECTION_PHASE = ActionWithPayload<CommonTypes.PhaseType>;
export type SET_SORT_TYPE = ActionWithPayload<string[]>;
export type SET_SELECTED_EVENT_HYP_IDS = ActionWithPayload<string>;
export type SET_OPEN_EVENT_HYP_ID = ActionWithPayload<string[]>;
export type SET_SELECTED_SD_IDS = ActionWithPayload<SignalDetectionTypes.SignalDetection[]>;
export type SET_FK_SDS_TO_SHOW = ActionWithPayload<WaveformSortType>;
export type SET_CHANNEL_FILTERS = ActionWithPayload<Immutable.Map<string, WaveformTypes.WaveformFilter>>;
export type SET_MEASUREMENT_MODE_ENTRIES = ActionWithPayload<Immutable.Map<string, boolean>>;

/**
 * A simple time interval
 */
export interface TimeInterval {
  startTimeSecs: number;
  endTimeSecs: number;
}

/**
 * The display mode options for the waveform display.
 */
export enum Mode {
  DEFAULT = 'Default',
  MEASUREMENT = 'Measurement'
}

/**
 * System wide analyst activity
 */
export enum AnalystActivity {
  eventRefinement = 'Event Refinement',
  globalScan = 'Global Scan',
  regionalScan = 'Regional Scan'
}

/**
 * Available waveform sort types.
 */
export enum WaveformSortType {
  distance = 'Distance',
  stationName = 'Station Name'
}

/**
 * Stage interval.
 */
export interface StageInterval {
  id: string;
  name: string;
  interval: {
    id: string;
    timeInterval: TimeInterval;
    activityInterval: {
      id: string;
      name: string;
      analystActivity: AnalystActivity;
    };
  };
}

/**
 * Measurement mode state.
 */
export interface MeasurementMode {
  /** The display mode */
  mode: Mode;

  /** 
   * Measurement entries that are manually added or hidden by the user.
   * The key is the signal detection id
   */
  entries: Immutable.Map<string, boolean>;
}

/**
 * Analyst workspace state.
 */
export interface AnalystWorkspaceState {
  currentStageInterval: StageInterval;
  createSignalDetectionPhase: CommonTypes.PhaseType;
  selectedEventIds: string[];
  openEventId: string;
  selectedSdIds: string[];
  sdIdsToShowFk: string[];
  selectedSortType: WaveformSortType;
  channelFilters: Immutable.Map<string, WaveformTypes.WaveformFilter>;
  measurementMode: MeasurementMode;
}
