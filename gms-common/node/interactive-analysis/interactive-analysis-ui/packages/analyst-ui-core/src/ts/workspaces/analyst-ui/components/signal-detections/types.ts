import * as Gl from '@gms/golden-layout';
import { Row } from '@gms/ui-core-components';
import * as Immutable from 'immutable';
import { ChildProps, MutationFunc } from 'react-apollo';
import { EventTypes, SignalDetectionTypes, StationTypes } from '~graphql/';
import {
  AnalystActivity,
  MeasurementMode,
  TimeInterval
} from '~state/analyst-workspace/types';

/**
 * Different filters that are available
 */
export enum FilterType {
  allRows = 'All Detections',
  openEvent = 'Open Event',
  complete = 'Completed',
}

/**
 * Table row object for signal detections
 */
export interface SignalDetectionsRow extends Row {
  id: string;
  hypothesisId: string;
  station: string;
  phase: string;
  time: number;
  timeUnc: number;
  assocEventId: string;
  color: string;
  // Added for filtering
  isSelectedEvent: boolean;
  // Added for filtering
  isComplete: boolean;
  associationModified: boolean;
}

/**
 * Mutations used by the Signal Detections display
 */
export interface SignalDetectionsMutations {
  // {} because we don't care about mutation results for now, handling that through subscriptions
  updateDetections: MutationFunc<{}>;
  rejectDetectionHypotheses: MutationFunc<{}>;
  changeSignalDetectionAssociations: MutationFunc<{}>;
  createEvent: MutationFunc<{}>;

}

/**
 * Props mapped in from Redux state
 */
export interface SignalDetectionsReduxProps {
  // Passed in from golden-layout
  glContainer?: Gl.Container;
  // The currently-open processing interval time range
  currentTimeInterval: TimeInterval;
  // The currently-open event hypothesis IDs
  openEventId: string;
  // The currently-selected signal detection IDs
  selectedSdIds: string[];
  analystActivity: AnalystActivity;
  /** The measurement mode */
  measurementMode: MeasurementMode;
  sdIdsToShowFk: string[];

  // callbacks
  setSelectedSdIds(ids: string[]): void;
  setSdIdsToShowFk(signalDetectionIds: string[]): void;
  setMeasurementModeEntries(entries: Immutable.Map<string, boolean>): void;
}

/**
 * Signal detection list local state
 */
export interface SignalDetectionsState {
  selectedFilter: FilterType;
  userSetFilter: boolean;
}

/**
 * Consolidated props type for signal detection list
 */
export type SignalDetectionsProps = SignalDetectionsReduxProps
  & ChildProps<SignalDetectionsMutations>
  & StationTypes.DefaultStationsQueryProps
  & SignalDetectionTypes.SignalDetectionsByStationQueryProps
  & EventTypes.EventsInTimeRangeQueryProps;

export type SignalDetectionRejector = (sdIds: string[]) => void;
