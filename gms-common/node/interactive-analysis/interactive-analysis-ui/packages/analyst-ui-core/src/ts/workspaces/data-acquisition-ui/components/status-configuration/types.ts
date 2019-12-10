import { Row } from '@gms/ui-core-components';
import { StationTypes } from '~graphql/';

/**
 * Different filters that are available
 */
export enum FilterType {
  ALL_STATIONS = 'All stations',
  ACQUIRE = 'Acquire',
  DONT_ACQUIRE = 'Don\'t Acquire',
  PKI_IN_USE = 'PKI in use',
  PKI_NOT_IN_USE = 'PKI not in use',
  UPLOAD_TO_PROCESSING_PARTITION = 'Upload to Processing Partition',
  DONT_UPLOAD_TO_PROCESSING_PARTITION = 'Don\'t Upload to Processing Partition',
  STORE_ON_ACQUISITION_PARTITION = 'Store on Acquisition Partition',
  DONT_STORE_ON_ACQUISITION_PARTITION = 'Don\'t Store on Acquisition Partition'
}

/**
 * Interface that defines the expected datatypes for
 * a Station Configuration table row.
 */
export interface StatusConfigurationRow extends Row {
  id: string;
  station: string;
  acquisition: string;
  pkiStatus: string;
  pkiInUse: string;
  processingPartition: string;
  storeOnAcquisitionPartition: string;
  edcA: boolean;
  edcB: boolean;
  edcC: boolean;
  color: string;
  modified: boolean;
}

/**
 * Interface that defines the expected datatypes for
 * a Popover Edc table row.
 */
export interface PopoverEdcRow extends Row {
  id: string;
  dataCenter: string;
  enabled: boolean;
}

/**
 * Dropdown values for Acquisition colukmn.
 */
export enum Acquisition {
  ACQUIRE = 'Acquire',
  DONT_ACQUIRE = 'Don\'t Acquire'
}
/**
 * Dropdown values for PKI in Use column.
 */
export enum PkiInUse {
  ENABLED = 'Enabled',
  DISABLED = 'Disabled'
}

/**
 * Values for PKI status column.
 */
export enum PkiStatus {
  INSTALLED = 'Installed',
  NOT_INSTALLED = 'Not installed',
  NEARING_EXPIRATION = 'Nearing expiration',
  EXPIRED = 'Expired'
}

/**
 * Values for Store on Processing Partition column.
 */
export enum StoreOnAcquisitionPartition {
  STORE = 'Store',
  DONT_STORE = 'Don\'t store'
}

/**
 * Values for Processing Partition column.
 */
export enum ProcessingPartition {
  UPLOAD = 'Upload',
  DONT_UPLOAD = 'Don\'t upload'
}

/**
 * StatusConfiguration State
 */
export interface StatusConfigurationsState {
  originalTableData: StatusConfigurationRow[];
  tableData: StatusConfigurationRow[];
  popupIsOpen: boolean;
  disableEditSelected: boolean;
  selectedFilter: FilterType;
  popover: {
    acquisitionValue: Acquisition;
    pkiInUseValue: PkiInUse;
    processingPartitionValue: ProcessingPartition;
    storeOnAcquisitionParitionValue: StoreOnAcquisitionPartition;
    edcTableData: PopoverEdcRow[];
  };
}

/**
 * StatusConfiguration Props
 */
export type StatusConfigurationsProps = StationTypes.DefaultStationsQueryProps;
