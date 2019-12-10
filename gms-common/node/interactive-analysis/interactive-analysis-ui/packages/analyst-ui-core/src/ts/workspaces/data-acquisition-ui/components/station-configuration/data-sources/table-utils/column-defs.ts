import { Column } from '@gms/ui-core-components';

export const dataSourcesDefs: Column[] =
  [
    {
      headerName: 'Data source',
      field: 'dataSource',
      cellStyle: { 'text-align': 'left' },
      resizable: true,
      sortable: true,
      filter: true,
      width: 200
    },
    {
      headerName: 'Available formats',
      field: 'availableFormats',
      cellStyle: { 'text-align': 'left' },
      resizable: true,
      sortable: true,
      filter: true,
      width: 300
    }
  ];
