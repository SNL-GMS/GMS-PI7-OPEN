import * as Gl from '@gms/golden-layout';
import ApolloClient from 'apollo-client';
import * as Immutable from 'immutable';
import { ChildProps, MutationFunc } from 'react-apollo';
import { EventTypes, FkTypes, SignalDetectionTypes, StationTypes, WaveformTypes } from '~graphql/';
import { FeaturePrediction } from '~graphql/event/types';
import { FkConfiguration, FkFrequencyThumbnail, FrequencyBand } from '~graphql/fk/types';
import { DistanceToSource } from '~graphql/station/types';
import { WaveformFilter } from '~graphql/waveform/types';
import {
  AnalystActivity,
  MeasurementMode,
  StageInterval,
  TimeInterval,
  WaveformSortType
} from '~state/analyst-workspace/types';
import { FilterType, FkThumbnailSize } from './components/fk-thumbnail-list/fk-thumbnails-controls';
import { LeadLagPairs } from './constants';

export interface FkParams {
  windowParams: FkTypes.WindowParameters;
  frequencyPair: FrequencyBand;
}

export interface LeadLagPairAndString {
  leadLagPairs: LeadLagPairs;
  windowParams: FkTypes.WindowParameters;
}

export enum FkUnits {
  FSTAT = 'FSTAT',
  POWER = 'POWER'
}

/**
 * Used to return a superset of the fk configuration from the fk config popover
 */
export interface FkConfigurationWithUnits extends FkConfiguration {
  fkUnitToDisplay: FkUnits;
}

/**
 * Azimuth Slowness Redux Props
 */
export interface AzimuthSlownessReduxProps {
  // passed in from golden-layout
  glContainer?: Gl.Container;
  currentStageInterval: StageInterval;
  currentTimeInterval: TimeInterval;
  selectedSdIds: string[];
  openEventId: string;
  selectedSortType: WaveformSortType;
  sdIdsToShowFk: string[];
  analystActivity: AnalystActivity;
  measurementMode: MeasurementMode;
  channelFilters: Immutable.Map<string, WaveformTypes.WaveformFilter>;
  client: ApolloClient<any>;
  setSelectedSdIds(ids: string[]): void;
  setSdIdsToShowFk(signalDetectionIds: string[]): void;
}

export interface SubscriptionAction {
  (list: SignalDetectionTypes.SignalDetection[],
    index: number,
    prev: SignalDetectionTypes.SignalDetection[],
    currentIteree: SignalDetectionTypes.SignalDetection): void;
}

/**
 * Azimuth Slowness State
 */
export interface AzimuthSlownessState {
  fkThumbnailSizePx: FkThumbnailSize;
  fkThumbnailColumnSizePx: number;
  filterType: FilterType;
  userInputFkWindowParameters: FkTypes.WindowParameters;
  userInputFkFrequency: FkTypes.FrequencyBand;
  numberOfOutstandingComputeFkMutations: number;
  fkUnitsForEachSdId: Immutable.Map<string, FkUnits>;
  fkInnerContainerWidthPx: number;
  fkFrequencyThumbnails: Immutable.Map<string, FkFrequencyThumbnail[]>;
}

/**
 * Mutations used by the Az Slow display
 */
export interface AzimuthSlownessMutations {
  computeFks: MutationFunc<{}>;
  computeFkFrequencyThumbnails: MutationFunc<{}>;
  setWindowLead: MutationFunc<{}>;
  markFksReviewed: MutationFunc<{}>;
}

/**
 * Consolidated props for Azimuth Slowness
 */
export type AzimuthSlownessProps =
  AzimuthSlownessReduxProps
  & ChildProps<AzimuthSlownessMutations>
  & StationTypes.DefaultStationsQueryProps
  & WaveformTypes.DefaultWaveformFiltersQueryProps
  & SignalDetectionTypes.SignalDetectionsByStationQueryProps
  & StationTypes.DistanceToSourceForDefaultStationsQueryProps
  & EventTypes.EventsInTimeRangeQueryProps;

export interface AzimuthSlownessPanelProps {
  // Data
  defaultStations: StationTypes.ProcessingStation[];
  eventsInTimeRange: EventTypes.Event[];
  displayedSignalDetection: SignalDetectionTypes.SignalDetection | undefined;
  openEvent: EventTypes.Event | undefined;
  associatedSignalDetections: SignalDetectionTypes.SignalDetection[];
  signalDetectionsToDraw: SignalDetectionTypes.SignalDetection[];
  signalDetectionsIdToFeaturePredictions: Immutable.Map<string, FeaturePrediction[]>;
  signalDetectionsByStation: SignalDetectionTypes.SignalDetection[];
  featurePredictionsForDisplayedSignalDetection: FeaturePrediction[];
  // Redux state as props
  selectedSdIds: string[];
  channelFilters: Immutable.Map<string, WaveformTypes.WaveformFilter>;
  defaultWaveformFilters: WaveformFilter[];
  selectedSortType: WaveformSortType;
  measurementMode: MeasurementMode;
  sdIdsToShowFk: string[];
  distanceToSource: DistanceToSource[];
  fkFrequencyThumbnails: FkFrequencyThumbnail[];
  // Azimuth display state as props
  fkThumbnailColumnSizePx: number;
  fkDisplayWidthPx: number;
  fkDisplayHeightPx: number;
  filterType: FilterType;
  fkThumbnailSizePx: FkThumbnailSize;
  fkUnitsForEachSdId: Immutable.Map<string, FkUnits>;
  numberOfOutstandingComputeFkMutations: number;
  userInputFkFrequency: FkTypes.FrequencyBand;
  fkUnitForDisplayedSignalDetection: FkUnits;
  userInputFkWindowParameters: FkTypes.WindowParameters;
  fkInnerContainerWidthPx: number;
  // Prop functions
  adjustFkInnerContainerWidth(fkThumbnailsContainer: HTMLDivElement, fkThumbnailsInnerContainer: HTMLDivElement): void;
  markFksForSdIdsAsReviewed(sdIds: string[]): void;
  updateFkThumbnailSize(size: FkThumbnailSize): void;
  updateFkFilter(filterType: FilterType): void;
  setFkThumbnailColumnSizePx(newSizePx: number): void;
  computeFkAndUpdateState(fkInput: FkTypes.FkInput): Promise<void>;
  changeUserInputFks(windowParams: FkTypes.WindowParameters, frequencyBand: FkTypes.FrequencyBand): void;
  setFkUnitForSdId(sdId: string, fkUnit: FkUnits): void;
  setWindowLead(sdId: string, leadFkSpectrumSeconds: number, windowLead: number): void;
  // Redux functions
  setSelectedSdIds(sdIds: string[]): void;
  setSdIdsToShowFk(sdIds: string[]): void;

}
