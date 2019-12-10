import { Column } from '@gms/ui-core-components';
import { LocationHistoryCheckBox, LocationSetSwitch } from './cell-renderer-frameworks';

/** The save fieled name */
export const SAVE_FIELD_NAME = 'save';

/** The preferred fieled name */
export const PREFERRED_FIELD_NAME = 'preferred';

/**
 * Column Definitions for Transfer Gaps list
 */
export const columnDefs: Column[] =
  [
    {
      headerName: 'ASBDUSA',
      field: 'locationSetId',
      rowGroup: true,
      cellRendererFramework: LocationSetSwitch,
      sortable: true,
      hide: true
    },
    {
      headerName: 'Type',
      field: 'locType',
      enableCellChangeFlash: true,
      resizable: true,
      width: 90,
      cellStyle: { 'text-align': 'left' },
      headerTooltip: 'Event Location Type (Standard or Master)'
    }, {
      headerName: 'Lat (\u00B0)',
      field: 'lat',
      resizable: true,
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      width: 70,
      headerTooltip: 'Latitude of event location'

    }, {
      headerName: 'Lon (\u00B0)',
      field: 'lon',
      resizable: true,
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      width: 70,
      headerTooltip: 'Longitude of event location'
    }, {
      headerName: 'Depth (km)',
      field: 'depth',
      resizable: true,
      cellStyle: { 'text-align': 'right' },
      width: 90,
      enableCellChangeFlash: true,
      headerTooltip: 'Depth of event location'
    },  {
      headerName: 'Time',
      field: 'time',
      resizable: true,
      cellStyle: { 'text-align': 'right' },
      width: 210,
      enableCellChangeFlash: true,
      headerTooltip: 'Time of event'
    }, {
      headerName: 'Restraint',
      field: 'restraint',
      resizable: true,
      width: 140,
      cellStyle: { 'text-align': 'left' },
      headerTooltip: 'Depth restraint of location calculation'
    }, {
      headerName: 'Semi Major (km)',
      field: 'smajax',
      resizable: true,
      width: 122,
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      headerTooltip: 'Semi-major axis length of error ellipse'
    },
      {
      headerName: 'Semi Minor (km)',
      field: 'sminax',
      resizable: true,
      width: 122,
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      headerTooltip: 'Semi-minor axis length of error ellipse'
    }, {
      headerName: 'Strike (\u00B0)',
      field: 'strike',
      resizable: true,
      width: 70,
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      headerTooltip: 'Strike of major axis of error ellipse'
    }, {
      headerName: 'Std Dev',
      field: 'stdev',
      resizable: true,
      width: 70,
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      headerTooltip: 'Standard deviation of the observed'
    }, {
      headerName: 'Count',
      field: 'count',
      resizable: true,
      hide: true,
      width: 95,
      cellStyle: { 'text-align': 'right' },
      enableCellChangeFlash: true,
      sortable: true
    },
    {
      headerName: 'Preferred',
      field: PREFERRED_FIELD_NAME,
      cellStyle: {
        display: 'flex',
        'justify-content': 'center'
      },
      cellRendererFramework: LocationHistoryCheckBox,
      width: 80,
      headerTooltip: 'Mark an event location as preferred'
    }
  ];
export const autoGroupColumnDef = {
  headerName: 'Save',
  field: SAVE_FIELD_NAME,
  width: 65,
  cellRendererFramework: LocationSetSwitch,
  comparator: (a, b) => {
    if (a === null || b === null) return b - a;
  },
  headerTooltip: 'Save this location set when the event is saved'
};
