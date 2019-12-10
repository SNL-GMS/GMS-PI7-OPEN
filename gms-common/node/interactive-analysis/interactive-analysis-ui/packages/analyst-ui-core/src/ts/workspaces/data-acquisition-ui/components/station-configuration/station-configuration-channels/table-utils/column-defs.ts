import { Column } from '@gms/ui-core-components';

export const channelDefs: Column[] =
  [
    {
      headerName: 'Channel',
      field: 'channelName',
      cellStyle: { 'text-align': 'left' },
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      width: 80
    },
    {
      headerName: 'ID',
      field: 'id',
      cellStyle: { 'text-align': 'left' },
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      width: 150
    },
    {
      headerName: 'Type',
      field: 'type',
      cellStyle: { 'text-align': 'left' },
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      width: 100
    },
    {
      headerName: 'System Change Time',
      field: 'systemChangeTime',
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      width: 100
    },
    {
      headerName: 'Actual Change Time',
      field: 'actualChangeTime',
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      width: 100
    },
    {
      headerName: 'Depth',
      field: 'depth',
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      width: 100
    }
  ];
