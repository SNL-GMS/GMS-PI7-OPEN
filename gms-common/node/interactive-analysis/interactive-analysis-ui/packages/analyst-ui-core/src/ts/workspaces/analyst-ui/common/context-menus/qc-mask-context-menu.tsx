import { Menu, MenuItem } from '@blueprintjs/core';
import * as React from 'react';
import { QcMaskTypes } from '~graphql/';
import { QcMaskDialogBoxType } from '../dialogs/types';

/**
 * QcMaskDialogOpener
 * function to initiate opening of QcMaskDialog
 */
export type QcMaskDialogOpener
  = (eventX: number, eventY: number, qcMask: QcMaskTypes.QcMask, qcMaskDialogType: QcMaskDialogBoxType) => void;

/**
 * Creates the appropriate blueprint context menu
 * 
 * @param eventX x coordinate of event
 * @param eventY y coordinate of event
 * @param mask mask to modify or reject
 * @param openDialog function to open mask dialog
 * @param isRejected is mask rejected or not
 * 
 * @returns jsx for an blueprint menu
 */
export function QcMaskContextMenu(
  eventX: number, eventY: number, mask: QcMaskTypes.QcMask,
  openDialog: QcMaskDialogOpener, isRejected: boolean): JSX.Element {
  if (isRejected) {
    return (
      <Menu>
        <MenuItem
          text="View"
          disabled={false}
          onClick={e => {
            e.stopPropagation();
            openDialog(eventX, eventY, mask, QcMaskDialogBoxType.View);
          }}
        />
      </Menu>
    );
  } else {
    return (
      <Menu>
        <MenuItem
          text="Modify"
          disabled={false}
          onClick={e => {
            e.stopPropagation();
            openDialog(eventX, eventY, mask, QcMaskDialogBoxType.Modify);
          }}
        />
        <MenuItem
          text="Reject"
          disabled={false}
          onClick={e => {
            e.stopPropagation();
            openDialog(eventX, eventY, mask, QcMaskDialogBoxType.Reject);
          }}
        />
      </Menu>
    );
  }
}
