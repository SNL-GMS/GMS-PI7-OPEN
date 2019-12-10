/**
 * A helper utility that builds buttons for the Table view in the qcMaskDialogBox
 */
import { Button, Icon, Intent } from '@blueprintjs/core';
import { Column, Table, TimeUtil } from '@gms/ui-core-components';
import * as classNames from 'classnames';
import * as React from 'react';

/**
 * Builds a modify or reject button
 * 
 * @param params Table (ag-grid) parameters
 * 
 * @returns a JSX.Element or null
 */
export function modifyButton(params): JSX.Element | null {

  const modifyIcon = (<Icon icon="edit" title={false} />);
  const viewIcon = (<Icon icon="eye-open" title={false} />);

  return (
    !params.value.disabled ?
      (
        <Button
          onClick={e => {
            e.stopPropagation();
            params.value.onClick(e.clientX, e.clientY, params);
          }}
          className="qc-mask-history-table__button"
          icon={modifyIcon}
          small={true}
          minimal={true}
          title={'modify'}

        />
      )
      : (
        <Button
          onClick={e => {
            e.stopPropagation();
            params.value.onClick(e.clientX, e.clientY, params);
          }}
          className="qc-mask-history-table__button"
          icon={viewIcon}
          title={'view'}
          small={true}
          minimal={true}

        />)
  );
}
/**
 * Builds a button for selecting qc masks
 * 
 * @param Table (ag-grid) parameters
 * 
 * @returns a JSX.Element or null
 */
export function selectButton(params) {
  const selectIcon = (<Icon icon="select" title={false} />);

  return (
    !params.value.disabled ?
      (
        <Button
          onClick={e => {
            e.stopPropagation();
            params.value.onClick(e.clientX, e.clientY, params);
          }}
          className="qc-mask-history-table__button"
          icon={selectIcon}
          small={true}
          minimal={true}
          title={'select'}
        />)
      : null
  );
}
/**
 * Builds a button for opening the qc mask reject dialog
 * 
 * @param Table (ag-grid) parameters
 * 
 * @returns a JSX.Element or null
 */
export function rejectButton(params) {
  const rejectIcon = (<Icon icon="cross" title={false} />);

  return (
    !params.value.disabled ?
      (
        <Button
          onClick={e => {
            e.stopPropagation();
            params.value.onClick(e.clientX, e.clientY, params);
          }}
          className="qc-mask-history-table__button qc-mask-history-table__button--reject"
          icon={rejectIcon}
          small={true}
          minimal={true}
          title={'reject'}
          intent={Intent.DANGER}
        />)
      : null
  );
}

/**
 * Column definitions for the overlapping mask table.
 */
const OVERLAPPING_MASKS_COLUMN_DEFINITIONS: Column[] = [
  {
    headerName: '',
    field: 'select',
    cellStyle: { 'text-align': 'left', 'vertical-align': 'middle' },
    width: 25,
    cellRendererFramework: selectButton,
    enableCellChangeFlash: true

  },
  {
    headerName: '',
    field: 'modify',
    cellStyle: { 'text-align': 'left', 'vertical-align': 'middle' },
    width: 25,
    cellRendererFramework: modifyButton,
    enableCellChangeFlash: true
  },
  {
    headerName: '',
    field: 'reject',
    cellStyle: { 'text-align': 'left', 'vertical-align': 'middle' },
    width: 25,
    cellRendererFramework: rejectButton,
    enableCellChangeFlash: true

  },
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

/**
 * Render the Mask table (used for history and overlapping masks).
 * 
 * @param tableProps a set of props used by table
 * 
 * @returns a JSX.Element with a rendered table
 */
export const renderOverlappingMaskTable = (tableProps: {}) => (
  <div
    className={classNames('ag-dark', 'qc-mask-overlapping-table')}
  >
    <div style={{ flex: '1 1 auto', position: 'relative', minHeight: '150px' }}>
      <div className="max">
        <Table
          columnDefs={OVERLAPPING_MASKS_COLUMN_DEFINITIONS}
          getRowNodeId={node => node.id}
          rowSelection="single"
          {...tableProps}
        />
      </div>
    </div>
  </div>
);
