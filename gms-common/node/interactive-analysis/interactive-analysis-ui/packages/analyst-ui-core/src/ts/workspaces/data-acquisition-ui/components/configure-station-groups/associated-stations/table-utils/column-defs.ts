import { Column } from '@gms/ui-core-components';

export const associatedStationsDefs: Column[] =
  [
    {
      headerName: 'No Network Selected',
      field: 'stations',
      cellStyle: { 'text-align': 'left' },
      resizable: true,
      sortable: true,
      filter: true,
      colId: 'associated_stations'
    }
  ];
