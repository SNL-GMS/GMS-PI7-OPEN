import { Column } from '@gms/ui-core-components';

export const stationConfigDefs: Column[] =
  [
    {
      headerName: 'Site',
      field: 'name',
      cellStyle: { 'text-align': 'left' },
      resizable: true,
      sortable: true,
      filter: true,
      width: 80
    },
    {
      headerName: 'ID',
      field: 'id',
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      width: 150
    },
    {
      headerName: 'Latitude',
      field: 'latitude',
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      width: 100
    },
    {
      headerName: 'Longitude',
      field: 'longitude',
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      width: 100
    },
    {
      headerName: 'Elevation',
      field: 'elevation',
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      width: 100
    },
    {
      headerName: 'North offset',
      field: 'northOffset',
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      width: 100
    },
    {
      headerName: 'East offset',
      field: 'eastOffset',
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      width: 100
    },
    {
      headerName: 'Sample rate',
      field: 'sampleRate',
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      width: 100
    },
  ];
