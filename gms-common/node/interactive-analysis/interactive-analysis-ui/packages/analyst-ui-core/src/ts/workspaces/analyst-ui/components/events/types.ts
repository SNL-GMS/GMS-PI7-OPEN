import * as Gl from '@gms/golden-layout';
import { ChildProps, MutationFunc } from 'react-apollo';
import { userPreferences } from '~analyst-ui/config';
import { EventTypes, SignalDetectionTypes, StationTypes } from '~graphql/';
import { SignalDetectionHypothesis } from '~graphql/signal-detection/types';
import { AnalystActivity, TimeInterval } from '~state/analyst-workspace/types';

/**
 * Types of events which can be show
 */
export enum EventFilters {
  EDGE = 'Edge',
  COMPLETED = 'Completed'
}
export const eventFilterToColorMap: Map<any, string> = new Map();
eventFilterToColorMap.set(EventFilters.EDGE, userPreferences.colors.events.edge);
eventFilterToColorMap.set(EventFilters.COMPLETED, userPreferences.colors.events.complete);

/**
 * Table row object for events
 */
export interface EventsRow {
  id: string;
  eventHypId: string;
  isOpen: boolean;
  stageId: string;
  lat: number;
  lon: number;
  depth: number;
  time: number;
  activeAnalysts: string[];
  numDetections: number;
  status: string;
  edgeEvent: boolean;
  conflictingSdHyps: SignalDetectionHypothesisWithStation[];
}

/**
 * Event list local state
 */
export interface EventsState {
  currentTimeInterval: TimeInterval;
  suppressScrollOnNewData: boolean;
  showEventOfType: Map<EventFilters, boolean>;
}

/**
 * Mutations used in the event list
 */
export interface EventsMutations {
  updateEvents: MutationFunc<{}>;
}

/**
 * Props mapped in from Redux state
 */
export interface EventsReduxProps {
  // Passed in from golden-layout
  glContainer?: Gl.Container;
  currentTimeInterval: TimeInterval;
  analystActivity: AnalystActivity;
  openEventId: string;
  selectedEventIds: string[];

  // callbacks
  setOpenEventId(event: EventTypes.Event): void;
  setSelectedEventIds(ids: string[]): void;
}
export interface SignalDetectionHypothesisWithStation extends SignalDetectionHypothesis {
  stationName?: string;
}
/**
 * Consolidated props type for event list
 */
export type EventsProps = EventsReduxProps
  & ChildProps<EventsMutations>
  & EventTypes.EventsInTimeRangeQueryProps
  & StationTypes.DefaultStationsQueryProps
  & SignalDetectionTypes.SignalDetectionsByStationQueryProps;
