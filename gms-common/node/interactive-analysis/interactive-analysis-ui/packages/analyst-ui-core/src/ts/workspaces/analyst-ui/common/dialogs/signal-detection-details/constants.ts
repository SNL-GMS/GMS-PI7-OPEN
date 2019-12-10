import { Column, TimeUtil } from '@gms/ui-core-components';
import { formatUncertainty } from './utils';

export const MILLIS_SEC = 1000;

/**
 * Column definitions for the history table.
 */
export const SIGNAL_DETECTION_HISTORY_COLUMN_DEFINITIONS: Column[] = [
  {
    headerName: 'Creation time',
    field: 'creationTime',
    cellStyle: { 'text-align': 'left' },
    width: 165,
    enableCellChangeFlash: true,
    valueFormatter: e => TimeUtil.toString(e.data.creationTime)
  },
  {
    headerName: 'Phase',
    field: 'phase',
    cellStyle: { 'text-align': 'left' },
    width: 70,
    enableCellChangeFlash: true
  },
  {
    headerName: 'Detection time',
    field: 'arrivalTimeMeasurementTimestamp',
    cellStyle: { 'text-align': 'left' },
    width: 165,
    enableCellChangeFlash: true,
    valueFormatter: e => TimeUtil.toString(e.data.arrivalTimeMeasurementTimestamp)
  },
  {
    headerName: 'Time uncertainty',
    field: 'arrivalTimeMeasurementUncertaintySec',
    cellStyle: { 'text-align': 'left' },
    width: 125,
    enableCellChangeFlash: true,
    valueFormatter: e => formatUncertainty(e.data.arrivalTimeMeasurementUncertaintySec)
  },
  {
    headerName: 'Rejected',
    field: 'rejected',
    cellStyle: { 'text-align': 'left' },
    width: 75,
    valueFormatter: e => (e.data.rejected) ? 'Yes' : 'No'
  },
  {
    headerName: 'Author',
    field: 'author',
    cellStyle: { 'text-align': 'left' },
    width: 90,
    enableCellChangeFlash: true
  },
];
