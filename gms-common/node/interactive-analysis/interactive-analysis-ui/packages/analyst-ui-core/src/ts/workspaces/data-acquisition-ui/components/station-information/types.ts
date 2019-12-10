import * as Gl from '@gms/golden-layout';
import { Row } from '@gms/ui-core-components';
import { StationTypes } from '~graphql/';
import { ProcessingStation } from '~graphql/station/types';

/**
 * Interface that defines the expected datatypes for
 * a Popover Edc table row.
 */
export interface PopoverStationNamesRow extends Row {
  id: string;
  stationName: string;
}

/**
 * Different filters that are available
 */
export enum FilterType {
  ACQUIRED = 'All Stations',
  ACTIVE = 'Enabled',
  INACTIVE = 'Disabled',
  INTERACTIVE_PROCESSING_DEFAULT = 'Interactive Processing - Available by default',
  INTERACTIVE_PROCESSING_REQUEST = 'Interactive Processing - Available by request',
  STATION_PROCESSING = 'Station Processing stations',
  NETWORK_PROCESSING = 'Network Processing stations',
  DISABLED_PROCESSING = 'Disabled Processing stations'
}

/**
 * Dropdown values for Data acquisition column
 */
export enum DataAcquisitionStatus {
  ENABLED = 'enabled',
  DISABLED = 'disabled'
}

/**
 * Dropdown values for Interactive processing column
 */
export enum InteractiveProcessingStatus {
  BY_DEFAULT = 'default',
  ON_REQUEST = 'request'
}

/**
 * Dropdown values for Automatic processing column
 */
export enum AutomaticProcessingStatus {
  STATION_ONLY = 'station',
  NETWORK = 'network',
  DISABLED = 'disabled'
}

/**
 * Interface that defines the expected data types for
 * a Station Information table row.
 */
export interface StationInformationRow extends Row {
  id: string;
  stationId: string;
  modified: boolean;
  station: string;
  dataAcquisition: string;
  interactiveProcessing: string;
  automaticProcessing: string;
  configure: string;
  color: string;
}

/**
 * StationInformation State
 */
export interface StationInformationState {
  originalTableData: StationInformationRow[];
  tableData: StationInformationRow[];
  selectedFilter: FilterType;
  disableEditSelected: boolean;
  batchEditPopupIsOpen: boolean;
  rowId: number;
  popoverDataAcquisitionStatusValue: DataAcquisitionStatus;
  popoverInteractiveProcessingStatusValue: InteractiveProcessingStatus;
  popoverAutomaticProcessingStatusValue: AutomaticProcessingStatus;
  importCssPopupIsOpen: boolean;
  siteFilename: string;
  siteFile: any;
  sitechanFilename: string;
  sitechanFile: any;
  importedStationNames: string[];
  stationsFromFiles: ProcessingStation[];
  selectedStationFromFiles: ProcessingStation;
}

/**
 * StationInformation Props
 */
export type StationInformationProps =
  StationInformationReduxProps
  & StationTypes.DefaultStationsQueryProps;

/**
 * StationConfiguration Redux Props
 */
export interface StationInformationReduxProps {
  // Redux state, added to props via mapStateToProps
  glContainer?: Gl.Container;
  selectedStationIds: string[];
  selectedProcessingStation: ProcessingStation;
  unmodifiedProcessingStation: ProcessingStation;
  setSelectedStationIds(ids: string[]): void;
  setSelectedProcessingStation(station: ProcessingStation): void;
  setUnmodifiedProcessingStation(station: ProcessingStation): void;
}
