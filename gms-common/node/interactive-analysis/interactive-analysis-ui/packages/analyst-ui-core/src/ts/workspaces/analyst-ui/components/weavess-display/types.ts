import * as Gl from '@gms/golden-layout';
import {
  WaveformDisplayProps as WeavessProps
} from '@gms/weavess/dist/types/components/waveform-display/types';
import { ApolloClient } from 'apollo-client';
import * as Immutable from 'immutable';
import { ChildMutateProps, MutationFunc } from 'react-apollo';
import {
  CommonTypes,
  EventTypes,
  QcMaskTypes,
  SignalDetectionTypes,
  StationTypes,
  WaveformTypes
} from '~graphql/';
import {
  AnalystActivity, MeasurementMode, Mode, TimeInterval, WaveformSortType
} from '~state/analyst-workspace/types';

export interface WeavessDisplayState {
  selectedChannels: string[];
  qcMaskModifyInterval?: TimeInterval;
  selectedQcMask?: QcMaskTypes.QcMask;
}

interface WeavessDisplayReduxProps {
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
  setSelectedSdIds(id: string[]): void;
  setSdIdsToShowFk(signalDetections: string[]): void;
  setSelectedSortType(selectedSortType: WaveformSortType): void;
  setChannelFilters(filters: Immutable.Map<string, WaveformTypes.WaveformFilter>);
  setMeasurementModeEntries(entries: Immutable.Map<string, boolean>): void;
}

interface WeavessDisplayMutations {
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

export interface WeavessDisplayComponentProps {
  weavessProps: Partial<WeavessProps>;
  defaultWaveformFilters: WaveformTypes.WaveformFilter[];
  defaultStations: StationTypes.ProcessingStation[];
  eventsInTimeRange: EventTypes.Event[];
  signalDetectionsByStation: SignalDetectionTypes.SignalDetection[];
  qcMasksByChannelId: QcMaskTypes.QcMask[];
}

export type WeavessDisplayProps = WeavessDisplayReduxProps
  & ChildMutateProps<WeavessDisplayMutations>
  & WeavessDisplayComponentProps;
