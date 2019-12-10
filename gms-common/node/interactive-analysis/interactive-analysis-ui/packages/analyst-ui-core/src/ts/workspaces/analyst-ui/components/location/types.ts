import * as Gl from '@gms/golden-layout';
import * as Immutable from 'immutable';
import { ChildProps, MutationFunc } from 'react-apollo';
import { EventTypes, SignalDetectionTypes, StationTypes } from '~graphql/';
import {
  AnalystActivity,
  MeasurementMode,
  TimeInterval
} from '~state/analyst-workspace/types';
import { DefiningChange } from './components/location-signal-detections/types';

/**
 * Mutations used by the Location
 */
export interface LocationMutations {
  locateEvent: MutationFunc<{}>;
  updateEvents: MutationFunc<{}>;
  createEvent: MutationFunc<{}>;
  updateFeaturePredictions: MutationFunc<{}>;
  updateDetections: MutationFunc<{}>;
  rejectDetectionHypotheses: MutationFunc<{}>;
  changeSignalDetectionAssociations: MutationFunc<{}>;
}

/**
 * Location State
 */
export interface LocationState {
  topTableHeightPx: number;
  selectedLocationSolutionSetId: string | undefined;
  selectedLocationSolutionId: string | undefined;
  sdDefiningChanges: SignalDetectionTableRowChanges[];
  outstandingLocateCall: boolean;
}

/**
 * List of tool tip messages for location button
 */
export enum LocateButtonTooltipMessage {
  NotEnoughDefiningBehaviors = 'minimum defining behaviors must be set',
  Correct = 'Calculates a new event location',
  BadLocationAttributes = 'Location attributes (depth, latitude, or longitude) are not within constrants',
  InvalidData = 'Error in props or query'
}

/**
 * Lists of Location data state
 */
export enum LocationDataState {
  NO_SDS,
  NO_EVENTS,
  NO_EVENT_OPEN,
  NO_INTERVAL,
  READY
}

/**
 * Changes to LocationSD table per row
 */
export interface SignalDetectionTableRowChanges {
  signalDetectionId: string;
  // Defining diffs
  arrivalTimeDefining: DefiningChange;
  slownessDefining: DefiningChange;
  azimuthDefining: DefiningChange;

}

export interface LocationSDRowDiffs {
  isAssociatedDiff: boolean;
  arrivalTimeDefining: DefiningChange;
  slownessDefining: DefiningChange;
  azimuthDefining: DefiningChange;
  // Value diffs
  channelNameDiff?: boolean;
  arrivalTimeDiff?: boolean;
  azimuthObsDiff?: boolean;
  slownessObsDiff?: boolean;
  phaseDiff?: boolean;

}

export interface DefiningStatus {
  arrivalTimeDefining: DefiningChange;
  slownessDefining: DefiningChange;
  azimuthDefining: DefiningChange;
}

export interface SignalDetectionSnapshotWithDiffs extends EventTypes.SignalDetectionSnapshot {
  diffs: LocationSDRowDiffs;
  rejectedOrUnnassociated: boolean;
}

/**
 * Props mapped in from Redux state
 */
export interface LocationReduxProps {
  // Passed in from golden-layout
  glContainer?: Gl.Container;
  // The currently-open processing interval time range
  currentTimeInterval: TimeInterval;
  // The currently-open event hypothesis IDs
  openEventId: string;
  // The currently-selected signal detection IDs
  selectedSdIds: string[];
  // used for additional time range values
  analystActivity: AnalystActivity;
  /** The measurement mode */
  measurementMode: MeasurementMode;
  sdIdsToShowFk: string[];

  setOpenEventId(event: EventTypes.Event): void;
  setSelectedEventIds(ids: string[]): void;
  setSelectedSdIds(ids: string[]): void;
  setSdIdsToShowFk(signalDetectionIds: string[]): void;
  setMeasurementModeEntries(entries: Immutable.Map<string, boolean>): void;
}

/**
 * Location Props
 */
export type LocationProps = LocationReduxProps
  & ChildProps<LocationMutations>
  & StationTypes.DefaultStationsQueryProps
  & SignalDetectionTypes.SignalDetectionsByStationQueryProps
  & EventTypes.EventsInTimeRangeQueryProps
  & StationTypes.DistanceToSourceForDefaultStationsQueryProps;
