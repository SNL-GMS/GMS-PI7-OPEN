import { Column } from '@gms/ui-core-components';
import * as moment from 'moment';
import { systemConfig, userPreferences } from '~analyst-ui/config';
import { DetectionColorCellRenderer, ModifiedDot, SignalDetectionConflictMarker } from './cell-renderer-frameworks';

/**
 * Column Definitions for Signal Detection List
 */
export const columnDefs: Column[] =
  [
    {
      headerName: '',
      field: 'modified',
      cellStyle: { display: 'flex', 'justify-content': 'center', 'align-items': 'center' },
      enableCellChangeFlash: true,
      width: 20,
      resizable: true,
      sortable: true,
      filter: true,
      cellRendererFramework: ModifiedDot
    }, {
      cellStyle: { display: 'flex', 'justify-content': 'center' },
      width: 50,
      field: 'color',
      headerName: '',
      resizable: true,
      sortable: true,
      filter: true,
      cellRendererFramework: DetectionColorCellRenderer,
    }, {
      headerName: 'Hyp ID',
      field: 'hypothesisId',
      cellStyle: { 'text-align': 'center' },
      enableCellChangeFlash: true,
      resizable: true,
      sortable: true,
      filter: true,
      hide: !userPreferences.signalDetectionList.showIds,
      width: 100,
    }, {
      headerName: 'Time',
      field: 'time',
      cellStyle: { 'text-align': 'center' },
      width: 100,
      enableCellChangeFlash: true,
      editable: true,
      resizable: true,
      sortable: true,
      filter: true,
      valueFormatter: e => moment.unix(e.data.time)
        .utc()
        .format('HH:mm:ss')
    }, {
      headerName: 'Station',
      field: 'station',
      width: 70,
      resizable: true,
      sortable: true,
      filter: true,
      cellStyle: { 'text-align': 'left' },
    }, {
      headerName: 'Phase',
      field: 'phase',
      editable: true,
      resizable: true,
      sortable: true,
      filter: true,
      cellEditor: 'agSelectCellEditor',
      cellEditorParams: {
        values: systemConfig.defaultSdPhases
      },
      cellStyle: { 'text-align': 'left' },
      enableCellChangeFlash: true,
      width: 60
    }, {
      headerName: 'Time Unc',
      field: 'timeUnc',
      editable: true,
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      width: 80,
      resizable: true,
      sortable: true,
      filter: true,
    }, {
      headerName: 'Assoc Evt ID',
      field: 'assocEventId',
      cellStyle: { 'text-align': 'center' },
      enableCellChangeFlash: true,
      width: 100,
      resizable: true,
      sortable: true,
      filter: true,
      hide: !userPreferences.signalDetectionList.showIds,
      valueFormatter: e => e.data.assocEventId ? e.data.assocEventId : 'N/A'
    }, {
      headerName: 'Conflict',
      field: 'possiblyConflictingEvents',
      cellRendererFramework: SignalDetectionConflictMarker,
      width: 30,
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
  ];
