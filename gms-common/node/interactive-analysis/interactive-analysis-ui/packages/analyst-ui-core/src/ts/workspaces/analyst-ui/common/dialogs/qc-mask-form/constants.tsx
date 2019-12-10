import { Column, TimeUtil } from '@gms/ui-core-components';
import * as React from 'react';

export const MILLI_SEC = 1000;

/**
 * Column definitions for the overlapping mask table.
 */
export const MASK_HISTORY_COLUMN_DEFINTIONS: Column[] = [
  {
    headerName: '',
    field: 'color',
    cellStyle: { 'text-align': 'left', 'vertical-align': 'middle' },
    width: 30,
    cellRendererFramework: e => (
      <div
        style={{
          height: '10px',
          width: '20px',
          backgroundColor: e.data.color,
          marginTop: '4px'
        }}
      />
    ),
    enableCellChangeFlash: true
  },
  {
    headerName: 'Creation time',
    field: 'timestamp',
    cellStyle: { 'text-align': 'left' },
    width: 170,
    enableCellChangeFlash: true,
    valueFormatter: e => TimeUtil.toString(e.data.creationTime)
  },
  {
    headerName: 'Category',
    field: 'category',
    cellStyle: { 'text-align': 'left' },
    width: 130,
    enableCellChangeFlash: true
  },
  {
    headerName: 'Type',
    field: 'type',
    cellStyle: { 'text-align': 'left' },
    width: 130,
    enableCellChangeFlash: true
  },
  {
    headerName: 'Start time',
    field: 'startTime',
    cellStyle: { 'text-align': 'left' },
    width: 170,
    enableCellChangeFlash: true,
    valueFormatter: e => TimeUtil.toString(e.data.startTime)
  },
  {
    headerName: 'End time',
    field: 'endTime',
    cellStyle: { 'text-align': 'left' },
    width: 170,
    enableCellChangeFlash: true,
    valueFormatter: e => TimeUtil.toString(e.data.endTime)
  },
  {
    headerName: 'Author',
    field: 'author',
    cellStyle: { 'text-align': 'left' },
    width: 75,
    enableCellChangeFlash: true
  },
  {
    headerName: 'Rationale',
    field: 'rationale',
    cellStyle: { 'text-align': 'left' },
    width: 300,
    enableCellChangeFlash: true
  }
];
