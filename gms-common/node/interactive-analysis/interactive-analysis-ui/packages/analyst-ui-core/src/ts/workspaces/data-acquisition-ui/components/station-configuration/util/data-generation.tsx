import * as lodash from 'lodash';
import { DataDestinationsRow } from '../data-destinations/types';
import { DataAcquisitionProtocols, DataSources, DataSourcesRow } from '../data-sources/types';

// TODO: Data for the Data sources and Data Destinations sub-tables in the station-configuration
// TODO: display is being generated in the following 2 methods.
// TODO: This is a temporary solution.
// TODO: Data will be generated in the gateway as per UI standards.
export function generateDataSourcesData(): DataSourcesRow[] {
  const dataSourcesRows = [];
  const maxRows = 10;
  for (let i = 0; i < maxRows; i++) {
    // get a random enum value
    // the length of an enum is double its number of elements due to hidden index
    let idx = Math.floor(Math.random() * (Object.keys(DataSources).length) / 2);
    const sourceName = DataSources[idx];
    idx = Math.floor(Math.random() * (Object.keys(DataAcquisitionProtocols).length) / 2);
    const dataFormat = DataAcquisitionProtocols[idx];
    const tempDataSourcesRow: DataSourcesRow = {
      id: String(i),
      dataSource: sourceName,
      availableFormats: dataFormat
    };
    dataSourcesRows.push(tempDataSourcesRow);
  }
  return lodash.sortBy(dataSourcesRows, 'dataSource');
}

export function generateDataDestinationsData(): DataDestinationsRow[] {
  const dataDestinationsRows = [];
  const maxRows = 3;
  const lowerCaseConversionCode = 65;
  for (let i = 0; i < maxRows; i++) {
    const tempDataDestinationsRow: DataDestinationsRow = {
      id: String(i),
      dataDestinations: 'EDC ' + String.fromCharCode(i + lowerCaseConversionCode),
      enableForwarding: ''
    };
    dataDestinationsRows.push(tempDataDestinationsRow);
  }
  return dataDestinationsRows;
}
