import { Row } from '@gms/ui-core-components';
import { StationTypes } from '~graphql/';

/**
 * ConfigureStationGroupsState State
 */
export interface ConfigureStationGroupsState {
  networkOriginalTableData: NetworkRow[];
  networkTableData: NetworkRow[];
  currentNetworkID: string;
  networkToStationsMap: Map<string, StationsSelectionState>;
  showStationsTables: boolean;
  selectedFilter: FilterType;
}

/**
 * This is used for the states of the stations associated 
 * and available with the currently selected network
 */
export interface StationsSelectionState {
  associatedStationsOriginalTableData: StationsRow[];
  associatedStationsTableData: StationsRow[];

  availableStationsOriginalTableData: StationsRow[];
  availableStationsTableData: StationsRow[];
}

/**
 * Different filters that are available
 */
export enum FilterType {
  ALL_STATIONS = 'All stations',
  ACTIVE = 'Active',
  INACTIVE = 'Inactive'
}

/**
 * ConfigureStationGroupsState Props
 */
export type ConfigureStationGroupsProps =
  StationTypes.DefaultStationsQueryProps;

/**
 * Interface that defines the expected data types for
 * a Stations table row.
 */
export interface StationsRow extends Row {
  id: string;
  stations: string;
}

/**
 * Interface that defines the expected data types for
 * a network table row.
 */
export interface NetworkRow extends Row {
  id: string;
  newAndUnsaved: boolean;
  modified: boolean;
  network: string;
  status: string;
  prevStatus: string;
  modifiedTime: string;
}
