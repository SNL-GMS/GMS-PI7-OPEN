import { Column } from '@gms/ui-core-components';

export const availableStationsDefs: Column[] =
  [
    {
      headerName: 'Available Stations',
      field: 'stations',
      cellStyle: { 'text-align': 'left' },
      resizable: true,
      sortable: true,
      filter: 'agTextColumnFilter',
      filterParams: {
        filterOptions: ['contains']},
      width: 200
      }
  ];
