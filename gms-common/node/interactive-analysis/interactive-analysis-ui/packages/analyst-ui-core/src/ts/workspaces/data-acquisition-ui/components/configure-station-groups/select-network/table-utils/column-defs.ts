import { Column } from '@gms/ui-core-components';
import { NetworkModifiedDot } from './cell-renderer-frameworks';

export const selectNetworkDefs: Column[] =
  [
    {
      headerName: '',
      field: 'modified',
      cellStyle: { display: 'flex', 'justify-content': 'center', 'align-items': 'center' },
      enableCellChangeFlash: true,
      width: 20,
      cellRendererFramework: NetworkModifiedDot,
      resizable: true,
      sortable: true,
      filter: true,
    },
    {
      headerName: 'Network',
      field: 'network',
      cellStyle: { 'text-align': 'left' },
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      editable: true,
    },
    {
      headerName: 'Status',
      field: 'status',
      cellStyle: { 'text-align': 'left' },
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
    },
    {
      headerName: 'Last Modified',
      field: 'modifiedTime',
      cellStyle: { 'text-align': 'left' },
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
    }
  ];
