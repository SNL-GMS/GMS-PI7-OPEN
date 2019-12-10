import { Column } from '@gms/ui-core-components';

/**
 * Column Definitions for Transfer Gaps list
 */
export const columnDefs: Column[] =
  [
    {
      headerName: 'Station',
      field: 'name',
      enableCellChangeFlash: true,
      width: 100,
      cellRenderer: 'agGroupCellRenderer',
      resizable: true,
      sortable: true,
      filter: true,
    }, {
      headerName: 'Priority',
      field: 'priority',
      cellStyle: { 'text-align': 'left' },
      enableCellChangeFlash: true,
      editable: true,
      width: 100,
      resizable: true,
      sortable: true,
      filter: true
    }, {
      headerName: 'Gap Start Time',
      cellStyle: { 'text-align': 'right' },
      width: 220,
      field: 'gapStartTime',
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true
    },  {
      headerName: 'Gap End Time',
      field: 'gapEndTime',
      cellStyle: { 'text-align': 'right' },
      width: 220,
      resizable: true,
      sortable: true,
      filter: true,
      enableCellChangeFlash: true,
    }, {
      headerName: 'Duration',
      field: 'duration',
      width: 100,
      cellStyle: { 'text-align': 'right' },
      resizable: true,
      sortable: true,
      filter: true
    }, {
      headerName: 'Location',
      field: 'location',
      width: 120,
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true
    }
  ];
