import { Row } from '@gms/ui-core-components';

export interface DataSourcesRow extends Row {
  dataSource: string;
  availableFormats: string;
}

/**
 * Site base values for Data Source Column
 * a 2-digit integer is appended
 * at the time of data generation to create a randomized
 * site, e.g. MK01
 */
export enum DataSources {
  'IRIS',
  'IDC'
}

/**
 * Data acquisition protocol data formats
 */
export enum DataAcquisitionProtocols {
  'CD1.1',
  'Miniseed',
  'IMS2.0',
}
