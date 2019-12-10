
import * as Gl from '@gms/golden-layout';
import * as Immutable from 'immutable';
import { ChildProps, MutationFunc } from 'react-apollo';
import { EventTypes, SignalDetectionTypes, StationTypes } from '~graphql/';
import {
  AnalystActivity,
  MeasurementMode,
  TimeInterval
} from '~state/analyst-workspace/types';

/**
 * Mutations used in the map
 */
// tslint:disable-next-line:no-empty-interface
export interface MapMutations {
  updateEvents: MutationFunc<{}>;
  rejectDetectionHypotheses: MutationFunc<{}>;
  changeSignalDetectionAssociations: MutationFunc<{}>;
  createEvent: MutationFunc<{}>;
  updateDetections: MutationFunc<{}>;
}

// tslint:disable-next-line:no-empty-interface
export interface MapState {
}

/**
 * Props mapped in from Redux state
 */
export interface MapReduxProps {
  // passed in from golden-layout
  glContainer?: Gl.Container;
  currentTimeInterval: TimeInterval;
  selectedEventIds: string[];
  openEventId: string;
  selectedSdIds: string[];
  analystActivity: AnalystActivity;
  measurementMode: MeasurementMode;
  sdIdsToShowFk: string[];

  // callbacks
  setSelectedEventIds(eventIds: string[]): void;
  setSdIdsToShowFk(signalDetectionIds: string[]): void;
  setSelectedSdIds(SdIds: string[]): void;
  setOpenEventId(event: EventTypes.Event): void;
  setMeasurementModeEntries(entries: Immutable.Map<string, boolean>): void;
}

/**
 * Consolidated props type for map
 */
export type MapProps = MapReduxProps
  & ChildProps<MapMutations>
  & StationTypes.DefaultStationsQueryProps
  & EventTypes.EventsInTimeRangeQueryProps
  & SignalDetectionTypes.SignalDetectionsByStationQueryProps;

export enum LayerTooltips {
  Events = 'Seismic Event',
  Stations = 'Station',
  Assoc = 'Signal Detections associated to currently open event',
  OtherAssoc = 'Signal Detections associated to events that are not open',
  UnAssociated = 'Signal Detections unassociated from all events'
}
export enum LayerLabels {
  Events = 'Events',
  Stations = 'Stations',
  Assoc = 'Open Assoc.',
  OtherAssoc = 'Other Assoc.',
  UnAssociated = 'Unassociated'
}
