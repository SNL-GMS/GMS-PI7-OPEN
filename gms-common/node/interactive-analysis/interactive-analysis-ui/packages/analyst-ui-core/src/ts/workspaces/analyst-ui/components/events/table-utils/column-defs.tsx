
import { Column } from '@gms/ui-core-components';
import * as moment from 'moment';
import { analystUiConfig, userPreferences } from '~analyst-ui/config';
import { EventConflictMarker, EventModifiedDot, MarkCompleteCellRenderer } from './cell-renderer-frameworks';

/**
 * Definition of columns used in event-list Render function
 */
export const columnDefs: Column[] =
  [
    {
      headerName: '',
      field: 'modified',
      cellStyle: { display: 'flex', 'justify-content': 'center', 'align-items': 'center' },
      enableCellChangeFlash: true,
      width: 20,
      cellRendererFramework: EventModifiedDot,
      resizable: true,
      sortable: true,
      filter: true,
    }, {
      headerName: 'Time',
      field: 'time',
      width: 100,
      resizable: true,
      sortable: true,
      filter: true,
      cellStyle: { 'text-align': 'center' },
      valueFormatter: e => moment.unix(e.data.time)
        .utc()
        .format('HH:mm:ss')
    }, {
      headerName: 'ID',
      field: 'id',
      hide: !userPreferences.eventList.showIds,
      width: 75,
    }, {
      headerName: '#Det',
      field: 'numDetections',
      cellStyle: params =>
        ({
          'background-color': params.value <= 0 ?
            analystUiConfig.userPreferences.colors.events.noSignalDetections
            : '',
          color: params.value <= 0 ? 'black'
          : 'white',
          textAlign: 'center'
        }),
      width: 50,
      resizable: true,
      sortable: true,
      filter: true,
    }, {
      headerName: 'Lat\u00B0',
      field: 'lat',
      width: 70,
      resizable: true,
      sortable: true,
      filter: true,
      cellStyle: { 'text-align': 'right' },
      valueFormatter: e => e.data.lat.toFixed(3)
    }, {
      headerName: 'Lon\u00B0',
      field: 'lon',
      width: 70,
      resizable: true,
      sortable: true,
      filter: true,
      cellStyle: { 'text-align': 'right' },
      valueFormatter: e => e.data.lon.toFixed(3)
    }, {
      headerName: 'Depth (km)',
      field: 'depth',
      width: 90,
      resizable: true,
      sortable: true,
      filter: true,
      cellStyle: { 'text-align': 'right' },
      valueFormatter: e => e.data.depth.toFixed(2)
    }, {
      headerName: 'Active analysts',
      field: 'activeAnalysts',
      resizable: true,
      sortable: true,
      filter: true,
      enableCellChangeFlash: true,
      valueGetter: e => e.data.activeAnalysts.toString(),
      width: 115
    }, {
      headerName: 'Conflict',
      field: 'conflictingSdHyps',
      cellRendererFramework: EventConflictMarker,
      width: 70,
      resizable: true,
      sortable: true,
      filter: true,
      cellStyle: {
        display: 'flex',
        'justify-content': 'center',
        'align-items': 'center',
        padding: '3px 0 0 0'
      }
    },
    {
      headerName: 'Mark Complete',
      field: 'status',
      cellRendererFramework: MarkCompleteCellRenderer,
      width: 120,
      resizable: true,
      sortable: true,
      filter: true,
    },
  ];
