import { Row } from '@gms/ui-core-components';
import { EventTypes } from '~graphql/';

/**
 * LocationHistory Props
 */
export interface LocationHistoryProps {
  // Current open event
  event: EventTypes.Event;
  selectedLocationSolutionSetId: string;
  selectedLocationSolutionId: string;
  setSelectedLSSAndLS(locationSolutionSetId: string, locationSolutionId: string): void;
}
/**
 * Location History State
 */
export interface LocationHistoryState {
  preferredLSSetId: string;
  preferredLSid: string;
}

// TODO Determine what type of information is actually needed to populate this
// export interface PreferredInLocation {
//   preferDefault: boolean;
//   preferFirstChild: boolean;
//   preferSecondChild: boolean;
// }
export interface LocationHistoryRow extends Row {
  locationSolutionId: string;
  locationSetId: string;
  locType: string;
  lat: number | string;
  lon: number | string;
  depth: number | string;
  time: string;
  restraint: string;
  smajax: number | string;
  sminax: number| string;
  strike: number| string;
  stdev: number | string;
  depthRestraintType: EventTypes.DepthRestraintType;
  count: number;
  selectedLocationSolutionSetId: string;
  isLocationSolutionSetPreferred?: boolean;
  isLastInLSSet?: boolean;
  isFirstInLSSet?: boolean;
  preferred?: boolean;
  locationGroup?: LocationHistoryRow[];
  setPreferred(locationSolutionId: string, locationSolutionSetId: string): void;
  setToSave(locationSolutionId: string, locationSolutionSetId: string): void;
}
