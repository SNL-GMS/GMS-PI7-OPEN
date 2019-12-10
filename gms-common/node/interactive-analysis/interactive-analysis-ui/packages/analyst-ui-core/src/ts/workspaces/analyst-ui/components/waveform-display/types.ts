import * as Gl from '@gms/golden-layout';
import {
  WeavessTypes
} from '@gms/weavess';
import { ApolloClient } from 'apollo-client';
import * as Immutable from 'immutable';
import { ChildMutateProps, MutationFunc } from 'react-apollo';
import { QcMaskDisplayFilters } from '~analyst-ui/config';
import {
  CommonTypes,
  EventTypes,
  QcMaskTypes,
  SignalDetectionTypes,
  StationTypes,
  WaveformTypes
} from '~graphql/';
import {
  AnalystActivity,
  MeasurementMode,
  Mode,
  TimeInterval,
  WaveformSortType
} from '~state/analyst-workspace/types';

export enum PhaseAlignments {
  PREDICTED_PHASE = 'Predicted',
  OBSERVED_PHASE = 'Observed'
}

export enum AlignWaveformsOn {
  TIME = 'Time',
  PREDICTED_PHASE = 'Predicted',
  OBSERVED_PHASE = 'Observed'
}

/**
 * Waveform Display display state.
 * keep track of selected channels & signal detections
 */
export interface WaveformDisplayState {
  stations: WeavessTypes.Station[];
  currentTimeInterval: TimeInterval;
  // because the user may load more waveform
  // data than the currently opened time interval
  viewableInterval: TimeInterval;
  loadingWaveforms: boolean;
  loadingWaveformsPercentComplete: number;
  maskDisplayFilters: QcMaskDisplayFilters;
  analystNumberOfWaveforms: number;
  currentOpenEventId: string;
  showPredictedPhases: boolean;
  alignWaveformsOn: AlignWaveformsOn;
  phaseToAlignOn: CommonTypes.PhaseType | undefined;
  isMeasureWindowVisible: boolean;
}

/**
 * Props mapped in from Redux state
 */
export interface WaveformDisplayReduxProps {
  // passed in from golden-layout
  glContainer?: Gl.Container;
  apolloClient: ApolloClient<any>;
  currentTimeInterval: TimeInterval;
  createSignalDetectionPhase: CommonTypes.PhaseType;
  currentOpenEventId: string;
  selectedSdIds: string[];
  waveformSortType: WaveformSortType;
  analystActivity: AnalystActivity;
  measurementMode: MeasurementMode;
  sdIdsToShowFk: string[];
  channelFilters: Immutable.Map<string, WaveformTypes.WaveformFilter>;

  // callbacks
  setMode(mode: Mode): void;
  setCreateSignalDetectionPhase(phase: CommonTypes.PhaseType): void;
  setOpenEventId(event: EventTypes.Event): void;
  setSelectedSdIds(idx: string[]): void;
  setSdIdsToShowFk(signalDetections: string[]): void;
  setSelectedSortType(selectedSortType: WaveformSortType): void;
  setChannelFilters(filters: Immutable.Map<string, WaveformTypes.WaveformFilter>);
  setMeasurementModeEntries(entries: Immutable.Map<string, boolean>): void;
}

/**
 * Mutations used by the Waveform display
 */
export interface WaveformDisplayMutations {
  // {} because we don't care about mutation results for now, handling that through subscriptions
  createDetection: MutationFunc<{}>;
  updateDetections: MutationFunc<{}>;
  rejectDetectionHypotheses: MutationFunc<{}>;
  createQcMask: MutationFunc<{}>;
  updateQcMask: MutationFunc<{}>;
  rejectQcMask: MutationFunc<{}>;
  updateEvents: MutationFunc<{}>;
  createEvent: MutationFunc<{}>;
  changeSignalDetectionAssociations: MutationFunc<{}>;
}

/**
 * Consolidated props type for waveform display.
 */
export type WaveformDisplayProps = WaveformDisplayReduxProps
  & ChildMutateProps<WaveformDisplayMutations>
  & StationTypes.DefaultStationsQueryProps
  & StationTypes.DistanceToSourceForDefaultStationsQueryProps
  & WaveformTypes.DefaultWaveformFiltersQueryProps
  & SignalDetectionTypes.SignalDetectionsByStationQueryProps
  & EventTypes.EventsInTimeRangeQueryProps
  & QcMaskTypes.QcMasksByChannelIdQueryProps;

// Enum to clarify pan button interactions
export enum PanType {
  Left,
  Right
}
