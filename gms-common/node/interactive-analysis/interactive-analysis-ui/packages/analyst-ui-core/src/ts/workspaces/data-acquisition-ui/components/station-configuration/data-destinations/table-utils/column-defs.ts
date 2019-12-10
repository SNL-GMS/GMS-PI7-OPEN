import { Column } from '@gms/ui-core-components';

export const dataDestinationsDefs: Column[] =
  [
    {
      headerName: 'Data destination',
      field: 'dataDestinations',
      resizable: true,
      sortable: true,
      filter: true,
      width: 300
    },
    {
      headerName: 'Enable forwarding',
      cellRenderer: params =>
        `<input type='checkbox' ${params.value ? 'checked' : ''} />`,
      field: 'enableForwarding',
      cellStyle: { 'text-align': 'center' },
      resizable: true,
      width: 200
    }
  ];
